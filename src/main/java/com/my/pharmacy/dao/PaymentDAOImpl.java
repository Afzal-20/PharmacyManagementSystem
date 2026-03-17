package com.my.pharmacy.dao;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.model.LedgerRecord;
import com.my.pharmacy.model.Payment;
import com.my.pharmacy.util.TimeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class PaymentDAOImpl implements PaymentDAO {

    private static final Logger log = LoggerFactory.getLogger(PaymentDAOImpl.class);

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

            // Balance is now calculated exclusively via getDynamicCustomerBalance() which reads
            // directly from the payments and sales tables. No stored current_balance writes needed.
            // This eliminates the dual source of truth that could silently drift out of sync.

            conn.commit();
        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { log.error("{}: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex); }
            log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e); }
        }
    }

    @Override
    public List<LedgerRecord> getCustomerLedger(int customerId) {
        List<LedgerRecord> ledger = new ArrayList<>();
        // Issue 3: Show ALL invoices (full purchase history), not just khata ones
        // Debit = total invoice amount (so customer can see everything they bought)
        // Credit = cash paid at time of sale + khata payments received + return credits
        String sql =
                // All invoices — full amount as debit
                "SELECT sale_date as date_val, 'Invoice #' || id || ' (Cash: ' || ROUND(amount_paid,0) || ')' as desc, " +
                        "total_amount as debit, 0 as credit " +
                        "FROM sales WHERE customer_id = ? " +
                        "UNION ALL " +
                        // All payments: cash recoveries + return credits
                        "SELECT payment_date as date_val, description as desc, 0 as debit, amount as credit " +
                        "FROM payments WHERE entity_id = ? AND entity_type = 'CUSTOMER' " +
                        "AND payment_mode IN ('CASH', 'RETURN_CREDIT') " +
                        "ORDER BY date_val ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            pstmt.setInt(2, customerId);
            // FIX: pass UTC Calendar so the driver treats the stored string as UTC,
            // then TimeUtil.format() converts it to local time for display
            Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String formattedDate = TimeUtil.format(rs.getTimestamp("date_val", utcCal));
                    ledger.add(new LedgerRecord(formattedDate, rs.getString("desc"), rs.getDouble("debit"), rs.getDouble("credit")));
                }
            }
        } catch (SQLException e) { log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e); }
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
            Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String formattedDate = TimeUtil.format(rs.getTimestamp("date_val", utcCal));
                    ledger.add(new LedgerRecord(formattedDate, rs.getString("desc"), rs.getDouble("debit"), rs.getDouble("credit")));
                }
            }
        } catch (SQLException e) { log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e); }
        return ledger;
    }

    @Override
    public double getDynamicCustomerBalance(int customerId) {
        // Balance = total khata billed − (CASH recoveries + RETURN_CREDIT reductions)
        // CASH_REFUND is excluded here: every cash refund is accompanied by a RETURN_CREDIT
        // entry that already cancels the khata portion. Including CASH_REFUND would double-count.
        String sql = "SELECT " +
                "(SELECT TOTAL(balance_due) FROM sales WHERE customer_id = ?) - " +
                "(SELECT TOTAL(amount) FROM payments " +
                " WHERE entity_id = ? AND entity_type = 'CUSTOMER' AND payment_mode IN ('CASH', 'RETURN_CREDIT'))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            pstmt.setInt(2, customerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e); }
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
        } catch (SQLException e) { log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e); }
        return 0.0;
    }

}