package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {

    private static final Logger log = LoggerFactory.getLogger(ProductDAOImpl.class);

    @Override
    public int addProduct(Product product) {
        String sql = "INSERT INTO products (name, generic_name, manufacturer, description, pack_size, min_stock_level, shelf_location) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getGenericName());
            pstmt.setString(3, product.getManufacturer());
            pstmt.setString(4, product.getDescription());
            pstmt.setInt(5, product.getPackSize());
            pstmt.setInt(6, product.getMinStockLevel());
            pstmt.setString(7, product.getShelfLocation());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Failed to add product '{}': {}", product.getName(), e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) products.add(mapResultSetToProduct(rs));
        } catch (SQLException e) {
            log.error("Failed to load products: {}", e.getMessage(), e);
        }
        return products;
    }

    @Override
    public Product getProductById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            log.error("Failed to get product by id {}: {}", id, e.getMessage(), e);
        }
        return null;
    }

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
            pstmt.setInt(8, product.getId());
            pstmt.executeUpdate();
            log.info("Product updated: {}", product.getName());
        } catch (SQLException e) {
            log.error("Failed to update product '{}': {}", product.getName(), e.getMessage(), e);
        }
    }

    @Override
    public void deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            log.info("Product deleted (id={})", id);
        } catch (SQLException e) {
            // FK constraint fires if product has sales or batch history — expected behaviour
            log.error("Cannot delete product id={} — it may have existing sales or batch history: {}", id, e.getMessage(), e);
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"), rs.getString("name"), rs.getString("generic_name"),
                rs.getString("manufacturer"), rs.getString("description"),
                rs.getInt("pack_size"), rs.getInt("min_stock_level"), rs.getString("shelf_location"));
    }
}