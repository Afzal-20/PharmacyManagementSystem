package com.my.pharmacy.test;

import com.my.pharmacy.model.Batch;
import java.time.LocalDate;

public class BatchTest {
    public static void main(String[] args) {
        System.out.println("--- TESTING BATCH MODEL ---");

        // 1. Create an expired batch (Yesterday)
        Batch expiredBatch = new Batch(1, 101, "OLD-01", LocalDate.now().minusDays(1), 50.0, 70.0, 10);

        // 2. Create a fresh batch (Next year)
        Batch freshBatch = new Batch(2, 101, "NEW-99", LocalDate.now().plusYears(1), 50.0, 70.0, 100);

        // 3. Test Logic
        System.out.println("Batch OLD-01 is expired? " + expiredBatch.isExpired()); // Should be true
        System.out.println("Batch NEW-99 is expired? " + freshBatch.isExpired()); // Should be false

        // 4. Test Reactive Quantity
        freshBatch.qtyOnHandProperty().addListener((obs, oldQ, newQ) -> {
            System.out.println("   [STOCK ALERT] Quantity changed from " + oldQ + " to " + newQ);
        });

        System.out.println("Simulating a sale of 5 units...");
        freshBatch.setQtyOnHand(freshBatch.getQtyOnHand() - 5);

        System.out.println("--- TEST COMPLETE ---");
    }
}