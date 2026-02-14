package com.my.pharmacy.test;

import com.my.pharmacy.dao.ProductDAO;
import com.my.pharmacy.dao.ProductDAOImpl;
import com.my.pharmacy.model.Product;
import javafx.collections.ObservableList;

public class ProductDAOTest {
    public static void main(String[] args) {
        System.out.println("--- TESTING PRODUCT DAO (DATABASE) ---");

        ProductDAO dao = new ProductDAOImpl();

        // 1. Create a Product Object
        Product newProduct = new Product(0, "Amoxil", "Amoxicillin", "GSK", 1, 0.10, 12, 20);

        // 2. Save it to DB
        System.out.println("Saving product...");
        dao.addProduct(newProduct);

        // 3. Read it back
        System.out.println("Reading database...");
        ObservableList<Product> products = dao.getAllProducts();

        boolean found = false;
        for (Product p : products) {
            System.out.println("ID: " + p.getId() + " | Name: " + p.getName());
            if (p.getName().equals("Amoxil")) found = true;
        }

        if (found) {
            System.out.println("✅ SUCCESS: Data persisted to SQLite!");
        } else {
            System.out.println("❌ FAILURE: Product not found in DB.");
        }
    }
}