package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.model.Sale;
import com.my.pharmacy.model.SaleItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleDAOImpl implements SaleDAO {

    private BatchDAO batchDAO = new BatchDAOImpl();

    // --- 1. SAVE SALE (Complex Transaction) ---
    @Override
    public void saveSale(Sale sale) {
        String insertSaleSQL = "INSERT INTO sales (total_amount, payment_mode, customer_id, salesman_id) VALUES (?, ?, ?, ?)";
        String insertItemSQL = "INSERT INTO sale_items (sale_id, product_id, batch_id, quantity, unit_price, sub_total, bonus_qty, discount_percent) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // A. Insert Sale Header
            try (PreparedStatement pstmt = conn.prepareStatement(insertSaleSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setDouble(1, sale.getTotalAmount());
                pstmt.setString(2, sale.getPaymentMode());
                pstmt.setInt(3, sale.getCustomerId());
                pstmt.setInt(4, sale.getSalesmanId());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                int saleId = 0;
                if (rs.next()) saleId = rs.getInt(1);

                // B. Insert Items & Reduce Stock
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

                        // C. Update Inventory
                        batchDAO.reduceStock(item.getBatchId(), item.getQuantity() + item.getBonusQty());
                    }
                    itemStmt.executeBatch();
                }
            }
            conn.commit(); // Commit Transaction
            System.out.println("✅ Sale Saved Successfully!");

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // --- 2. GET ALL SALES (For Reports) ---
    @Override
    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY sale_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Sale sale = new Sale(
                        rs.getInt("id"),
                        rs.getTimestamp("sale_date"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_mode"),
                        rs.getInt("customer_id"),
                        rs.getInt("salesman_id")
                );
                sales.add(sale);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    // --- 3. GET SALE BY ID (For Reprinting) ---
    @Override
    public Sale getSaleById(int id) {
        String sql = "SELECT * FROM sales WHERE id = ?";
        Sale sale = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    sale = new Sale(
                            rs.getInt("id"),
                            rs.getTimestamp("sale_date"),
                            rs.getDouble("total_amount"),
                            rs.getString("payment_mode"),
                            rs.getInt("customer_id"),
                            rs.getInt("salesman_id")
                    );
                    // Fetch items for this sale? (Optional: implement getItemsBySaleId if needed)
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sale;
    }
}