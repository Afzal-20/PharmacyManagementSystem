package com.my.pharmacy.model;

public class Batch {
    private int id;
    private int productId;
    private String batchNo;
    private String expiryDate;
    private int qtyOnHand;
    private double costPrice;
    private double tradePrice;
    private double retailPrice;
    private double discountPercent;
    private Product product;

    public Batch(int id, int productId, String batchNo, String expiryDate, int qtyOnHand,
                 double costPrice, double tradePrice, double retailPrice, double discountPercent) {
        this.id = id;
        this.productId = productId;
        this.batchNo = batchNo;
        this.expiryDate = expiryDate;
        this.qtyOnHand = qtyOnHand;
        this.costPrice = costPrice;
        this.tradePrice = tradePrice;
        this.retailPrice = retailPrice;
        this.discountPercent = discountPercent;
    }

    // --- ADD THIS METHOD TO FIX THE ERROR ---
    public double getDiscountPercent() {
        return discountPercent;
    }

    public int getBatchId() {
        return this.id;
    }

    // Standard Getters & Setters
    public int getId() { return id; }
    public int getProductId() { return productId; }
    public String getBatchNo() { return batchNo; }
    public String getExpiryDate() { return expiryDate; }
    public int getQtyOnHand() { return qtyOnHand; }
    public double getCostPrice() { return costPrice; }
    public double getTradePrice() { return tradePrice; }
    public double getRetailPrice() { return retailPrice; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}