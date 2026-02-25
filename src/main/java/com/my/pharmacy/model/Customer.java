package com.my.pharmacy.model;

public class Customer {
    private int id;
    private String name;
    private String phone;
    private String address;
    private String type;
    private double currentBalance;
    private String areaCode;
    private String areaName;
    private String cnic; // New Field

    public Customer(int id, String name, String phone, String address, String type,
                    double currentBalance, String areaCode, String areaName, String cnic) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.type = type;
        this.currentBalance = currentBalance;
        this.areaCode = areaCode;
        this.areaName = areaName;
        this.cnic = cnic;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getType() { return type; }
    public double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(double currentBalance) { this.currentBalance = currentBalance; }
    public String getAreaCode() { return areaCode; }
    public String getAreaName() { return areaName; }
    public String getCnic() { return cnic; }
    public void setCnic(String cnic) { this.cnic = cnic; }

    @Override
    public String toString() { return name; }
}