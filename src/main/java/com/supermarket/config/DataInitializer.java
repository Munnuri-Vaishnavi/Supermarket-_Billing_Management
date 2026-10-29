package com.supermarket.config;

import com.supermarket.model.*;
import com.supermarket.repository.BillRepository;
import com.supermarket.repository.CustomerRepository;
import com.supermarket.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final BillRepository billRepository;

    public DataInitializer(ProductRepository productRepository, CustomerRepository customerRepository, BillRepository billRepository) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.billRepository = billRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return; // Data already exists
        }

        // 1. Seed Products across all 7 Categories
        List<Product> sampleProducts = Arrays.asList(
                // Grocery & Staples
                new Product("890103001", "Basmati Rice Royal Premium 5kg", Category.GROCERY, 14.50, 11.00, 45, "bag", "🌾"),
                new Product("890103002", "Whole Wheat Atta Flour 5kg", Category.GROCERY, 8.20, 6.50, 50, "bag", "🌾"),
                new Product("890103003", "Refined Sunflower Cooking Oil 1L", Category.GROCERY, 4.90, 3.80, 60, "bottle", "🪔"),
                new Product("890103004", "Iodized Table Salt 1kg", Category.GROCERY, 1.20, 0.80, 80, "pack", "🧂"),
                new Product("890103005", "Organic Brown Sugar 1kg", Category.GROCERY, 2.80, 2.00, 35, "pack", "🍯"),

                // Dairy & Bakery
                new Product("890103006", "Farm Fresh Pasteurized Milk 1L", Category.DAIRY_BAKERY, 1.80, 1.40, 40, "carton", "🥛"),
                new Product("890103007", "Pure Salted Table Butter 500g", Category.DAIRY_BAKERY, 3.99, 3.10, 25, "pack", "🧈"),
                new Product("890103008", "Whole Grain Brown Bread 400g", Category.DAIRY_BAKERY, 2.10, 1.50, 8, "loaf", "🍞"), // Low stock alert!
                new Product("890103009", "Cheddar Cheese Slices 200g", Category.DAIRY_BAKERY, 3.50, 2.70, 30, "pack", "🧀"),
                new Product("890103010", "Greek Vanilla Yogurt 400g", Category.DAIRY_BAKERY, 2.60, 1.90, 18, "tub", "🍧"),

                // Beverages
                new Product("890103011", "Premium Arabica Roasted Coffee Beans 250g", Category.BEVERAGES, 7.90, 5.50, 22, "pack", "☕"),
                new Product("890103012", "Green Tea Bags with Mint 50s", Category.BEVERAGES, 4.20, 3.00, 28, "box", "🍵"),
                new Product("890103013", "Sparkling Orange Soda 750ml", Category.BEVERAGES, 1.95, 1.20, 55, "bottle", "🥤"),
                new Product("890103014", "Pure Apple & Pomegranate Juice 1L", Category.BEVERAGES, 3.80, 2.80, 32, "bottle", "🧃"),

                // Snacks
                new Product("890103015", "Oreo Dark Chocolate Creme Biscuits 120g", Category.SNACKS, 1.75, 1.20, 65, "pack", "🍪"),
                new Product("890103016", "Roasted & Salted Almonds 200g", Category.SNACKS, 5.40, 4.10, 6, "pouch", "🥜"), // Low stock alert!
                new Product("890103017", "Crispy Kettle Cooked Potato Chips 150g", Category.SNACKS, 2.25, 1.50, 48, "pack", "🥔"),
                new Product("890103018", "Artisan Dark Chocolate 70% 100g", Category.SNACKS, 3.20, 2.20, 35, "bar", "🍫"),

                // Fruits & Veggies
                new Product("890103019", "Fresh Washington Red Apples 1kg", Category.FRUITS_VEGGIES, 3.90, 2.90, 20, "kg", "🍎"),
                new Product("890103020", "Organic Yellow Bananas (Dozen)", Category.FRUITS_VEGGIES, 2.40, 1.60, 15, "dozen", "🍌"),
                new Product("890103021", "Farm Fresh Red Tomatoes 1kg", Category.FRUITS_VEGGIES, 1.80, 1.20, 5, "kg", "🍅"), // Low stock alert!

                // Personal Care
                new Product("890103022", "Moisturizing Aloe Vera Body Wash 500ml", Category.PERSONAL_CARE, 6.50, 4.80, 19, "bottle", "🧴"),
                new Product("890103023", "Anti-Dandruff Herbal Shampoo 400ml", Category.PERSONAL_CARE, 5.90, 4.20, 24, "bottle", "🧴"),

                // Household
                new Product("890103024", "Concentrated Liquid Laundry Detergent 1L", Category.HOUSEHOLD, 7.80, 5.80, 30, "can", "🧼"),
                new Product("890103025", "Antibacterial Surface Cleaner Lemon 500ml", Category.HOUSEHOLD, 3.40, 2.40, 26, "bottle", "✨")
        );

        productRepository.saveAll(sampleProducts);

        // 2. Seed Customers
        Customer c1 = new Customer("John Miller", "9876543210", "john.m@example.com");
        c1.setLoyaltyPoints(45);
        c1.setMembershipTier("SILVER");

        Customer c2 = new Customer("Sarah Connor", "9898989898", "sarah.c@example.com");
        c2.setLoyaltyPoints(120);
        c2.setMembershipTier("PLATINUM");

        Customer c3 = new Customer("David Lee", "9123456789", "david.l@example.com");
        c3.setLoyaltyPoints(70);
        c3.setMembershipTier("GOLD");

        customerRepository.saveAll(Arrays.asList(c1, c2, c3));

        // 3. Seed Sample Historical Bills for Instant Dashboard Charts
        Bill bill1 = new Bill();
        bill1.setBillNumber("SB-20261028-1001");
        bill1.setDateTime(LocalDateTime.now().minusDays(1));
        bill1.setCustomerName("John Miller");
        bill1.setCustomerPhone("9876543210");
        bill1.setPaymentMethod("UPI");
        bill1.setSubtotal(24.30);
        bill1.setTaxRate(5.0);
        bill1.setTaxAmount(1.22);
        bill1.setDiscountAmount(2.43);
        bill1.setCouponCode("SAVE10");
        bill1.setGrandTotal(23.09);
        bill1.addItem(new BillItem(sampleProducts.get(0).getId(), sampleProducts.get(0).getBarcode(), sampleProducts.get(0).getName(), sampleProducts.get(0).getCategory().getDisplayName(), 1, 14.50));
        bill1.addItem(new BillItem(sampleProducts.get(5).getId(), sampleProducts.get(5).getBarcode(), sampleProducts.get(5).getName(), sampleProducts.get(5).getCategory().getDisplayName(), 2, 1.80));
        bill1.addItem(new BillItem(sampleProducts.get(14).getId(), sampleProducts.get(14).getBarcode(), sampleProducts.get(14).getName(), sampleProducts.get(14).getCategory().getDisplayName(), 3, 1.75));
        billRepository.save(bill1);

        Bill bill2 = new Bill();
        bill2.setBillNumber("SB-20261028-1002");
        bill2.setDateTime(LocalDateTime.now().minusHours(18));
        bill2.setCustomerName("Sarah Connor");
        bill2.setCustomerPhone("9898989898");
        bill2.setPaymentMethod("CARD");
        bill2.setSubtotal(34.89);
        bill2.setTaxRate(5.0);
        bill2.setTaxAmount(1.39);
        bill2.setDiscountAmount(6.98);
        bill2.setCouponCode("SUPER20");
        bill2.setGrandTotal(29.30);
        bill2.addItem(new BillItem(sampleProducts.get(10).getId(), sampleProducts.get(10).getBarcode(), sampleProducts.get(10).getName(), sampleProducts.get(10).getCategory().getDisplayName(), 2, 7.90));
        bill2.addItem(new BillItem(sampleProducts.get(6).getId(), sampleProducts.get(6).getBarcode(), sampleProducts.get(6).getName(), sampleProducts.get(6).getCategory().getDisplayName(), 2, 3.99));
        bill2.addItem(new BillItem(sampleProducts.get(22).getId(), sampleProducts.get(22).getBarcode(), sampleProducts.get(22).getName(), sampleProducts.get(22).getCategory().getDisplayName(), 1, 5.90));
        bill2.addItem(new BillItem(sampleProducts.get(20).getId(), sampleProducts.get(20).getBarcode(), sampleProducts.get(20).getName(), sampleProducts.get(20).getCategory().getDisplayName(), 2, 1.80));
        billRepository.save(bill2);

        Bill bill3 = new Bill();
        bill3.setBillNumber("SB-20261029-1003");
        bill3.setDateTime(LocalDateTime.now().minusHours(4));
        bill3.setCustomerName("Walk-in Shopper");
        bill3.setCustomerPhone("9123456789");
        bill3.setPaymentMethod("CASH");
        bill3.setSubtotal(16.55);
        bill3.setTaxRate(5.0);
        bill3.setTaxAmount(0.83);
        bill3.setDiscountAmount(0.00);
        bill3.setCouponCode(null);
        bill3.setGrandTotal(17.38);
        bill3.addItem(new BillItem(sampleProducts.get(1).getId(), sampleProducts.get(1).getBarcode(), sampleProducts.get(1).getName(), sampleProducts.get(1).getCategory().getDisplayName(), 1, 8.20));
        bill3.addItem(new BillItem(sampleProducts.get(7).getId(), sampleProducts.get(7).getBarcode(), sampleProducts.get(7).getName(), sampleProducts.get(7).getCategory().getDisplayName(), 1, 2.10));
        bill3.addItem(new BillItem(sampleProducts.get(12).getId(), sampleProducts.get(12).getBarcode(), sampleProducts.get(12).getName(), sampleProducts.get(12).getCategory().getDisplayName(), 2, 1.95));
        billRepository.save(bill3);
    }
}

