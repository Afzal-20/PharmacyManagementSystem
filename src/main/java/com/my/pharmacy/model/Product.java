package com.my.pharmacy.model;

public class Product {
    private int id;
    private String name;
    private String genericName;   // NEW: e.g. Paracetamol
    private String manufacturer;  // NEW: e.g. GSK
    private String description;
    private int packSize;         // NEW: e.g. 10 (1 Box = 10 Strips)
    private int minStockLevel;
    private String shelfLocation;

    public Product(int id, String name, String genericName, String manufacturer,
                   String description, int packSize, int minStockLevel, String shelfLocation) {
        this.id = id;
        this.name = name;
        this.genericName = genericName;
        this.manufacturer = manufacturer;
        this.description = description;
        this.packSize = packSize;
        this.minStockLevel = minStockLevel;
        this.shelfLocation = shelfLocation;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPackSize() { return packSize; }
    public void setPackSize(int packSize) { this.packSize = packSize; }

    public int getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(int minStockLevel) { this.minStockLevel = minStockLevel; }

    public String getShelfLocation() { return shelfLocation; }
    public void setShelfLocation(String shelfLocation) { this.shelfLocation = shelfLocation; }

    @Override
    public String toString() {
        return name + " (" + manufacturer + ")";
    }
}