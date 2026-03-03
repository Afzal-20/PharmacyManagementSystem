package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.Batch;
import com.my.pharmacy.model.Sale;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.time.LocalDate;
import java.util.List;

public class DashboardController {

    @FXML private Label lblTodaySales;
    @FXML private Label lblLowStock;
    @FXML private Label lblExpiry;
    @FXML private Label lblDealers;

    private final SaleDAO saleDAO = new SaleDAOImpl();
    private final BatchDAO batchDAO = new BatchDAOImpl();
    private final DealerDAO dealerDAO = new DealerDAOImpl();

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    private void refreshDashboard() {
        // 1. Calculate Today's Sales
        List<Sale> todaySales = saleDAO.getSalesByDate(LocalDate.now());
        double totalRevenue = todaySales.stream().mapToDouble(Sale::getTotalAmount).sum();
        lblTodaySales.setText(String.format("Rs. %,.0f", totalRevenue));

        // 2. Analyze Inventory
        List<Batch> allBatches = batchDAO.getAllBatches();
        long lowStockCount = allBatches.stream().filter(b -> b.getQtyOnHand() < 10).count();
        lblLowStock.setText(String.valueOf(lowStockCount));

        LocalDate sixMonthsFromNow = LocalDate.now().plusMonths(6);
        long expiryCount = allBatches.stream().filter(b -> {
            try {
                return LocalDate.parse(b.getExpiryDate()).isBefore(sixMonthsFromNow);
            } catch (Exception e) { return false; }
        }).count();
        lblExpiry.setText(String.valueOf(expiryCount));

        // 3. Count Dealers
        int dealerCount = dealerDAO.getAllDealers().size();
        lblDealers.setText(String.valueOf(dealerCount));
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
}