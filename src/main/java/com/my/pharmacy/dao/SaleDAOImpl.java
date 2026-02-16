package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.model.Sale;
import com.my.pharmacy.model.SaleItem;

import java.sql.*;

public class SaleDAOImpl implements SaleDAO {

    public SaleDAOImpl() {
        createTablesIfNotExists();
    }

    private void createTablesIfNotExists() {
        try (Statement stmt = DatabaseConnection.getInstance().createStatement()) {
            // 1. Table for the Invoice Header
            String saleSql = """
                CREATE TABLE IF NOT EXISTS sales (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    invoice_no TEXT UNIQUE NOT NULL,
                    timestamp TEXT,
                    total_amount REAL,
                    payment_method TEXT
                );
                """;
            stmt.execute(saleSql);

            // 2. Table for the Invoice Items
            String itemSql = """
                CREATE TABLE IF NOT EXISTS sale_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_id INTEGER,
                    product_id INTEGER,
                    batch_id INTEGER,
                    quantity INTEGER,
                    sub_total REAL,
                    FOREIGN KEY(sale_id) REFERENCES sales(id)
                );
                """;
            stmt.execute(itemSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveSale(Sale sale) throws Exception {
        Connection conn = DatabaseConnection.getInstance();

        try {
            // CRITICAL: Turn off auto-save. We want to save everything or nothing.
            conn.setAutoCommit(false);

            // 1. Insert the Sale Header
            String saleSql = "INSERT INTO sales(invoice_no, timestamp, total_amount, payment_method) VALUES(?,?,?,?)";
            int saleId = 0;

            try (PreparedStatement pstmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, sale.getInvoiceNumber());
                pstmt.setString(2, sale.getTimestamp().toString());
                pstmt.setDouble(3, sale.getTotalAmount());
                pstmt.setString(4, sale.getPaymentMethod());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    saleId = rs.getInt(1);
                }
            }

            // 2. Insert Every Item in the Cart
            String itemSql = "INSERT INTO sale_items(sale_id, product_id, batch_id, quantity, sub_total) VALUES(?,?,?,?,?)";
            String stockSql = "UPDATE batches SET qty_on_hand = qty_on_hand - ? WHERE batch_id = ?";

            try (PreparedStatement itemStmt = conn.prepareStatement(itemSql);
                 PreparedStatement stockStmt = conn.prepareStatement(stockSql)) {

                for (SaleItem item : sale.getItems()) {
                    // A. Save the Item Row
                    itemStmt.setInt(1, saleId);
                    itemStmt.setInt(2, item.getProductId());
                    itemStmt.setInt(3, item.getBatchId());
                    itemStmt.setInt(4, item.getQuantity());
                    itemStmt.setDouble(5, item.getSubTotal());
                    itemStmt.addBatch(); // Group them for speed

                    // B. Deduct Stock immediately
                    stockStmt.setInt(1, item.getQuantity());
                    stockStmt.setInt(2, item.getBatchId());
                    stockStmt.addBatch();
                }

                // Execute all updates at once
                itemStmt.executeBatch();
                stockStmt.executeBatch();
            }

            // 3. Commit (Save everything permanently)
            conn.commit();
            System.out.println("✅ Invoice Saved: " + sale.getInvoiceNumber());

        } catch (Exception e) {
            // ROLLBACK: If anything crashed, undo all changes!
            conn.rollback();
            System.err.println("❌ Transaction Failed! Rolling back changes.");
            throw e;
        } finally {
            conn.setAutoCommit(true); // Reset to default
        }
    }
}