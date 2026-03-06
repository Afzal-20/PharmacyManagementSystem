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

            // FIX #1: Only update customer balance for genuine cash recoveries (CASH).
            // CASH_REFUND entries are handled inside SaleDAOImpl.processReturn() and must
            // NOT trigger a second balance update here.
            if ("CUSTOMER".equals(p.getEntityType()) && "CASH".equals(p.getPaymentMode())) {
                String updateCustomer = "UPDATE customers SET current_balance = current_balance - ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateCustomer)) {
                    pstmt.setDouble(1, p.getAmount());
                    pstmt.setInt(2, p.getEntityId());
                    pstmt.executeUpdate();
                }
            }

            // FIX #10: Removed the "UPDATE dealers SET current_balance" block entirely.
            // The dealers.current_balance column was being written to on every purchase/payment
            // but was never read back anywhere — DealerDAOImpl.getAllDealers() never mapped it,
            // and the Dealer model has no currentBalance field.
            // getDynamicDealerBalance() calculates the correct live balance from the payments
            // ledger, making the stored column completely redundant.

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
        String sql = "SELECT sale_date as date_val, 'Invoice #' || id as desc, balance_due as debit, 0 as credit " +
                "FROM sales WHERE customer_id = ? AND balance_due > 0 " +
                "UNION ALL " +
                "SELECT payment_date as date_val, description as desc, 0 as debit, amount as credit " +
                "FROM payments WHERE entity_id = ? AND entity_type = 'CUSTOMER' AND payment_mode = 'CASH' " +
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

    @Override
    public List<LedgerRecord> getDealerLedger(int dealerId) {
        List<LedgerRecord> ledger = new ArrayList<>();
        // For Dealers, PURCHASE = Bill (Debit column), CASH = Payment Made (Credit column)
        String sql = "SELECT payment_date as date_val, description as desc, " +
                "CASE WHEN payment_mode = 'PURCHASE' THEN amount ELSE 0 END as debit, " +
                "CASE WHEN payment_mode != 'PURCHASE' THEN amount ELSE 0 END as credit " +
                "FROM payments WHERE entity_id = ? AND entity_type = 'DEALER' " +
                "ORDER BY date_val ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, dealerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String formattedDate = sdf.format(rs.getTimestamp("date_val"));
                    ledger.add(new LedgerRecord(formattedDate, rs.getString("desc"), rs.getDouble("debit"), rs.getDouble("credit")));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ledger;
    }

    @Override
    public double getDynamicCustomerBalance(int customerId) {
        // FIX #5: Only subtract CASH payments (genuine recoveries), not CASH_REFUND entries.
        // CASH_REFUND amounts are negative in the DB and would incorrectly inflate the balance
        // if included in the SUM.
        String sql = "SELECT " +
                "(SELECT TOTAL(balance_due) FROM sales WHERE customer_id = ?) - " +
                "(SELECT TOTAL(amount) FROM payments " +
                " WHERE entity_id = ? AND entity_type = 'CUSTOMER' AND payment_mode = 'CASH')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            pstmt.setInt(2, customerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    @Override
    public double getDynamicDealerBalance(int dealerId) {
        // Balance = (Sum of Purchases) - (Sum of Cash Paid Out)
        String sql = "SELECT TOTAL(CASE WHEN payment_mode = 'PURCHASE' THEN amount ELSE -amount END) " +
                "FROM payments WHERE entity_id = ? AND entity_type = 'DEALER'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, dealerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

}