package com.my.pharmacy.model;

import java.sql.Timestamp;

public class Payment {
    private int id, entityId;
    private String entityType, paymentMode, description;
    private double amount;
    private Timestamp paymentDate;

    public Payment(int id, int entityId, String entityType, double amount, String paymentMode, String description, Timestamp paymentDate) {
        this.id = id;
        this.entityId = entityId;
        this.entityType = entityType;
        this.amount = amount;
        this.paymentMode = paymentMode;
        this.description = description;
        this.paymentDate = paymentDate;
    }

    public int getEntityId() { return entityId; }
    public String getEntityType() { return entityType; }
    public double getAmount() { return amount; }
    public String getPaymentMode() { return paymentMode; }
    public String getDescription() { return description; }
    public Timestamp getPaymentDate() { return paymentDate; }
}