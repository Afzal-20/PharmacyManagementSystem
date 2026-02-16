package com.my.pharmacy.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Batch {
    // We use JavaFX Properties (Smart Variables) instead of int/double
    private final IntegerProperty batchId;
    private final IntegerProperty productId;
    private final StringProperty batchNo;
    private final ObjectProperty<LocalDate> expiryDate;
    private final DoubleProperty purchasePrice;
    private final DoubleProperty salePrice;
    private final IntegerProperty qtyOnHand;

    // Constructor
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

    // --- 1. Batch ID ---
    public int getBatchId() { return batchId.get(); }
    public void setBatchId(int value) { batchId.set(value); }
    public IntegerProperty batchIdProperty() { return batchId; }

    // --- 2. Product ID (This was missing!) ---
    public int getProductId() { return productId.get(); }
    public void setProductId(int value) { productId.set(value); }
    public IntegerProperty productIdProperty() { return productId; }

    // --- 3. Batch Number ---
    public String getBatchNo() { return batchNo.get(); }
    public void setBatchNo(String value) { batchNo.set(value); }
    public StringProperty batchNoProperty() { return batchNo; }

    // --- 4. Expiry Date ---
    public LocalDate getExpiryDate() { return expiryDate.get(); }
    public void setExpiryDate(LocalDate value) { expiryDate.set(value); }
    public ObjectProperty<LocalDate> expiryDateProperty() { return expiryDate; }

    // --- 5. Purchase Price (This was missing!) ---
    public double getPurchasePrice() { return purchasePrice.get(); }
    public void setPurchasePrice(double value) { purchasePrice.set(value); }
    public DoubleProperty purchasePriceProperty() { return purchasePrice; }

    // --- 6. Sale Price (This was missing!) ---
    public double getSalePrice() { return salePrice.get(); }
    public void setSalePrice(double value) { salePrice.set(value); }
    public DoubleProperty salePriceProperty() { return salePrice; }

    // --- 7. Quantity on Hand ---
    public int getQtyOnHand() { return qtyOnHand.get(); }
    public void setQtyOnHand(int value) { qtyOnHand.set(value); }
    public IntegerProperty qtyOnHandProperty() { return qtyOnHand; }

    // --- Business Logic: Check Expiration ---
    public boolean isExpired() {
        if (expiryDate.get() == null) return false;
        return expiryDate.get().isBefore(LocalDate.now());
    }

    // --- Helper for Console Output ---
    @Override
    public String toString() {
        return batchNo.get(); // Useful for debugging or ComboBoxes
    }
}