package com.my.pharmacy.model;

import javafx.beans.property.*;

public class Product {

    // 1. Use JavaFX Properties (Smart Variables)
    // These wrapper classes let the UI "listen" for changes automatically.
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty genericName;
    private final StringProperty manufacturer;
    private final IntegerProperty supplierId;
    private final DoubleProperty taxRate;
    private final IntegerProperty packSize;     // e.g., 10 tablets per strip
    private final IntegerProperty minStockAlert; // e.g., Warn if below 50

    // 2. Constructor (Empty) - Required for some Database tools
    public Product() {
        this(0, "", "", "", 0, 0.0, 1, 10);
    }

    // 3. Constructor (Full) - Used when loading from Database
    public Product(int id, String name, String genericName, String manufacturer,
                   int supplierId, double taxRate, int packSize, int minStockAlert) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.genericName = new SimpleStringProperty(genericName);
        this.manufacturer = new SimpleStringProperty(manufacturer);
        this.supplierId = new SimpleIntegerProperty(supplierId);
        this.taxRate = new SimpleDoubleProperty(taxRate);
        this.packSize = new SimpleIntegerProperty(packSize);
        this.minStockAlert = new SimpleIntegerProperty(minStockAlert);
    }

    // 4. Getters & Setters (The JavaFX Pattern)
    // pattern: getX(), setX(), xProperty()

    // ID
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    // Name
    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    // Generic Name
    public String getGenericName() { return genericName.get(); }
    public void setGenericName(String value) { genericName.set(value); }
    public StringProperty genericNameProperty() { return genericName; }

    // Manufacturer
    public String getManufacturer() { return manufacturer.get(); }
    public void setManufacturer(String value) { manufacturer.set(value); }
    public StringProperty manufacturerProperty() { return manufacturer; }

    // Supplier ID
    public int getSupplierId() { return supplierId.get(); }
    public void setSupplierId(int value) { supplierId.set(value); }
    public IntegerProperty supplierIdProperty() { return supplierId; }

    // Tax Rate
    public double getTaxRate() { return taxRate.get(); }
    public void setTaxRate(double value) { taxRate.set(value); }
    public DoubleProperty taxRateProperty() { return taxRate; }

    // Pack Size
    public int getPackSize() { return packSize.get(); }
    public void setPackSize(int value) { packSize.set(value); }
    public IntegerProperty packSizeProperty() { return packSize; }

    // Min Stock Alert
    public int getMinStockAlert() { return minStockAlert.get(); }
    public void setMinStockAlert(int value) { minStockAlert.set(value); }
    public IntegerProperty minStockAlertProperty() { return minStockAlert; }

    @Override
    public String toString() {
        return name.get(); // Useful for ComboBoxes (Drop-downs)
    }
}