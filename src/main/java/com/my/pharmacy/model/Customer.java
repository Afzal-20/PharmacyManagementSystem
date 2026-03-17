package com.my.pharmacy.model;

public class Customer {
    private int id;
    private String name;
    private String phone;
    private String address;
    private String type;
    private String cnic;

    public Customer(int id, String name, String phone, String address, String type, String cnic) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.type = type;
        this.cnic = cnic;
    }

    public int getId()           { return id; }
    public void setId(int id)    { this.id = id; }
    public String getName()      { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone()     { return phone; }
    public String getAddress()   { return address; }
    public String getType()      { return type; }
    public String getCnic()      { return cnic; }
    public void setCnic(String cnic) { this.cnic = cnic; }

    @Override
    public String toString() { return name; }
}