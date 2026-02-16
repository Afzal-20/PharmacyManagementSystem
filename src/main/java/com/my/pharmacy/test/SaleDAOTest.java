package com.my.pharmacy.test;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.Batch;
import com.my.pharmacy.model.Product;
import com.my.pharmacy.model.Sale;
import com.my.pharmacy.model.SaleItem;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class SaleDAOTest {
    public static void main(String[] args) {
        System.out.println("--- TESTING SALE TRANSACTION & STOCK DEDUCTION ---");

        try {
            // 1. Initialize DAOs
            ProductDAO productDAO = new ProductDAOImpl();
            BatchDAO batchDAO = new BatchDAOImpl();
            SaleDAO saleDAO = new SaleDAOImpl();

            // 2. Setup: Create Product & Batch
            System.out.println("\n[SETUP] Creating Product & Stock...");

            // Create Product
            Product syrup = new Product(0, "Cough Syrup", "Generic", "PharmaCo", 1, 0.0, 1, 5);
            productDAO.addProduct(syrup);
            int prodId = syrup.getId();
            System.out.println("   -> Product Created. ID: " + prodId);

            // Create Batch with 10 Items
            // Batch(id, productId, batchNo, expiry, purchasePrice, salePrice, quantity)
            Batch batch = new Batch(0, prodId, "B-500", LocalDate.now().plusYears(1), 5.0, 10.0, 10);
            batchDAO.addBatch(batch);

            // Retrieve the batch to get its generated ID
            int batchId = batchDAO.getBatchesByProductId(prodId).get(0).getBatchId();
            System.out.println("   -> Batch Created. ID: " + batchId + " | Initial Stock: 10");

            // 3. Create the Sale
            System.out.println("\n[ACTION] Selling 2 Bottles...");
            Sale sale = new Sale();
            sale.setTimestamp(LocalDateTime.now());

            // FIX: Updated Constructor -> SaleItem(id, saleId, PRODUCT_ID, batchId, quantity, unitPrice, name, batchNo)
            SaleItem item = new SaleItem(0, 0, prodId, batchId, 2, 10.0, "Cough Syrup", "B-500");
            sale.getItems().add(item);

            // 4. Save the Sale (This triggers the Transaction)
            saleDAO.saveSale(sale);

            // 5. VERIFY: Did Stock drop from 10 to 8?
            System.out.println("\n[VERIFICATION] Checking Stock Level...");
            Batch updatedBatch = batchDAO.getBatchesByProductId(prodId).get(0);

            System.out.println("   -> Old Stock: 10");
            System.out.println("   -> New Stock: " + updatedBatch.getQtyOnHand());

            if (updatedBatch.getQtyOnHand() == 8) {
                System.out.println("\n✅ SUCCESS: Sale saved and Stock deducted correctly!");
            } else {
                System.out.println("\n❌ FAILURE: Stock was not updated.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}