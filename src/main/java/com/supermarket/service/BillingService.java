package com.supermarket.service;

import com.supermarket.model.*;
import com.supermarket.repository.BillRepository;
import com.supermarket.repository.CustomerRepository;
import com.supermarket.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BillingService {

    private final BillRepository billRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    private static final Map<String, Double> COUPON_CODES = new HashMap<>();

    static {
        COUPON_CODES.put("SAVE10", 0.10);    // 10% discount
        COUPON_CODES.put("SUPER20", 0.20);   // 20% discount
        COUPON_CODES.put("FESTIVE15", 0.15); // 15% discount
        COUPON_CODES.put("WELCOME5", 0.05);  // 5% discount
    }

    public BillingService(BillRepository billRepository, ProductRepository productRepository, CustomerRepository customerRepository) {
        this.billRepository = billRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    public List<Bill> getAllBills() {
        return billRepository.findAllByOrderByDateTimeDesc();
    }

    public Optional<Bill> getBillById(Long id) {
        return billRepository.findById(id);
    }

    public Optional<Bill> getBillByNumber(String billNumber) {
        return billRepository.findByBillNumber(billNumber);
    }

    @Transactional
    public Bill generateBill(BillRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot generate bill: Cart is empty.");
        }

        // 1. Calculate subtotal using Java Stream API
        double subtotal = request.getItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
        subtotal = Math.round(subtotal * 100.0) / 100.0;

        // 2. Calculate Discount using Stream / Map lookup
        double discountRate = 0.0;
        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            String upperCode = request.getCouponCode().trim().toUpperCase();
            discountRate = COUPON_CODES.getOrDefault(upperCode, 0.0);
        } else if (request.getCustomDiscountPercentage() > 0) {
            discountRate = request.getCustomDiscountPercentage() / 100.0;
        }

        double discountAmount = Math.round((subtotal * discountRate) * 100.0) / 100.0;
        double taxableAmount = Math.max(0.0, subtotal - discountAmount);

        // 3. Tax calculation (5% standard GST / VAT)
        double taxRate = 5.0;
        double taxAmount = Math.round((taxableAmount * (taxRate / 100.0)) * 100.0) / 100.0;
        double grandTotal = Math.round((taxableAmount + taxAmount) * 100.0) / 100.0;

        // 4. Generate guaranteed unique bill number (Date + Unique Sequence)
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long randomSuffix = (System.currentTimeMillis() % 90000) + 10000;
        String billNumber = "SB-" + dateStr + "-" + randomSuffix;

        // Ensure collision safety
        while (billRepository.findByBillNumber(billNumber).isPresent()) {
            randomSuffix++;
            billNumber = "SB-" + dateStr + "-" + randomSuffix;
        }

        // 5. Create Bill Entity
        Bill bill = new Bill();
        bill.setBillNumber(billNumber);
        bill.setDateTime(LocalDateTime.now());
        
        String custName = (request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty())
                ? request.getCustomerName().trim() : "Walk-in Customer";
        String custPhone = (request.getCustomerPhone() != null && !request.getCustomerPhone().trim().isEmpty())
                ? request.getCustomerPhone().trim() : "N/A";

        bill.setCustomerName(custName);
        bill.setCustomerPhone(custPhone);
        bill.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod().toUpperCase() : "CASH");
        bill.setSubtotal(subtotal);
        bill.setTaxRate(taxRate);
        bill.setTaxAmount(taxAmount);
        bill.setDiscountAmount(discountAmount);
        bill.setCouponCode(request.getCouponCode());
        bill.setGrandTotal(grandTotal);
        bill.setCashierName("Cashier - Counter 1");

        // 6. Deduct stock and attach BillItems
        for (CartItem cartItem : request.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + cartItem.getProductId()));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for: " + product.getName() + " (Available: " + product.getStockQuantity() + ", Requested: " + cartItem.getQuantity() + ")");
            }

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Create BillItem
            BillItem item = new BillItem(
                    product.getId(),
                    product.getBarcode(),
                    product.getName(),
                    product.getCategory() != null ? product.getCategory().getDisplayName() : "General",
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice()
            );
            bill.addItem(item);
        }

        // 7. Update or Create Customer loyalty points safely
        if (request.getCustomerPhone() != null) {
            String digitsOnly = request.getCustomerPhone().replaceAll("[^0-9]", "");
            if (digitsOnly.length() >= 10) {
                try {
                    Customer customer = customerRepository.findByPhoneNumber(digitsOnly)
                            .orElseGet(() -> {
                                Customer newCustomer = new Customer();
                                newCustomer.setName(custName);
                                newCustomer.setPhoneNumber(digitsOnly);
                                return newCustomer;
                            });

                    // 1 loyalty point per 50 currency units spent
                    int pointsEarned = (int) (grandTotal / 50.0);
                    customer.setLoyaltyPoints(customer.getLoyaltyPoints() + pointsEarned);

                    // Update tier based on points
                    if (customer.getLoyaltyPoints() >= 100) {
                        customer.setMembershipTier("PLATINUM");
                    } else if (customer.getLoyaltyPoints() >= 50) {
                        customer.setMembershipTier("GOLD");
                    } else {
                        customer.setMembershipTier("SILVER");
                    }
                    customerRepository.save(customer);
                } catch (Exception e) {
                    // Non-fatal: loyalty update failure should not fail checkout
                    System.err.println("Notice: Could not update customer loyalty points: " + e.getMessage());
                }
            }
        }

        return billRepository.save(bill);
    }

    public Map<String, Object> getAvailableCoupons() {
        Map<String, Object> coupons = new LinkedHashMap<>();
        coupons.put("SAVE10", "10% Instant Discount on Entire Cart");
        coupons.put("SUPER20", "20% Super Saver Discount");
        coupons.put("FESTIVE15", "15% Festive Special Discount");
        coupons.put("WELCOME5", "5% Welcome Discount for New Shoppers");
        return coupons;
    }
}
