package com.my.pharmacy.model;

public class Dealer {
    private int id;
    private String name;
    private String companyName;
    private String phone;
    private String address;
    private String licenseNo;

    public Dealer(int id, String name, String companyName, String phone, String address, String licenseNo) {
        this.id = id;
        this.name = name;
        this.companyName = companyName;
        this.phone = phone;
        this.address = address;
        this.licenseNo = licenseNo;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCompanyName() { return companyName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getLicenseNo() { return licenseNo; }

    @Override
    public String toString() { return name; }
}