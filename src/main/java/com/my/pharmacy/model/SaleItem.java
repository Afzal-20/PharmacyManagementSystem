package com.my.pharmacy.model;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;

public class SaleItem {
    private final IntegerProperty id;
    private final IntegerProperty saleId;
    private final IntegerProperty productId; // <--- NEW FIELD
    private final IntegerProperty batchId;
    private final IntegerProperty quantity;
    private final DoubleProperty unitPrice;
    private final DoubleProperty subTotal;
    private final StringProperty productName;
    private final StringProperty batchNo;

    // Updated Constructor with 'productId'
    public SaleItem(int id, int saleId, int productId, int batchId,
                    int quantity, double unitPrice, String productName, String batchNo) {
        this.id = new SimpleIntegerProperty(id);
        this.saleId = new SimpleIntegerProperty(saleId);
        this.productId = new SimpleIntegerProperty(productId); // <--- Initialize
        this.batchId = new SimpleIntegerProperty(batchId);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.productName = new SimpleStringProperty(productName);
        this.batchNo = new SimpleStringProperty(batchNo);

        // Reactive Subtotal: Quantity * UnitPrice
        this.subTotal = new SimpleDoubleProperty();
        this.subTotal.bind(this.quantity.multiply(this.unitPrice));
    }

    // --- Getters & Properties ---

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public int getSaleId() { return saleId.get(); }
    public void setSaleId(int value) { saleId.set(value); }
    public IntegerProperty saleIdProperty() { return saleId; }

    // --- NEW GETTERS FOR PRODUCT ID ---
    public int getProductId() { return productId.get(); }
    public void setProductId(int value) { productId.set(value); }
    public IntegerProperty productIdProperty() { return productId; }
    // ----------------------------------

    public int getBatchId() { return batchId.get(); }
    public void setBatchId(int value) { batchId.set(value); }
    public IntegerProperty batchIdProperty() { return batchId; }

    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int value) { quantity.set(value); }
    public IntegerProperty quantityProperty() { return quantity; }

    public double getUnitPrice() { return unitPrice.get(); }
    public void setUnitPrice(double value) { unitPrice.set(value); }
    public DoubleProperty unitPriceProperty() { return unitPrice; }

    public double getSubTotal() { return subTotal.get(); }
    public DoubleProperty subTotalProperty() { return subTotal; }

    public String getProductName() { return productName.get(); }
    public void setProductName(String value) { productName.set(value); }
    public StringProperty productNameProperty() { return productName; }

    public String getBatchNo() { return batchNo.get(); }
    public void setBatchNo(String value) { batchNo.set(value); }
    public StringProperty batchNoProperty() { return batchNo; }
}