package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.model.Batch;
import com.my.pharmacy.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BatchDAOImpl implements BatchDAO {

    @Override
    public void addBatch(Batch b) {
        String sql = "INSERT INTO batches (product_id, batch_no, expiry_date, qty_on_hand, " +
                "cost_price, trade_price, retail_price, discount_percent, tax_percent, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, b.getProductId());
            pstmt.setString(2, b.getBatchNo());
            pstmt.setString(3, b.getExpiryDate());
            pstmt.setInt(4, b.getQtyOnHand());
            pstmt.setDouble(5, b.getCostPrice());
            pstmt.setDouble(6, b.getTradePrice());
            pstmt.setDouble(7, b.getRetailPrice());
            pstmt.setDouble(8, b.getDiscountPercent());
            pstmt.setDouble(9, 0.0);
            pstmt.setInt(10, 1);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public List<Batch> getAllBatches() {
        List<Batch> batches = new ArrayList<>();
        String sql = "SELECT b.*, p.name, p.generic_name, p.pack_size FROM batches b " +
                "JOIN products p ON b.product_id = p.id WHERE b.is_active = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                batches.add(mapResultSetToBatch(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return batches;
    }

    @Override
    public Batch getBatchById(int id) {
        String sql = "SELECT b.*, p.name, p.generic_name, p.pack_size FROM batches b " +
                "JOIN products p ON b.product_id = p.id WHERE b.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToBatch(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<Batch> getBatchesByProductId(int productId) {
        List<Batch> batches = new ArrayList<>();
        String sql = "SELECT b.*, p.name, p.generic_name, p.pack_size FROM batches b " +
                "JOIN products p ON b.product_id = p.id WHERE b.product_id = ? AND b.is_active = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()){
                    batches.add(mapResultSetToBatch(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return batches;
    }

    // --- NEW: Update Batch Details ---
    @Override
    public void updateBatch(Batch b) {
        String sql = "UPDATE batches SET product_id = ?, batch_no = ?, expiry_date = ?, " +
                "qty_on_hand = ?, cost_price = ?, trade_price = ?, retail_price = ?, " +
                "discount_percent = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, b.getProductId());
            pstmt.setString(2, b.getBatchNo());
            pstmt.setString(3, b.getExpiryDate());
            pstmt.setInt(4, b.getQtyOnHand());
            pstmt.setDouble(5, b.getCostPrice());
            pstmt.setDouble(6, b.getTradePrice());
            pstmt.setDouble(7, b.getRetailPrice());
            pstmt.setDouble(8, b.getDiscountPercent());
            pstmt.setInt(9, b.getId());
            pstmt.executeUpdate();
            System.out.println("✅ Batch " + b.getBatchNo() + " updated.");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- NEW: Soft Delete Batch (By Setting is_active to 0) ---
    @Override
    public void deleteBatch(int id) {
        // We usually don't delete medicine records completely to maintain sales history.
        // Instead, we "deactivate" them.
        String sql = "UPDATE batches SET is_active = 0 WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("🗑️ Batch deactivated (ID: " + id + ")");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void reduceStock(int batchId, int qty) {
        String sql = "UPDATE batches SET qty_on_hand = qty_on_hand - ? WHERE id = ? AND qty_on_hand >= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, qty);
            pstmt.setInt(2, batchId);
            pstmt.setInt(3, qty);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Batch mapResultSetToBatch(ResultSet rs) throws SQLException {
        Product p = new Product(rs.getInt("product_id"), rs.getString("name"),
                rs.getString("generic_name"), "", "",
                rs.getInt("pack_size"), 0, "");
        Batch b = new Batch(rs.getInt("id"), rs.getInt("product_id"),
                rs.getString("batch_no"), rs.getString("expiry_date"),
                rs.getInt("qty_on_hand"), rs.getDouble("cost_price"),
                rs.getDouble("trade_price"), rs.getDouble("retail_price"),
                rs.getDouble("discount_percent"));
        b.setProduct(p);
        return b;
    }
}