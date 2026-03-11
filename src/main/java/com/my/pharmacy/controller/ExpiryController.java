package com.my.pharmacy.controller;

import com.my.pharmacy.dao.BatchDAO;
import com.my.pharmacy.dao.BatchDAOImpl;
import com.my.pharmacy.model.Batch;
import com.my.pharmacy.util.DialogUtil;
import com.my.pharmacy.util.NotificationService;
import com.my.pharmacy.util.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ExpiryController {

    @FXML private TableView<Batch> expiryTable;
    @FXML private TableColumn<Batch, String>  colProduct, colBatchNo, colExpiry, colDaysLeft, colStatus;
    @FXML private TableColumn<Batch, Integer> colStock;
    @FXML private TableColumn<Batch, Double>  colTradePrice;
    @FXML private Label lblExpired, lblCritical, lblWarning, lblTotal;
    @FXML private Button btnWriteOff;

    private static final int ALERT_DAYS = 90;
    private final BatchDAO batchDAO = new BatchDAOImpl();
    private final ObservableList<Batch> expiryData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        boolean isAdmin = UserSession.getInstance() != null &&
                UserSession.getInstance().getUser().isAdmin();
        btnWriteOff.setVisible(isAdmin);
        btnWriteOff.setManaged(isAdmin);
        setupColumns();
        setupRowColoring();
        loadExpiryData();
    }

    private void setupColumns() {
        colProduct.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduct().getName()));
        colBatchNo.setCellValueFactory(new PropertyValueFactory<>("batchNo"));
        colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
        colTradePrice.setCellValueFactory(new PropertyValueFactory<>("tradePrice"));
        colDaysLeft.setCellValueFactory(d -> {
            long days = daysUntilExpiry(d.getValue().getExpiryDate());
            if (days < 0) return new SimpleStringProperty("EXPIRED (" + Math.abs(days) + "d ago)");
            return new SimpleStringProperty(days + " days");
        });
        colStatus.setCellValueFactory(d -> {
            long days = daysUntilExpiry(d.getValue().getExpiryDate());
            if (days < 0)   return new SimpleStringProperty("🔴  EXPIRED");
            if (days <= 30) return new SimpleStringProperty("🔴  Critical");
            if (days <= 60) return new SimpleStringProperty("🟠  Warning");
            return new SimpleStringProperty("🟡  Watch");
        });
        expiryTable.setItems(expiryData);
    }

    private void setupRowColoring() {
        expiryTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Batch item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setStyle(""); return; }
                long days = daysUntilExpiry(item.getExpiryDate());
                if (days < 0)        setStyle("-fx-background-color: #f5b7b1;");
                else if (days <= 30) setStyle("-fx-background-color: #fadbd8;");
                else if (days <= 60) setStyle("-fx-background-color: #fdebd0;");
                else                 setStyle("-fx-background-color: #fef9e7;");
            }
        });
    }

    @FXML
    private void loadExpiryData() {
        List<Batch> all = batchDAO.getAllBatches();
        LocalDate cutoff = LocalDate.now().plusDays(ALERT_DAYS);
        List<Batch> expiring = all.stream().filter(b -> {
            try { return !LocalDate.parse(b.getExpiryDate()).isAfter(cutoff); }
            catch (Exception e) { return false; }
        }).sorted((a, b2) -> {
            try { return LocalDate.parse(a.getExpiryDate()).compareTo(LocalDate.parse(b2.getExpiryDate())); }
            catch (Exception e) { return 0; }
        }).toList();

        expiryData.setAll(expiring);

        long expired  = expiring.stream().filter(b -> daysUntilExpiry(b.getExpiryDate()) <  0).count();
        long critical = expiring.stream().filter(b -> { long d = daysUntilExpiry(b.getExpiryDate()); return d >= 0 && d <= 30; }).count();
        long warning  = expiring.stream().filter(b -> { long d = daysUntilExpiry(b.getExpiryDate()); return d > 30 && d <= 60; }).count();

        lblExpired.setText(String.valueOf(expired));
        lblCritical.setText(String.valueOf(critical));
        lblWarning.setText(String.valueOf(warning));
        lblTotal.setText(String.valueOf(expiring.size()));
    }

    @FXML
    private void handleWriteOff() {
        Batch selected = expiryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationService.warn("Please select a batch to write off.");
            return;
        }
        if (selected.getQtyOnHand() == 0) {
            NotificationService.info("This batch already has zero stock. No write-off needed.");
            return;
        }

        boolean confirmed = DialogUtil.confirm(
                "Confirm Write-Off",
                "Write off " + selected.getProduct().getName() + " (Batch: " + selected.getBatchNo() + ")?",
                selected.getQtyOnHand() + " units in stock  |  Expired: " + selected.getExpiryDate()
        );
        if (confirmed) {
            int userId = UserSession.getInstance().getUser().getId();
            batchDAO.adjustStockWithAudit(selected.getId(), selected.getQtyOnHand(), 0,
                    "WRITE-OFF — Expired batch (" + selected.getExpiryDate() + ")", userId);
            NotificationService.success(selected.getProduct().getName() + " (Batch: " + selected.getBatchNo() + ") written off.");
            loadExpiryData();
        }
    }

    private long daysUntilExpiry(String expiryDateStr) {
        try { return ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(expiryDateStr)); }
        catch (Exception e) { return Long.MAX_VALUE; }
    }
}
