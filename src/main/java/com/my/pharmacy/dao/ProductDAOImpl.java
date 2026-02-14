package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class ProductDAOImpl implements ProductDAO {

    // 1. Initialize Table on Startup
    public ProductDAOImpl() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    generic_name TEXT,
                    manufacturer TEXT,
                    supplier_id INTEGER,
                    tax_rate REAL DEFAULT 0.0,
                    pack_size INTEGER DEFAULT 1,
                    min_stock_alert INTEGER DEFAULT 10
                );
                """;
        // FIX: We get the connection, but we do NOT close it here.
        // We only close the Statement.
        Connection conn = DatabaseConnection.getInstance();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addProduct(Product p) {
        String sql = "INSERT INTO products(name, generic_name, manufacturer, supplier_id, tax_rate, pack_size, min_stock_alert) VALUES(?,?,?,?,?,?,?)";

        Connection conn = DatabaseConnection.getInstance();
        // FIX: Only the PreparedStatement is in the try block
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, p.getName());
            pstmt.setString(2, p.getGenericName());
            pstmt.setString(3, p.getManufacturer());
            pstmt.setInt(4, p.getSupplierId());
            pstmt.setDouble(5, p.getTaxRate());
            pstmt.setInt(6, p.getPackSize());
            pstmt.setInt(7, p.getMinStockAlert());

            pstmt.executeUpdate();
            System.out.println("✅ Product Saved: " + p.getName());

        } catch (SQLException e) {
            System.err.println("Error saving product: " + e.getMessage());
        }
    }

    @Override
    public ObservableList<Product> getAllProducts() {
        ObservableList<Product> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM products";

        Connection conn = DatabaseConnection.getInstance();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- Helper Methods ---

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("generic_name"),
                rs.getString("manufacturer"),
                rs.getInt("supplier_id"),
                rs.getDouble("tax_rate"),
                rs.getInt("pack_size"),
                rs.getInt("min_stock_alert")
        );
    }

    // We will implement update/delete/search in the next step to keep this manageable.
    @Override public void updateProduct(Product product) {} // TODO
    @Override public void deleteProduct(int id) {} // TODO
    @Override public Product getProduct(int id) { return null; } // TODO
    @Override public ObservableList<Product> searchProducts(String query) { return null; } // TODO
}