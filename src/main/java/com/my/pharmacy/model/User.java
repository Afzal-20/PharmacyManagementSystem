package com.my.pharmacy.model;

public class User {
    private int id;
    private String username;
    private String password;
    private String role; // 'ADMIN' or 'SALESMAN'
    private String fullName;
    private boolean isActive;

    public User(int id, String username, String password, String role, String fullName, boolean isActive) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.isActive = isActive;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
    public boolean isActive() { return isActive; }

    // Permission Helper
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }
}