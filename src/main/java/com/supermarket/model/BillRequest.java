package com.supermarket.model;

import java.util.ArrayList;
import java.util.List;

public class BillRequest {
    private String customerName;
    private String customerPhone;
    private String paymentMethod; // CASH, UPI, CARD
    private String couponCode;
    private double customDiscountPercentage;
    private List<CartItem> items = new ArrayList<>();

    public BillRequest() {}

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public double getCustomDiscountPercentage() {
        return customDiscountPercentage;
    }

    public void setCustomDiscountPercentage(double customDiscountPercentage) {
        this.customDiscountPercentage = customDiscountPercentage;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}

