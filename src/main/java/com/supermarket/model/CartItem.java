package com.supermarket.model;

public class CartItem {
    private Long productId;
    private String barcode;
    private String name;
    private String category;
    private double unitPrice;
    private int quantity;
    private double totalPrice;

    public CartItem() {}

    public CartItem(Long productId, String barcode, String name, String category, double unitPrice, int quantity) {
        this.productId = productId;
        this.barcode = barcode;
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = Math.round((quantity * unitPrice) * 100.0) / 100.0;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.totalPrice = Math.round((this.quantity * unitPrice) * 100.0) / 100.0;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = Math.round((quantity * this.unitPrice) * 100.0) / 100.0;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}

