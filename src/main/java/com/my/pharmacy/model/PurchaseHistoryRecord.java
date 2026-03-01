package com.my.pharmacy.model;

import java.sql.Timestamp;

public class PurchaseHistoryRecord {
    private Timestamp purchaseDate;
    private String dealerName;
    private String invoiceNo;
    private int initialBoxes;
    private double costPrice;
    private double tradePrice;

    public PurchaseHistoryRecord(Timestamp purchaseDate, String dealerName, String invoiceNo, int initialBoxes, double costPrice, double tradePrice) {
        this.purchaseDate = purchaseDate;
        this.dealerName = dealerName;
        this.invoiceNo = invoiceNo;
        this.initialBoxes = initialBoxes;
        this.costPrice = costPrice;
        this.tradePrice = tradePrice;
    }

    public Timestamp getPurchaseDate() { return purchaseDate; }
    public String getDealerName() { return dealerName; }
    public String getInvoiceNo() { return invoiceNo; }
    public int getInitialBoxes() { return initialBoxes; }
    public double getCostPrice() { return costPrice; }
    public double getTradePrice() { return tradePrice; }
}