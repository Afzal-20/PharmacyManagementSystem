package com.my.pharmacy.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int id;
    private Timestamp saleDate;
    private double totalAmount;

    // NEW Wholesale Fields
    private String paymentMode;  // 'CASH', 'CREDIT', 'CHECK'
    private int customerId;      // Link to Customer Ledger
    private int salesmanId;      // Link to User (Commission)

    private List<SaleItem> items = new ArrayList<>();

    // Constructor for reading from Database (DAO uses this)
    public Sale(int id, Timestamp saleDate, double totalAmount, String paymentMode, int customerId, int salesmanId) {
        this.id = id;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.paymentMode = paymentMode;
        this.customerId = customerId;
        this.salesmanId = salesmanId;
    }

    // Default Constructor for creating a NEW sale in the POS
    public Sale() {
        this.saleDate = new Timestamp(System.currentTimeMillis());
        this.paymentMode = "CASH"; // Default
    }

    public void addItem(SaleItem item) {
        items.add(item);
        totalAmount += item.getSubTotal();
    }

    // --- Getters and Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Timestamp getSaleDate() { return saleDate; }
    public void setSaleDate(Timestamp saleDate) { this.saleDate = saleDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getSalesmanId() { return salesmanId; }
    public void setSalesmanId(int salesmanId) { this.salesmanId = salesmanId; }

    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }
}