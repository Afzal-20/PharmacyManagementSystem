package com.my.pharmacy.model;

public class SaleItem {
    private int id, saleId, productId, batchId, quantity, bonusQty;
    private double unitPrice, discountPercent, subTotal;
    private String productName;

    public SaleItem(int productId, int batchId, int quantity, double unitPrice, int bonusQty, double discountPercent) {
        this.productId = productId;
        this.batchId = batchId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.bonusQty = bonusQty;
        this.discountPercent = discountPercent;

        // Wholesale/Retail Math
        double rawTotal = unitPrice * quantity;
        this.subTotal = rawTotal - (rawTotal * (discountPercent / 100.0));
    }

    // --- Essential Getters for the DAO ---
    public int getProductId() { return productId; }
    public int getBatchId() { return batchId; }
    public int getQuantity() { return quantity; }
    public int getBonusQty() { return bonusQty; }
    public double getUnitPrice() { return unitPrice; }
    public double getDiscountPercent() { return discountPercent; }
    public double getSubTotal() { return subTotal; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
}