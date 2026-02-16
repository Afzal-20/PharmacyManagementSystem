package com.my.pharmacy.test;

import com.my.pharmacy.model.Sale;
import com.my.pharmacy.model.SaleItem;

public class SaleReactiveTest {
    public static void main(String[] args) {
        System.out.println("--- TESTING REACTIVE GRAND TOTAL ---");

        Sale sale = new Sale();
        System.out.println("Initial Total: " + sale.getTotalAmount()); // 0.0

        // 1. Add an Item (2 packs @ $50 each)
        System.out.println("\n[ACTION] Adding 2 packs of Panadol ($50 each)...");
// SaleItem(id, saleId, PRODUCT_ID, batchId, qty, price, name, batchNo)
        SaleItem item1 = new SaleItem(1, 1, 1, 1, 2, 50.0, "Panadol", "B1");        sale.getItems().add(item1);

        // Check Total (Should be 100.0)
        System.out.println("Total: " + sale.getTotalAmount());

        // 2. Modify Quantity directly in the Item
        System.out.println("\n[ACTION] Changing quantity to 5...");
        item1.setQuantity(5); // 5 * 50 = 250.0

        // 3. Check Grand Total (Should update AUTOMATICALLY)
        System.out.println("New Grand Total: " + sale.getTotalAmount());

        if (sale.getTotalAmount() == 250.0) {
            System.out.println("✅ SUCCESS: Grand Total is fully reactive!");
        } else {
            System.out.println("❌ FAILURE: Grand Total did not update automatically.");
        }
    }
}