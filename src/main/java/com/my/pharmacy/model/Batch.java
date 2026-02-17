package com.my.pharmacy.model;

import com.my.pharmacy.util.FormatterUtil;

public class Batch {
    private int batchId;
    private int productId;
    private String batchNo;
    private String expiryDate;
    private int qtyOnHand;      // Always in smallest unit (e.g. Tablets)

    // The "Triple Pricing" Architecture
    private double costPrice;   // Your Purchase Price
    private double tradePrice;  // Wholesale Price (TP)
    private double retailPrice; // Consumer Price (RP)
    private double taxPercent;

    private Product product; // Link to parent product

    public Batch(int batchId, int productId, String batchNo, String expiryDate,
                 int qtyOnHand, double costPrice, double tradePrice, double retailPrice, double taxPercent) {
        this.batchId = batchId;
        this.productId = productId;
        this.batchNo = batchNo;
        this.expiryDate = expiryDate;
        this.qtyOnHand = qtyOnHand;
        this.costPrice = costPrice;
        this.tradePrice = tradePrice;
        this.retailPrice = retailPrice;
        this.taxPercent = taxPercent;
    }

    // Helper: Calculate Margin %
    public double getMarginPercentage() {
        if (tradePrice == 0) return 0;
        return ((retailPrice - tradePrice) / tradePrice) * 100;
    }

    // Getters and Setters
    public int getBatchId() { return batchId; }
    public void setBatchId(int batchId) { this.batchId = batchId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public int getQtyOnHand() { return qtyOnHand; }
    public void setQtyOnHand(int qtyOnHand) { this.qtyOnHand = qtyOnHand; }

    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }

    public double getTradePrice() { return tradePrice; }
    public void setTradePrice(double tradePrice) { this.tradePrice = tradePrice; }

    public double getRetailPrice() { return retailPrice; }
    public void setRetailPrice(double retailPrice) { this.retailPrice = retailPrice; }

    public double getTaxPercent() { return taxPercent; }
    public void setTaxPercent(double taxPercent) { this.taxPercent = taxPercent; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    // For UI Display (Table Columns)
    public String getFormattedRetailPrice() {
        return FormatterUtil.formatPrice(retailPrice);
    }

    public String getFormattedTradePrice() {
        return FormatterUtil.formatPrice(tradePrice);
    }
}