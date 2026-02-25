package com.my.pharmacy.model;

import java.sql.Timestamp;

public class SaleLedgerRecord {
    private int invoiceNo;
    private Timestamp saleDate;
    private int quantity;
    private double rate;
    private double total;

    public SaleLedgerRecord(int invoiceNo, Timestamp saleDate, int quantity, double rate, double total) {
        this.invoiceNo = invoiceNo;
        this.saleDate = saleDate;
        this.quantity = quantity;
        this.rate = rate;
        this.total = total;
    }

    public int getInvoiceNo() { return invoiceNo; }
    public Timestamp getSaleDate() { return saleDate; }
    public int getQuantity() { return quantity; }
    public double getRate() { return rate; }
    public double getTotal() { return total; }
}