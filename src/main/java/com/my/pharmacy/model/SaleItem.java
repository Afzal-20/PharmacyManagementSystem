package com.my.pharmacy.model;

import com.my.pharmacy.util.CalculationEngine;

public class SaleItem {
    private int id, saleId, productId, batchId, quantity, bonusQty;
    private double unitPrice, discountPercent, discountAmount, subTotal;
    private String productName;

    public SaleItem(int productId, int batchId, int quantity, double unitPrice, int bonusQty, double discountPercent) {
        this.productId = productId;
        this.batchId = batchId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.bonusQty = bonusQty;
        this.discountPercent = discountPercent;
        recalculate();
    }

    private void recalculate() {
        double grossTotal = CalculationEngine.calculateGrossTotal(this.unitPrice, this.quantity);
        this.discountAmount = CalculationEngine.calculateDiscountAmount(grossTotal, this.discountPercent);
        this.subTotal = CalculationEngine.calculateNetTotal(grossTotal, this.discountAmount);
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        recalculate();
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
        recalculate();
    }

    public double getDiscountAmount() { return discountAmount; }
    public double getSubTotal() { return subTotal; }

    // --- Essential Getters & Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }
    public int getProductId() { return productId; }
    public int getBatchId() { return batchId; }
    public int getQuantity() { return quantity; }
    public int getBonusQty() { return bonusQty; }
    public double getUnitPrice() { return unitPrice; }
    public double getDiscountPercent() { return discountPercent; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
}