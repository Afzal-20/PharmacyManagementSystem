package com.my.pharmacy.dao;

import com.my.pharmacy.model.Product;
import javafx.collections.ObservableList;

public interface ProductDAO {
    // CRUD Operations
    void addProduct(Product product);
    void updateProduct(Product product);
    void deleteProduct(int id);

    // Read Operations
    Product getProduct(int id);
    ObservableList<Product> getAllProducts();

    // Search Operation (The foundation for your Search Bar)
    ObservableList<Product> searchProducts(String query);
}
