package com.my.pharmacy.test;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.dao.ProductDAO;
import com.my.pharmacy.dao.ProductDAOImpl;
import com.my.pharmacy.model.Product;

public class ProductDAOTest {
    public static void main(String[] args) {
        System.out.println("--- TESTING PRODUCT DAO (ID GENERATION) ---");

        try {
            ProductDAO dao = new ProductDAOImpl();

            // 1. Create a Product Object (ID is 0 initially)
            Product newProduct = new Product(0, "Panadol Extra", "Paracetamol", "GSK", 1, 0.15, 10, 50);
            System.out.println("Before Save ID: " + newProduct.getId()); // Should be 0

            // 2. Save it to DB
            System.out.println("Saving product...");
            dao.addProduct(newProduct);

            // 3. CHECK: Did the ID update?
            System.out.println("After Save ID:  " + newProduct.getId());

            if (newProduct.getId() > 0) {
                System.out.println("✅ SUCCESS: ID was generated and assigned back to the object!");
            } else {
                System.out.println("❌ FAILURE: ID is still 0. logic for 'RETURN_GENERATED_KEYS' is missing.");
            }

        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}