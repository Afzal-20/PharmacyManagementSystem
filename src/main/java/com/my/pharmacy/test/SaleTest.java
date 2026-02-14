package com.my.pharmacy.test;

import com.my.pharmacy.model.Sale;
import com.my.pharmacy.model.SaleItem;

public class SaleTest {
    public static void main(String[] args) {
        System.out.println("--- TESTING SALE (INVOICE) MODEL ---");

        // 1. Create a blank Sale
        Sale sale = new Sale();
        System.out.println("New Invoice: " + sale.getInvoiceNumber());
        System.out.println("Initial Total: " + sale.getTotalAmount()); // Should be 0.0

        // 2. Add Items to the Cart
        System.out.println("\n[ACTION] Adding Items...");

        // Item 1: Panadol ($50)
        SaleItem item1 = new SaleItem(0, 0, 0, 1, 50.0, "Panadol", "B1");
        sale.getItems().add(item1);

        // Item 2: Syrup ($120)
        SaleItem item2 = new SaleItem(0, 0, 0, 1, 120.0, "Cough Syrup", "B2");
        sale.getItems().add(item2);

        // 3. Recalculate
        sale.recalculateGrandTotal();
        System.out.println("Grand Total: " + sale.getTotalAmount());

        if (sale.getTotalAmount() == 170.0) {
            System.out.println("✅ Grand Total Calculation Correct (50 + 120 = 170)");
        } else {
            System.out.println("❌ Calculation Failed!");
        }

        // 4. Test Date Formatting
        System.out.println("Formatted Date: " + sale.getFormattedDate());

        System.out.println("--- TEST COMPLETE ---");
    }
}