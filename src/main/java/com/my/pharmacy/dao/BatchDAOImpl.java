package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.dao.BatchDAO;
import com.my.pharmacy.model.Batch;
import com.my.pharmacy.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BatchDAOImpl implements BatchDAO {

    // --- 1. CREATE (ADD STOCK) ---
    @Override
    public void addBatch(Batch batch) {
        String sql = "INSERT INTO batches (product_id, batch_no, expiry_date, qty_on_hand, cost_price, trade_price, retail_price, tax_percent, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, batch.getProductId());
            pstmt.setString(2, batch.getBatchNo());
            pstmt.setString(3, batch.getExpiryDate());
            pstmt.setInt(4, batch.getQtyOnHand());
            pstmt.setDouble(5, batch.getCostPrice());
            pstmt.setDouble(6, batch.getTradePrice());
            pstmt.setDouble(7, batch.getRetailPrice());
            pstmt.setDouble(8, batch.getTaxPercent());

            pstmt.executeUpdate();
            System.out.println("✅ Batch Added: " + batch.getBatchNo());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- 2. READ (ALL) ---
    @Override
    public List<Batch> getAllBatches() {
        // We JOIN with products to get the Name for the Grid View
        String sql = "SELECT b.*, p.name, p.generic_name, p.manufacturer, p.pack_size " +
                "FROM batches b " +
                "JOIN products p ON b.product_id = p.id " +
                "WHERE b.is_active = 1 AND b.qty_on_hand > 0";
        return executeQuery(sql);
    }

    // --- 2b. READ (BY ID) ---
    @Override
    public Batch getBatchById(int id) {
        String sql = "SELECT b.*, p.name, p.generic_name, p.manufacturer, p.pack_size " +
                "FROM batches b " +
                "JOIN products p ON b.product_id = p.id " +
                "WHERE b.batch_id = ?";
        List<Batch> results = executeQuery(sql, id);
        return results.isEmpty() ? null : results.get(0);
    }

    // --- 2c. READ (BY PRODUCT) ---
    @Override
    public List<Batch> getBatchesByProductId(int productId) {
        String sql = "SELECT b.*, p.name, p.generic_name, p.manufacturer, p.pack_size " +
                "FROM batches b " +
                "JOIN products p ON b.product_id = p.id " +
                "WHERE b.product_id = ?";
        return executeQuery(sql, productId);
    }

    // --- 3. UPDATE ---
    @Override
    public void updateBatch(Batch batch) {
        String sql = "UPDATE batches SET batch_no=?, expiry_date=?, qty_on_hand=?, cost_price=?, trade_price=?, retail_price=?, tax_percent=? WHERE batch_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, batch.getBatchNo());
            pstmt.setString(2, batch.getExpiryDate());
            pstmt.setInt(3, batch.getQtyOnHand());
            pstmt.setDouble(4, batch.getCostPrice());
            pstmt.setDouble(5, batch.getTradePrice());
            pstmt.setDouble(6, batch.getRetailPrice());
            pstmt.setDouble(7, batch.getTaxPercent());
            pstmt.setInt(8, batch.getBatchId());

            pstmt.executeUpdate();
            System.out.println("✅ Batch Updated: " + batch.getBatchNo());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- 4. DELETE ---
    @Override
    public void deleteBatch(int id) {
        String sql = "DELETE FROM batches WHERE batch_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("✅ Batch Deleted (ID: " + id + ")");

        } catch (SQLException e) {
            System.err.println("❌ Cannot delete batch! It might be linked to sales.");
            e.printStackTrace();
        }
    }

    // --- 5. REDUCE STOCK (Transactional) ---
    @Override
    public void reduceStock(int batchId, int quantity) {
        String sql = "UPDATE batches SET qty_on_hand = qty_on_hand - ? WHERE batch_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantity);
            pstmt.setInt(2, batchId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- HELPER: Execute Query and Map Results ---
    private List<Batch> executeQuery(String sql, Object... params) {
        List<Batch> batches = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Map Batch Data
                    Batch batch = new Batch(
                            rs.getInt("batch_id"),
                            rs.getInt("product_id"),
                            rs.getString("batch_no"),
                            rs.getString("expiry_date"),
                            rs.getInt("qty_on_hand"),
                            rs.getDouble("cost_price"),
                            rs.getDouble("trade_price"),
                            rs.getDouble("retail_price"),
                            rs.getDouble("tax_percent")
                    );

                    // Map Linked Product Data (Transient)
                    Product p = new Product(
                            rs.getInt("product_id"),
                            rs.getString("name"),
                            rs.getString("generic_name"),
                            rs.getString("manufacturer"),
                            "",
                            rs.getInt("pack_size"),
                            0,
                            ""
                    );
                    batch.setProduct(p);
                    batches.add(batch);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return batches;
    }
}