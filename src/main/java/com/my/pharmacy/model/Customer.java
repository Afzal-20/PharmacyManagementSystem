package com.my.pharmacy.model;

public class Customer {
    private int id;
    private String name;
    private String phone;
    private String address;
    private String type; // 'RETAIL' or 'WHOLESALE'
    private double creditLimit;
    private double currentBalance;

    // Constructor for creating new customers
    public Customer(String name, String phone, String address, String type) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.type = type;
        this.creditLimit = 0.0;
        this.currentBalance = 0.0;
    }

    // Constructor for Database retrieval
    public Customer(int id, String name, String phone, String address, String type, double creditLimit, double currentBalance) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.type = type;
        this.creditLimit = creditLimit;
        this.currentBalance = currentBalance;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getCreditLimit() { return creditLimit; }
    public void setCreditLimit(double creditLimit) { this.creditLimit = creditLimit; }
    public double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(double currentBalance) { this.currentBalance = currentBalance; }
}