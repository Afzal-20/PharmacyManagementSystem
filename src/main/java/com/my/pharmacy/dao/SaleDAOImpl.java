package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.model.Sale;
import com.my.pharmacy.model.SaleItem;
import com.my.pharmacy.model.SaleLedgerRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar; // Required for TimeZone fix
import java.util.List;
import java.util.TimeZone; // Required for TimeZone fix

public class SaleDAOImpl implements SaleDAO {

    private final BatchDAO batchDAO = new BatchDAOImpl();

    @Override
    public void saveSale(Sale sale) {
        // FIX 1: Changed 'salesman_id' to 'user_id'
        String insertSaleSQL = "INSERT INTO sales (total_amount, amount_paid, balance_due, payment_mode, customer_id, user_id) VALUES (?, ?, ?, ?, ?, ?)";
        String insertItemSQL = "INSERT INTO sale_items (sale_id, product_id, batch_id, quantity, unit_price, sub_total, bonus_qty, discount_percent) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String updateCustomerSQL = "UPDATE customers SET current_balance = current_balance + ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Insert Sale Header
            try (PreparedStatement pstmt = conn.prepareStatement(insertSaleSQL, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setDouble(1, sale.getTotalAmount());
                pstmt.setDouble(2, sale.getAmountPaid());
                pstmt.setDouble(3, sale.getBalanceDue());
                pstmt.setString(4, sale.getPaymentMode());
                pstmt.setInt(5, sale.getCustomerId());
                pstmt.setInt(6, sale.getSalesmanId()); // Maps Java 'salesmanId' to DB 'user_id'
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                int saleId = 0;
                if (rs.next()) {
                    saleId = rs.getInt(1);
                    sale.setId(saleId); // Update ID in memory
                }

                // 2. Update Customer Khata Balance
                if (sale.getBalanceDue() > 0) {
                    try (PreparedStatement custStmt = conn.prepareStatement(updateCustomerSQL)) {
                        custStmt.setDouble(1, sale.getBalanceDue());
                        custStmt.setInt(2, sale.getCustomerId());
                        custStmt.executeUpdate();
                    }
                }

                // 3. Insert Items & Update Inventory
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

                        batchDAO.reduceStock(conn, item.getBatchId(), item.getQuantity() + item.getBonusQty());
                    }
                    itemStmt.executeBatch();
                }
            }
            conn.commit();
            System.out.println("✅ Sale saved successfully.");

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public List<Sale> getAllSales() {
        return getSalesByDate(null);
    }

    @Override
    public Sale getSaleById(int id) {
        String sql = "SELECT * FROM sales WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRowToSale(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // FIX 2: Correct TimeZone handling for Item Ledger (Sales Tab)
    @Override
    public List<SaleLedgerRecord> getSalesHistoryByProductId(int productId) {
        List<SaleLedgerRecord> history = new ArrayList<>();
        // Helper calendar to interpret DB time as UTC
        Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        String sql = "SELECT s.id, s.sale_date, si.quantity, si.unit_price, si.sub_total " +
                "FROM sale_items si JOIN sales s ON si.sale_id = s.id " +
                "WHERE si.product_id = ? ORDER BY s.sale_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new SaleLedgerRecord(
                            rs.getInt("id"),
                            rs.getTimestamp("sale_date", utcCal), // <--- Converts UTC DB time to Local Java time
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getDouble("sub_total")
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return history;
    }

    @Override
    public List<Sale> getSalesByDate(java.time.LocalDate date) {
        List<Sale> sales = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM sales");

        // FIX 3: Use 'localtime' in SQL so filtering matches your PC date (e.g. 2:40 AM belongs to Today, not Yesterday)
        if (date != null) {
            sql.append(" WHERE date(sale_date, 'localtime') = date(?)");
        }
        sql.append(" ORDER BY sale_date DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            if (date != null) pstmt.setString(1, date.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sales.add(mapRowToSale(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return sales;
    }

    // FIX 4: Centralized mapping helper with TimeZone and User ID fixes
    private Sale mapRowToSale(ResultSet rs) throws SQLException {
        // Create a Calendar set to UTC. This tells the driver "The string in DB is UTC, please convert it to my Local Time".
        Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        return new Sale(
                rs.getInt("id"),
                rs.getTimestamp("sale_date", utcCal), // <--- Converts UTC DB time to Local Java time
                rs.getDouble("total_amount"),
                rs.getString("payment_mode"),
                rs.getInt("customer_id"),
                rs.getInt("user_id"), // Fixed column mapping
                rs.getDouble("amount_paid"),
                rs.getDouble("balance_due")
        );
    }

    @Override
    public List<SaleItem> getSaleItemsBySaleId(int saleId) {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT si.*, p.name as product_name FROM sale_items si " +
                "JOIN batches b ON si.batch_id = b.id JOIN products p ON b.product_id = p.id WHERE si.sale_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem(rs.getInt("product_id"), rs.getInt("batch_id"), rs.getInt("quantity"),
                            rs.getDouble("unit_price"), rs.getInt("bonus_qty"), rs.getDouble("discount_percent"));
                    item.setId(rs.getInt("id"));
                    item.setSaleId(saleId);
                    item.setProductName(rs.getString("product_name"));
                    item.setReturnedQty(rs.getInt("returned_qty"));
                    items.add(item);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return items;
    }

    @Override
    public void processReturn(int saleId, int customerId, SaleItem item, int returnQty, double refundAmount, String refundMethod, String reason) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String insertReturn = "INSERT INTO sale_returns (sale_id, sale_item_id, batch_id, returned_qty, refund_amount, refund_method, reason) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertReturn)) {
                pstmt.setInt(1, saleId);
                pstmt.setInt(2, item.getId());
                pstmt.setInt(3, item.getBatchId());
                pstmt.setInt(4, returnQty);
                pstmt.setDouble(5, refundAmount);
                pstmt.setString(6, refundMethod);
                pstmt.setString(7, reason);
                pstmt.executeUpdate();
            }
            String updateSaleItem = "UPDATE sale_items SET returned_qty = returned_qty + ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSaleItem)) {
                pstmt.setInt(1, returnQty);
                pstmt.setInt(2, item.getId());
                pstmt.executeUpdate();
            }
            String updateBatch = "UPDATE batches SET qty_on_hand = qty_on_hand + ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateBatch)) {
                pstmt.setInt(1, returnQty);
                pstmt.setInt(2, item.getBatchId());
                pstmt.executeUpdate();
            }
            // Use exact string matching for Khata Logic
            if ("KHATA CREDIT".equals(refundMethod) && customerId != 1) {
                String updateKhata = "UPDATE customers SET current_balance = current_balance - ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateKhata)) {
                    pstmt.setDouble(1, refundAmount);
                    pstmt.setInt(2, customerId);
                    pstmt.executeUpdate();
                }
                String insertPayment = "INSERT INTO payments (entity_id, entity_type, amount, payment_mode, description) VALUES (?, 'CUSTOMER', ?, 'RETURN_CREDIT', ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertPayment)) {
                    pstmt.setInt(1, customerId);
                    pstmt.setDouble(2, refundAmount);
                    pstmt.setString(3, "Refund for Invoice #" + saleId);
                    pstmt.executeUpdate();
                }
            } else {
                String insertPayment = "INSERT INTO payments (entity_id, entity_type, amount, payment_mode, description) VALUES (?, 'CUSTOMER', ?, 'CASH_REFUND', ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertPayment)) {
                    pstmt.setInt(1, customerId);
                    pstmt.setDouble(2, -refundAmount);
                    pstmt.setString(3, "Cash Refund Inv #" + saleId);
                    pstmt.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @Override
    public double getCurrentMonthTotalSales() {
        String sql = "SELECT SUM(total_amount) FROM sales WHERE date(sale_date, 'localtime') >= date('now', 'start of month', 'localtime')";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

}