package com.my.pharmacy.test;

import com.my.pharmacy.model.SaleItem;

public class SaleItemTest {
    public static void main(String[] args) {
        System.out.println("--- TESTING SALE ITEM MODEL ---");

        // 1. Create an Item: 2 packs of Panadol @ $50.00 each
        SaleItem item = new SaleItem(1, 101, 5, 2, 50.00, "Panadol", "B-100");

        // 2. Check Initial Math
        System.out.println("Item: " + item.getProductName());
        System.out.println("Qty: " + item.getQuantity() + " | Price: " + item.getUnitPriceSold());
        System.out.println("Initial Subtotal: " + item.getSubTotal()); // Should be 100.0

        if (item.getSubTotal() == 100.0) {
            System.out.println("✅ Initial calculation correct.");
        } else {
            System.out.println("❌ Initial calculation FAILED.");
        }

        // 3. Test Reactive Update
        System.out.println("\n[ACTION] Customer buys 3 more (Total 5)...");
        item.setQuantity(5);

        // The subtotal should now be 250.0 AUTOMATICALLY
        System.out.println("New Subtotal: " + item.getSubTotal());

        if (item.getSubTotal() == 250.0) {
            System.out.println("✅ Reactive Auto-Calculation SUCCESS!");
        } else {
            System.out.println("❌ Reactive update FAILED.");
        }

        System.out.println("--- TEST COMPLETE ---");
    }
}