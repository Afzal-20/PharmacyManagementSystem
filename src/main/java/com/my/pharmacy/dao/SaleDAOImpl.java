package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.model.Sale;
import com.my.pharmacy.model.SaleItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleDAOImpl implements SaleDAO {

    private final BatchDAO batchDAO = new BatchDAOImpl();

    @Override
    public void saveSale(Sale sale) {
        String insertSaleSQL = "INSERT INTO sales (total_amount, payment_mode, customer_id, salesman_id) VALUES (?, ?, ?, ?)";
        String insertItemSQL = "INSERT INTO sale_items (sale_id, product_id, batch_id, quantity, unit_price, sub_total, bonus_qty, discount_percent) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start Transaction for Safety

            // 1. Insert Sale Header
            try (PreparedStatement pstmt = conn.prepareStatement(insertSaleSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setDouble(1, sale.getTotalAmount());
                pstmt.setString(2, sale.getPaymentMode());
                pstmt.setInt(3, sale.getCustomerId());
                pstmt.setInt(4, sale.getSalesmanId());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                int saleId = 0;
                if (rs.next()) saleId = rs.getInt(1);

                // 2. Insert Items & Update Inventory
                try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSQL)) {
                    for (SaleItem item : sale.getItems()) {
                        itemStmt.setInt(1, saleId);
                        itemStmt.setInt(2, item.getProductId());
                        itemStmt.setInt(3, item.getBatchId());
                        itemStmt.setInt(4, item.getQuantity());
                        itemStmt.setDouble(5, item.getUnitPrice());
                        itemStmt.setDouble(6, item.getSubTotal());
                        itemStmt.setInt(7, item.getBonusQty());
                        itemStmt.setDouble(8, item.getDiscountPercent());
                        itemStmt.addBatch();

                        // Update Stock (Hybrid: Deducts individual units)
                        batchDAO.reduceStock(item.getBatchId(), item.getQuantity() + item.getBonusQty());
                    }
                    itemStmt.executeBatch();
                }
            }
            conn.commit(); // Save all changes
            System.out.println("✅ Sale and stock levels updated successfully.");

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY sale_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sales.add(new Sale(rs.getInt("id"), rs.getTimestamp("sale_date"),
                        rs.getDouble("total_amount"), rs.getString("payment_mode"),
                        rs.getInt("customer_id"), rs.getInt("salesman_id")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return sales;
    }

    @Override
    public Sale getSaleById(int id) {
        String sql = "SELECT * FROM sales WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Sale(rs.getInt("id"), rs.getTimestamp("sale_date"),
                            rs.getDouble("total_amount"), rs.getString("payment_mode"),
                            rs.getInt("customer_id"), rs.getInt("salesman_id"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}