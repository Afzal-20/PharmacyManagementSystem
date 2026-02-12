package com.my.pharmacy.test;

import com.my.pharmacy.model.Product;

public class ProductTest {
    public static void main(String[] args) {
        System.out.println("--- TESTING PRODUCT MODEL ---");

        // 1. Create a dummy product
        Product p = new Product(101, "Panadol", "Paracetamol", "GSK", 1, 0.15, 10, 50);

        // 2. Verify Data Integrity
        System.out.println("Checking Name: " + p.getName());
        System.out.println("Checking Tax:  " + (p.getTaxRate() * 100) + "%");

        // 3. Test Reactive Properties (The "Glitch-Free" Feature)
        // We add a listener to see if the system detects changes automatically
        p.nameProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("   [EVENT] Name changed from '" + oldVal + "' to '" + newVal + "'");
        });

        // 4. Simulate an Edit
        System.out.println("Changing name to 'Panadol Extra'...");
        p.setName("Panadol Extra"); // This should trigger the event above

        System.out.println("--- TEST COMPLETE ---");
    }
}