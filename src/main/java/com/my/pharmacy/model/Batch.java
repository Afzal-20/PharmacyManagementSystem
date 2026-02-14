package com.my.pharmacy.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Batch {
    private final IntegerProperty batchId;
    private final IntegerProperty productId;
    private final StringProperty batchNo;
    private final ObjectProperty<LocalDate> expiryDate;
    private final DoubleProperty purchasePrice;
    private final DoubleProperty salePrice;
    private final IntegerProperty qtyOnHand;

    public Batch(){
        this(0, 0, "", LocalDate.now(), 0.0, 0.0, 0);
    }

    public Batch(int batchId, int productId, String batchNo, LocalDate expiryDate,
                 double purchasePrice, double salePrice, int qtyOnHand) {
        this.batchId = new SimpleIntegerProperty(batchId);
        this.productId = new SimpleIntegerProperty(productId);
        this.batchNo = new SimpleStringProperty(batchNo);
        this.expiryDate = new SimpleObjectProperty<>(expiryDate);
        this.purchasePrice = new SimpleDoubleProperty(purchasePrice);
        this.salePrice = new SimpleDoubleProperty(salePrice);
        this.qtyOnHand = new SimpleIntegerProperty(qtyOnHand);
    }

    // Getters, Setters, and Property methods
    public int getBatchId() { return batchId.get(); }
    public IntegerProperty batchIdProperty() { return batchId; }

    public String getBatchNo() { return batchNo.get(); }
    public void setBatchNo(String value) { batchNo.set(value); }
    public StringProperty batchNoProperty() { return batchNo; }

    public LocalDate getExpiryDate() { return expiryDate.get(); }
    public void setExpiryDate(LocalDate value) { expiryDate.set(value); }
    public ObjectProperty<LocalDate> expiryDateProperty() { return expiryDate; }

    public int getQtyOnHand() { return qtyOnHand.get(); }
    public void setQtyOnHand(int value) { qtyOnHand.set(value); }
    public IntegerProperty qtyOnHandProperty() { return qtyOnHand; }

    // Logic Helper: Check if expired
    public boolean isExpired() {
        return expiryDate.get().isBefore(LocalDate.now());
    }

    /**
     * Search Helper: Combines batch details for searching.
     * Allows finding a batch by Number ("B102") or Expiry Year ("2026").
     */
    public String getSearchKeywords() {
        // We combine: Batch Number + Expiry Date
        return (getBatchNo() + " " +
                getExpiryDate().toString()).toLowerCase();
    }
}
