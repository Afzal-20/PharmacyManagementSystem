package com.my.pharmacy.dao;

import com.my.pharmacy.model.Product;
import java.util.List;

public interface ProductDAO {
    // 1. Create
    int addProduct(Product product);

    // 2. Read
    List<Product> getAllProducts();
    Product getProductById(int id); // Useful for scanning barcodes later

    // 3. Update (For fixing typos or changing pack size)
    void updateProduct(Product product);

    // 4. Delete (Removes product if no sales exist)
    void deleteProduct(int id);
}