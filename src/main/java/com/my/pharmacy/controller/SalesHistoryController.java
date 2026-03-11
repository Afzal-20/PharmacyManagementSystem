package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.*;
import com.my.pharmacy.util.CalculationEngine;
import com.my.pharmacy.util.DialogUtil;
import com.my.pharmacy.util.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.my.pharmacy.util.InvoiceGenerator;
import com.my.pharmacy.util.ThermalPrinter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import java.time.LocalDate;
import java.util.Optional;

public class SalesHistoryController {

    private static final Logger log = LoggerFactory.getLogger(SalesHistoryController.class);

    @FXML private DatePicker datePicker;
    @FXML private TableView<Sale> invoiceTable;
    @FXML private TableColumn<Sale, Integer> colInvId;
    @FXML private TableColumn<Sale, String> colInvDate, colInvMode;
    @FXML private TableColumn<Sale, Double> colInvTotal;

    @FXML private TableView<SaleItem> itemTable;
    @FXML private TableColumn<SaleItem, String> colItemName;
    @FXML private TableColumn<SaleItem, Integer> colItemQty, colItemRet;
    @FXML private TableColumn<SaleItem, Double> colItemPrice, colItemDisc;
    @FXML private Button btnProcessReturn;

    private final CustomerDAO customerDAO = new CustomerDAOImpl();

    private final SaleDAO saleDAO = new SaleDAOImpl();

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now()); // Default to today
        setupColumns();
        setupSelectionListener();
        setupRowHighlighter();
        loadInvoices();

        // RBAC Enforcement
        boolean isAdmin = com.my.pharmacy.util.UserSession.getInstance() != null &&
                com.my.pharmacy.util.UserSession.getInstance().getUser() != null &&
                com.my.pharmacy.util.UserSession.getInstance().getUser().isAdmin();
        btnProcessReturn.setVisible(isAdmin);
        btnProcessReturn.setManaged(isAdmin);
    }

    private void setupColumns() {
        colInvId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colInvDate.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        colInvTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colInvMode.setCellValueFactory(new PropertyValueFactory<>("paymentMode"));

        colItemName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colItemQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colItemPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colItemDisc.setCellValueFactory(new PropertyValueFactory<>("discountPercent"));
        colItemRet.setCellValueFactory(new PropertyValueFactory<>("returnedQty"));
    }

    private void setupSelectionListener() {
        invoiceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                itemTable.setItems(FXCollections.observableArrayList(saleDAO.getSaleItemsBySaleId(newSelection.getId())));
            } else {
                itemTable.getItems().clear();
            }
        });
    }

    // Highlights rows red if items have been returned
    private void setupRowHighlighter() {
        itemTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(SaleItem item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.getReturnedQty() > 0) {
                    setStyle("-fx-background-color: #fadbd8;"); // Faint red
                } else {
                    setStyle("");
                }
            }
        });
    }

    @FXML
    private void loadInvoices() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            invoiceTable.setItems(FXCollections.observableArrayList(saleDAO.getSalesByDate(selectedDate)));
            itemTable.getItems().clear();
        }
    }

    @FXML
    private void handleProcessReturn() {
        Sale selectedInvoice = invoiceTable.getSelectionModel().getSelectedItem();
        SaleItem selectedItem = itemTable.getSelectionModel().getSelectedItem();

        if (selectedInvoice == null || selectedItem == null) {
            NotificationService.warn("Please select an invoice and a specific item to return.");
            return;
        }

        int availableToReturn = selectedItem.getQuantity() - selectedItem.getReturnedQty();
        if (availableToReturn <= 0) {
            NotificationService.error("All quantities of this item have already been returned.");
            return;
        }

        showReturnDialog(selectedInvoice, selectedItem, availableToReturn);
    }

    private void showReturnDialog(Sale invoice, SaleItem item, int maxQty) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Process Sales Return");
        dialog.setHeaderText("Returning: " + item.getProductName() + "\nMax available to return: " + maxQty);

        ButtonType processButtonType = new ButtonType("Confirm Return", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(processButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));

        TextField qtyField = new TextField("1");
        TextField reasonField = new TextField();
        reasonField.setPromptText("Optional reason");
        ComboBox<String> methodCombo = new ComboBox<>(FXCollections.observableArrayList("CASH REFUND", "KHATA CREDIT"));

        if (invoice.getCustomerId() == 1) {
            methodCombo.setValue("CASH REFUND");
            methodCombo.setDisable(true); // Walk-ins cannot get Khata credit
        } else {
            methodCombo.setValue("KHATA CREDIT");
        }

        grid.add(new Label("Qty to Return:"), 0, 0); grid.add(qtyField, 1, 0);
        grid.add(new Label("Refund Method:"), 0, 1); grid.add(methodCombo, 1, 1);
        grid.add(new Label("Reason:"), 0, 2); grid.add(reasonField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == processButtonType) {
                try {
                    int qtyToReturn = Integer.parseInt(qtyField.getText());
                    if (qtyToReturn <= 0 || qtyToReturn > maxQty) {
                        NotificationService.error("Quantity must be between 1 and " + maxQty);
                        return null;
                    }

                    // Engine handles the exact math including past discounts applied
                    double refundAmount = CalculationEngine.calculateRefundAmount(item.getUnitPrice(), qtyToReturn, item.getDiscountPercent());

                    saleDAO.processReturn(invoice.getId(), invoice.getCustomerId(), item, qtyToReturn, refundAmount, methodCombo.getValue(), reasonField.getText());

                    // Save silent PDF soft copy of the return receipt
                    InvoiceGenerator.generateReturnReceipt(invoice, item, qtyToReturn, refundAmount, methodCombo.getValue(), reasonField.getText());

                    // Print thermal receipt automatically (user already confirmed via "Process Return")
                    com.my.pharmacy.util.ThermalPrinter.printReturnReceipt(
                            invoice, item, qtyToReturn, refundAmount,
                            methodCombo.getValue(), reasonField.getText());

                    NotificationService.success(String.format("Return processed. Refund: Rs. %.2f", refundAmount));
                    // Refresh Detail Table
                    itemTable.setItems(FXCollections.observableArrayList(saleDAO.getSaleItemsBySaleId(invoice.getId())));

                } catch (NumberFormatException e) {
                    NotificationService.error("Please enter a valid numeric quantity.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleReprintInvoice() {
        Sale selectedInvoice = invoiceTable.getSelectionModel().getSelectedItem();
        if (selectedInvoice == null) {
            NotificationService.warn("Please select an invoice to reprint.");
            return;
        }

        // Make sure items are populated in the Sale object
        selectedInvoice.setItems(saleDAO.getSaleItemsBySaleId(selectedInvoice.getId()));

        // Fetch the customer (ID 1 will be fetched correctly as the Walk-In default from the DB)
        Customer customer = customerDAO.getCustomerById(selectedInvoice.getCustomerId());

        try {
            // ── 1. Save/overwrite the PDF soft copy ──
            String reprintPath = com.my.pharmacy.util.AppPaths.reprintPath(selectedInvoice.getId());
            InvoiceGenerator.generateThermalReceipt(selectedInvoice, customer, reprintPath);

            // ── 2. Ask if they want to print the physical receipt ──
            if (DialogUtil.confirm("Reprint Invoice", "Print receipt?", "Invoice #" + selectedInvoice.getId())) {
                com.my.pharmacy.util.ThermalPrinter.printInvoice(
                        selectedInvoice, customer, "Reprint Invoice #" + selectedInvoice.getId());
            }
        } catch (Exception e) {
            NotificationService.error("Failed to process reprint: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
}