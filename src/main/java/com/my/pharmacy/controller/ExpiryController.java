package com.my.pharmacy.controller;

import com.my.pharmacy.dao.BatchDAO;
import com.my.pharmacy.dao.BatchDAOImpl;
import com.my.pharmacy.model.Batch;
import com.my.pharmacy.util.UserSession;
import javafx.beans.property.SimpleObjectProperty;
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
    @FXML private TableColumn<Batch, String>  colProduct;
    @FXML private TableColumn<Batch, String>  colBatchNo;
    @FXML private TableColumn<Batch, String>  colExpiry;
    @FXML private TableColumn<Batch, Integer> colStock;
    @FXML private TableColumn<Batch, Double>  colTradePrice;
    @FXML private TableColumn<Batch, String>  colDaysLeft;
    @FXML private TableColumn<Batch, String>  colStatus;

    @FXML private Label lblExpired;
    @FXML private Label lblCritical;
    @FXML private Label lblWarning;
    @FXML private Label lblTotal;
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
        colProduct.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getProduct().getName()));
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
                if (item == null || empty) {
                    setStyle("");
                    return;
                }
                long days = daysUntilExpiry(item.getExpiryDate());
                if (days < 0)   setStyle("-fx-background-color: #f5b7b1;"); // red — expired
                else if (days <= 30) setStyle("-fx-background-color: #fadbd8;"); // light red
                else if (days <= 60) setStyle("-fx-background-color: #fdebd0;"); // orange
                else                 setStyle("-fx-background-color: #fef9e7;"); // yellow
            }
        });
    }

    @FXML
    private void loadExpiryData() {
        List<Batch> all = batchDAO.getAllBatches();
        LocalDate cutoff = LocalDate.now().plusDays(ALERT_DAYS);

        List<Batch> expiring = all.stream().filter(b -> {
            try {
                return !LocalDate.parse(b.getExpiryDate()).isAfter(cutoff);
            } catch (Exception e) { return false; }
        }).sorted((a, b2) -> {
            try {
                return LocalDate.parse(a.getExpiryDate())
                        .compareTo(LocalDate.parse(b2.getExpiryDate()));
            } catch (Exception e) { return 0; }
        }).toList();

        expiryData.setAll(expiring);

        // Update summary counts
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
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Please select a batch from the table to write off.");
            return;
        }
        if (selected.getQtyOnHand() == 0) {
            showAlert(Alert.AlertType.INFORMATION, "Already Empty",
                    "This batch already has zero stock. No write-off needed.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Write-Off");
        confirm.setHeaderText("Write off: " + selected.getProduct().getName() +
                " — Batch: " + selected.getBatchNo());
        confirm.setContentText(
                "Current stock: " + selected.getQtyOnHand() + " units\n" +
                "Expiry: " + selected.getExpiryDate() + "\n\n" +
                "This will set stock to 0 and log the write-off in the audit trail.\n\n" +
                "Proceed?"
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;

            int userId = UserSession.getInstance().getUser().getId();
            batchDAO.adjustStockWithAudit(
                    selected.getId(),
                    selected.getQtyOnHand(),
                    0,
                    "WRITE-OFF — Expired batch (" + selected.getExpiryDate() + ")",
                    userId
            );

            showAlert(Alert.AlertType.INFORMATION, "Write-Off Complete",
                    selected.getProduct().getName() + " (Batch: " + selected.getBatchNo() +
                    ") has been written off. Audit record saved.");
            loadExpiryData();
        });
    }

    private long daysUntilExpiry(String expiryDateStr) {
        try {
            return ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(expiryDateStr));
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
