package com.my.pharmacy.model;

import javafx.beans.property.*;
import javafx.beans.binding.Bindings;

public class SaleItem {

    private final IntegerProperty id;
    private final IntegerProperty saleId;
    private final IntegerProperty batchId;
    private final IntegerProperty quantity;
    private final DoubleProperty unitPriceSold; // Locks price at moment of sale
    private final DoubleProperty subTotal;      // Auto-calculated

    // UI Helpers (Not stored in 'sale_items' table, but needed for the Cart View)
    private final StringProperty productName;
    private final StringProperty batchNo;

    public SaleItem() {
        this(0, 0, 0, 0, 0.0, "Unknown", "N/A");
    }

    public SaleItem(int id, int saleId, int batchId, int quantity,
                    double unitPriceSold, String productName, String batchNo) {
        this.id = new SimpleIntegerProperty(id);
        this.saleId = new SimpleIntegerProperty(saleId);
        this.batchId = new SimpleIntegerProperty(batchId);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unitPriceSold = new SimpleDoubleProperty(unitPriceSold);

        this.productName = new SimpleStringProperty(productName);
        this.batchNo = new SimpleStringProperty(batchNo);

        // --- THE REACTIVE MATH MAGIC ---
        // This binds the subTotal to (Quantity * Price).
        // If you change the quantity in the UI, this updates automatically.
        this.subTotal = new SimpleDoubleProperty();
        this.subTotal.bind(this.quantity.multiply(this.unitPriceSold));
    }

    // --- Getters & Property Accessors ---

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public int getSaleId() { return saleId.get(); }
    public void setSaleId(int value) { saleId.set(value); }
    public IntegerProperty saleIdProperty() { return saleId; }

    public int getBatchId() { return batchId.get(); }
    public IntegerProperty batchIdProperty() { return batchId; }

    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int value) { quantity.set(value); }
    public IntegerProperty quantityProperty() { return quantity; }

    public double getUnitPriceSold() { return unitPriceSold.get(); }
    public DoubleProperty unitPriceSoldProperty() { return unitPriceSold; }

    // Read-Only because it's calculated
    public double getSubTotal() { return subTotal.get(); }
    public DoubleProperty subTotalProperty() { return subTotal; }

    // UI Helpers
    public String getProductName() { return productName.get(); }
    public StringProperty productNameProperty() { return productName; }

    public String getBatchNo() { return batchNo.get(); }
    public StringProperty batchNoProperty() { return batchNo; }

}
