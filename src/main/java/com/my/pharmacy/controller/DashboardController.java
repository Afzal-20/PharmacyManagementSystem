package com.my.pharmacy.controller;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.Batch;
import com.my.pharmacy.util.CalculationEngine;
import com.my.pharmacy.util.TimeUtil;
import com.my.pharmacy.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    // Overview Labels
    @FXML private Label lblMonthlySales; // Renamed to represent Month
    @FXML private Label lblLowStock;
    @FXML private Label lblExpiry;
    @FXML private Label lblDealers;

    // Daily Closing Labels
    @FXML private Label lblCloseSales;
    @FXML private Label lblCloseCashIn;
    @FXML private Label lblCloseKhata;      // New khata billed today only
    @FXML private Label lblCloseRecovered;  // Old khata recovered today
    @FXML private Label lblCloseRefunds;

    // Quick Actions
    @FXML private Button btnQuickPurchase;
    @FXML private Button btnQuickBackup;

    private final SaleDAO saleDAO = new SaleDAOImpl();
    private final BatchDAO batchDAO = new BatchDAOImpl();
    private final DealerDAO dealerDAO = new DealerDAOImpl();

    @FXML
    public void initialize() {
        refreshDashboard();
        calculateMonthlySales();
        calculateDailyClosing();
        enforceRBAC();
    }

    private void enforceRBAC() {
        boolean isAdmin = UserSession.getInstance() != null && UserSession.getInstance().getUser().isAdmin();
        btnQuickPurchase.setVisible(isAdmin);
        btnQuickPurchase.setManaged(isAdmin);
        btnQuickBackup.setVisible(isAdmin);
        btnQuickBackup.setManaged(isAdmin);
    }

    private void refreshDashboard() {
        // 1. Analyze Inventory against User-Defined Minimum Stock Levels
        List<Batch> allBatches = batchDAO.getAllBatches();
        long lowStockCount = allBatches.stream()
                .filter(b -> b.getQtyOnHand() <= b.getProduct().getMinStockLevel())
                .count();
        lblLowStock.setText(String.valueOf(lowStockCount));

        LocalDate sixMonthsFromNow = TimeUtil.today().plusMonths(6);
        long expiryCount = allBatches.stream().filter(b -> {
            try {
                return LocalDate.parse(b.getExpiryDate()).isBefore(sixMonthsFromNow);
            } catch (Exception e) { return false; }
        }).count();
        lblExpiry.setText(String.valueOf(expiryCount));

        // 2. Count Dealers
        int dealerCount = dealerDAO.getAllDealers().size();
        lblDealers.setText(String.valueOf(dealerCount));
    }

    private void calculateMonthlySales() {
        double monthlyTotal = saleDAO.getCurrentMonthTotalSales();
        lblMonthlySales.setText(String.format("Rs. %,.0f", monthlyTotal));
    }

    private void calculateDailyClosing() {
        // TWO separate queries — avoids LEFT JOIN row-multiplication bug.
        // (e.g. 3 sales × 4 payments = 12 rows, DISTINCT fails on floats)

        // Query 1: Today's sales totals
        double totalBilled = 0, cashFromSales = 0, khataBilled = 0;
        String saleSql = "SELECT TOTAL(total_amount) as total_billed, " +
                "TOTAL(amount_paid)         as cash_from_sales, " +
                "TOTAL(balance_due)         as khata_billed " +
                "FROM sales WHERE " + TimeUtil.sqlToday("sale_date");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(saleSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                totalBilled   = rs.getDouble("total_billed");
                cashFromSales = rs.getDouble("cash_from_sales");
                khataBilled   = rs.getDouble("khata_billed");
            }
        } catch (Exception e) { log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e); }

        // Query 2: Today's customer payments (recoveries + refunds)
        double cashRecovered = 0, cashRefunds = 0;
        String paySql = "SELECT payment_mode, TOTAL(amount) as total " +
                "FROM payments WHERE entity_type='CUSTOMER' " +
                "AND " + TimeUtil.sqlToday("payment_date") +
                " GROUP BY payment_mode";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(paySql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String mode = rs.getString("payment_mode");
                double amt  = rs.getDouble("total");
                if ("CASH".equals(mode))        cashRecovered += amt;
                if ("CASH_REFUND".equals(mode)) cashRefunds   += amt;
            }
        } catch (Exception e) { log.error("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e); }

        // Net cash in drawer = cash collected at POS + old khata recovered - refunds paid out
        double netCash = CalculationEngine.calculateNetCashInDrawer(cashFromSales, cashRecovered, cashRefunds);

        // Khata Billed  = new credit created today (balance_due from sales)
        // Khata Recovered = old khata cash collected today — shown separately, NOT subtracted
        // These are independent figures for the pharmacist's closing report

        lblCloseSales.setText(String.format("Rs. %,.0f", totalBilled));
        lblCloseCashIn.setText(String.format("Rs. %,.0f", netCash));
        lblCloseKhata.setText(String.format("Rs. %,.0f", khataBilled));
        lblCloseRecovered.setText(String.format("Rs. %,.0f", cashRecovered));
        lblCloseRefunds.setText(String.format("Rs. %,.0f", cashRefunds));
    }

    // --- Navigation Shortcuts via MainController ---
    @FXML
    private void handleQuickPOS() {
        if (MainController.getInstance() != null) MainController.getInstance().showPOS();
    }

    @FXML
    private void handleQuickPurchase() {
        if (MainController.getInstance() != null) MainController.getInstance().showPurchaseEntry();
    }

    @FXML
    private void handleQuickExpiry() {
        if (MainController.getInstance() != null) MainController.getInstance().showExpiry();
    }

    @FXML
    private void handleQuickBackup() {
        if (MainController.getInstance() != null) MainController.getInstance().showBackup();
    }
}