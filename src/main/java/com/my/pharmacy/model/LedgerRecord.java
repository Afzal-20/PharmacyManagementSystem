package com.my.pharmacy.model;

public class LedgerRecord {
    private String date;
    private String description;
    private double debit;  // Amount added to Khata (e.g. Sales)
    private double credit; // Amount paid off (e.g. Cash Receipts)

    public LedgerRecord(String date, String description, double debit, double credit) {
        this.date = date;
        this.description = description;
        this.debit = debit;
        this.credit = credit;
    }

    public String getDate() { return date; }
    public String getDescription() { return description; }
    public double getDebit() { return debit; }
    public double getCredit() { return credit; }
}