package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.model.LedgerRecord;
import com.my.pharmacy.model.Payment;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAOImpl implements PaymentDAO {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

    @Override
    public void recordPayment(Payment p) {
        String insertPayment = "INSERT INTO payments (entity_id, entity_type, amount, payment_mode, description) VALUES (?, ?, ?, ?, ?)";
        // If it's a customer paying us, it REDUCES their balance.
        String updateCustomer = "UPDATE customers SET current_balance = current_balance - ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(insertPayment)) {
                pstmt.setInt(1, p.getEntityId());
                pstmt.setString(2, p.getEntityType());
                pstmt.setDouble(3, p.getAmount());
                pstmt.setString(4, p.getPaymentMode());
                pstmt.setString(5, p.getDescription());
                pstmt.executeUpdate();
            }

            if ("CUSTOMER".equals(p.getEntityType())) {
                try (PreparedStatement pstmt = conn.prepareStatement(updateCustomer)) {
                    pstmt.setDouble(1, p.getAmount());
                    pstmt.setInt(2, p.getEntityId());
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
    public List<LedgerRecord> getCustomerLedger(int customerId) {
        List<LedgerRecord> ledger = new ArrayList<>();
        // Query to union Sales (Debits) and Payments (Credits) into one chronological timeline
        String sql = "SELECT sale_date as date_val, 'Invoice #' || id as desc, balance_due as debit, 0 as credit " +
                "FROM sales WHERE customer_id = ? AND balance_due > 0 " +
                "UNION ALL " +
                "SELECT payment_date as date_val, description as desc, 0 as debit, amount as credit " +
                "FROM payments WHERE entity_id = ? AND entity_type = 'CUSTOMER' " +
                "ORDER BY date_val ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            pstmt.setInt(2, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String formattedDate = sdf.format(rs.getTimestamp("date_val"));
                    ledger.add(new LedgerRecord(formattedDate, rs.getString("desc"), rs.getDouble("debit"), rs.getDouble("credit")));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ledger;
    }
}