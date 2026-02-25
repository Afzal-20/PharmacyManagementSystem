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

    // Professional Accounting Fields
    private double companyDiscount;
    private double salesTax;

    private Product product;

    public Batch(int id, int productId, String batchNo, String expiryDate, int qtyOnHand,
                 double costPrice, double tradePrice, double retailPrice,
                 double discountPercent, double companyDiscount, double salesTax) {
        this.id = id;
        this.productId = productId;
        this.batchNo = batchNo;
        this.expiryDate = expiryDate;
        this.qtyOnHand = qtyOnHand;
        this.costPrice = costPrice;
        this.tradePrice = tradePrice;
        this.retailPrice = retailPrice;
        this.discountPercent = discountPercent;
        this.companyDiscount = companyDiscount;
        this.salesTax = salesTax;
    }

    // --- BOX-CENTRIC UI HELPERS ---
    public int getBoxCount() {
        return (product != null && product.getPackSize() > 0) ? qtyOnHand / product.getPackSize() : 0;
    }

    public double getBoxTradePrice() {
        return tradePrice * (product != null ? product.getPackSize() : 1);
    }

    public double getBoxRetailPrice() {
        return retailPrice * (product != null ? product.getPackSize() : 1);
    }

    // --- GETTERS ---
    public int getId() { return id; }
    public int getBatchId() { return id; }
    public int getProductId() { return productId; }
    public String getBatchNo() { return batchNo; }
    public String getExpiryDate() { return expiryDate; }
    public int getQtyOnHand() { return qtyOnHand; }
    public double getCostPrice() { return costPrice; }
    public double getTradePrice() { return tradePrice; }
    public double getRetailPrice() { return retailPrice; }
    public double getDiscountPercent() { return discountPercent; }
    public double getCompanyDiscount() { return companyDiscount; }
    public double getSalesTax() { return salesTax; }
    public Product getProduct() { return product; }

    // --- SETTERS (Required for Duplicate Merge Logic) ---
    public void setId(int id) { this.id = id; }
    public void setQtyOnHand(int qtyOnHand) { this.qtyOnHand = qtyOnHand; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }
    public void setTradePrice(double tradePrice) { this.tradePrice = tradePrice; }
    public void setRetailPrice(double retailPrice) { this.retailPrice = retailPrice; }
    public void setCompanyDiscount(double companyDiscount) { this.companyDiscount = companyDiscount; }
    public void setSalesTax(double salesTax) { this.salesTax = salesTax; }
    public void setProduct(Product product) { this.product = product; }
}