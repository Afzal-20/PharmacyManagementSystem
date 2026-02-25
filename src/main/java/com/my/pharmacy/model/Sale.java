package com.my.pharmacy.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int id;
    private Timestamp saleDate;
    private double totalAmount;
    private String paymentMode;
    private int customerId;
    private int salesmanId;

    // Payment Tracking
    private double amountPaid;
    private double balanceDue;

    private List<SaleItem> items = new ArrayList<>();

    public Sale(int id, Timestamp saleDate, double totalAmount, String paymentMode,
                int customerId, int salesmanId, double amountPaid, double balanceDue) {
        this.id = id;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.paymentMode = paymentMode;
        this.customerId = customerId;
        this.salesmanId = salesmanId;
        this.amountPaid = amountPaid;
        this.balanceDue = balanceDue;
    }

    public Sale() {
        this.saleDate = new Timestamp(System.currentTimeMillis());
        this.paymentMode = "CASH";
    }

    public void addItem(SaleItem item) {
        items.add(item);
        totalAmount += item.getSubTotal();
    }

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
    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }
    public double getBalanceDue() { return balanceDue; }
    public void setBalanceDue(double balanceDue) { this.balanceDue = balanceDue; }
    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }
}