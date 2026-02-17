package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.dao.ProductDAO;
import com.my.pharmacy.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {

    // --- 1. CREATE ---
    @Override
    public void addProduct(Product product) {
        String sql = "INSERT INTO products (name, generic_name, manufacturer, description, pack_size, min_stock_level, shelf_location) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getGenericName());
            pstmt.setString(3, product.getManufacturer());
            pstmt.setString(4, product.getDescription());
            pstmt.setInt(5, product.getPackSize());
            pstmt.setInt(6, product.getMinStockLevel());
            pstmt.setString(7, product.getShelfLocation());

            pstmt.executeUpdate();
            System.out.println("✅ Product Added: " + product.getName());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- 2. READ (ALL) ---
    @Override
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    // --- 2b. READ (BY ID) ---
    @Override
    public Product getProductById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // --- 3. UPDATE ---
    @Override
    public void updateProduct(Product product) {
        String sql = "UPDATE products SET name=?, generic_name=?, manufacturer=?, description=?, pack_size=?, min_stock_level=?, shelf_location=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getGenericName());
            pstmt.setString(3, product.getManufacturer());
            pstmt.setString(4, product.getDescription());
            pstmt.setInt(5, product.getPackSize());
            pstmt.setInt(6, product.getMinStockLevel());
            pstmt.setString(7, product.getShelfLocation());
            pstmt.setInt(8, product.getId()); // The WHERE clause

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Product Updated: " + product.getName());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- 4. DELETE ---
    @Override
    public void deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("✅ Product Deleted (ID: " + id + ")");

        } catch (SQLException e) {
            // This happens if you try to delete a product that has already been sold (Foreign Key Constraint)
            System.err.println("❌ Cannot delete product! It has existing sales or batches history.");
            e.printStackTrace();
        }
    }

    // Helper Method to avoid code duplication
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("generic_name"),
                rs.getString("manufacturer"),
                rs.getString("description"),
                rs.getInt("pack_size"),
                rs.getInt("min_stock_level"),
                rs.getString("shelf_location")
        );
    }
}