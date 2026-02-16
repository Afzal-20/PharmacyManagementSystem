package com.my.pharmacy.test;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.dao.BatchDAO;
import com.my.pharmacy.dao.BatchDAOImpl;
import com.my.pharmacy.dao.ProductDAO;
import com.my.pharmacy.dao.ProductDAOImpl;
import com.my.pharmacy.model.Batch;
import com.my.pharmacy.model.Product;
import javafx.collections.ObservableList;
import java.time.LocalDate;

public class BatchDAOTest {
    public static void main(String[] args) {
        System.out.println("--- TESTING INVENTORY (BATCHES) ---");

        try {
            ProductDAO productDAO = new ProductDAOImpl();
            BatchDAO batchDAO = new BatchDAOImpl();

            // 1. Create & Save a Product first (Foreign Key Requirement)
            System.out.println("[STEP 1] Creating Product...");
            Product panadol = new Product(0, "Panadol Extra", "Paracetamol", "GSK", 1, 0.0, 10, 50);
            productDAO.addProduct(panadol);

            int prodId = panadol.getId(); // This should now be > 0
            System.out.println("   -> Product Saved. Generated ID: " + prodId);

            if (prodId == 0) {
                System.err.println("❌ ERROR: Product ID is 0. Batch save will fail.");
                return;
            }

            // 2. Create a Batch for this Product
            // Batch(id, productId, batchNo, expiry, purchasePrice, salePrice, quantity)
            System.out.println("\n[STEP 2] Adding Stock (Batch #B-999)...");
            Batch newBatch = new Batch(
                    0,
                    prodId,
                    "B-999",
                    LocalDate.of(2028, 12, 31),
                    50.0,
                    60.0,
                    100
            );

            batchDAO.addBatch(newBatch);

            // 3. Read it back
            System.out.println("\n[STEP 3] Checking Inventory for Product ID " + prodId + "...");
            ObservableList<Batch> stock = batchDAO.getBatchesByProductId(prodId);

            boolean found = false;
            for (Batch b : stock) {
                System.out.println("   -> Found: " + b.getBatchNo() + " | Qty: " + b.getQtyOnHand() + " | Expires: " + b.getExpiryDate());
                if (b.getBatchNo().equals("B-999") && b.getQtyOnHand() == 100) {
                    found = true;
                }
            }

            if (found) {
                System.out.println("\n✅ SUCCESS: Inventory System is fully operational!");
            } else {
                System.out.println("\n❌ FAILURE: Batch was not saved.");
            }

        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}