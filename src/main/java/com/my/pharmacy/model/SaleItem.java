package com.my.pharmacy.model;

public class SaleItem {
    private int id;
    private int saleId;
    private int productId;
    private int batchId;
    private int quantity;
    private double unitPrice;
    private double subTotal;

    // NEW Wholesale Fields
    private int bonusQty;        // Free items given (Buy 10 , Get 1)
    private double discountPercent;

    private String productName;  // Helper for UI display

    // Main Constructor
    public SaleItem(int productId, int batchId, int quantity, double unitPrice, int bonusQty, double discountPercent) {
        this.productId = productId;
        this.batchId = batchId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.bonusQty = bonusQty;
        this.discountPercent = discountPercent;

        // Calculate Subtotal: (Price * Qty) - Discount
        double rawTotal = unitPrice * quantity;
        this.subTotal = rawTotal - (rawTotal * (discountPercent / 100.0));
    }

    // --- Getters and Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getBatchId() { return batchId; }
    public void setBatchId(int batchId) { this.batchId = batchId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getSubTotal() { return subTotal; }
    public void setSubTotal(double subTotal) { this.subTotal = subTotal; }

    public int getBonusQty() { return bonusQty; }
    public void setBonusQty(int bonusQty) { this.bonusQty = bonusQty; }

    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
}