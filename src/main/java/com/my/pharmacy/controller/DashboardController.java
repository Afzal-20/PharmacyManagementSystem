package com.my.pharmacy.controller;

import com.my.pharmacy.config.DatabaseConnection;
import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.Batch;
import com.my.pharmacy.model.Sale;
import com.my.pharmacy.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

public class DashboardController {

    // Overview Labels
    @FXML private Label lblMonthlySales; // Renamed to represent Month
    @FXML private Label lblLowStock;
    @FXML private Label lblExpiry;
    @FXML private Label lblDealers;

    // Daily Closing Labels
    @FXML private Label lblCloseSales;
    @FXML private Label lblCloseCashIn;
    @FXML private Label lblCloseKhata;
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

        LocalDate sixMonthsFromNow = LocalDate.now().plusMonths(6);
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
        // Step 1: Base metrics from Sales table for TODAY
        List<Sale> todaySales = saleDAO.getSalesByDate(LocalDate.now());
        double todayTotalSales = todaySales.stream().mapToDouble(Sale::getTotalAmount).sum();
        double todayCashFromSales = todaySales.stream().mapToDouble(Sale::getAmountPaid).sum();
        double todayKhataBilled = todaySales.stream().mapToDouble(Sale::getBalanceDue).sum();

        // Step 2: Extract Payments/Refunds strictly from the Ledger for TODAY
        double cashRecoveries = 0.0;
        double cashRefunds = 0.0;

        String sql = "SELECT payment_mode, SUM(amount) FROM payments " +
                "WHERE date(payment_date, 'localtime') = date('now', 'localtime') " +
                "AND entity_type = 'CUSTOMER' " +
                "GROUP BY payment_mode";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String mode = rs.getString(1);
                double amount = rs.getDouble(2);

                if ("CASH".equals(mode)) {
                    cashRecoveries += amount; // Old debt collected today
                } else if ("CASH_REFUND".equals(mode)) {
                    cashRefunds += amount; // Now stored as positive value directly
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Step 3: Compute Net Cash in Drawer
        double netCashInDrawer = todayCashFromSales + cashRecoveries - cashRefunds;

        // Step 4: Populate UI
        lblCloseSales.setText(String.format("Rs. %,.0f", todayTotalSales));
        lblCloseCashIn.setText(String.format("Rs. %,.0f", netCashInDrawer));
        lblCloseKhata.setText(String.format("Rs. %,.0f", todayKhataBilled));
        lblCloseRefunds.setText(String.format("Rs. %,.0f", cashRefunds));
    }

    // --- Navigation Shortcuts via MainController ---
    @FXML
    private void handleQuickPOS() {
        if (MainController.instance != null) MainController.instance.showPOS();
    }

    @FXML
    private void handleQuickPurchase() {
        if (MainController.instance != null) MainController.instance.showPurchaseEntry();
    }

    @FXML
    private void handleQuickExpiry() {
        if (MainController.instance != null) MainController.instance.showExpiry();
    }

    @FXML
    private void handleQuickBackup() {
        if (MainController.instance != null) MainController.instance.showBackup();
    }
}