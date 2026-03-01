package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.*;
import com.my.pharmacy.util.CalculationEngine;
import com.my.pharmacy.util.InvoiceGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

public class POSController {

    @FXML private TextField searchField;
    @FXML private TableView<Batch> productTable;
    @FXML private TableColumn<Batch, String> colName, colGeneric, colBatch, colExpiry;
    @FXML private TableColumn<Batch, Double> colPrice;
    @FXML private TableColumn<Batch, Integer> colStock;

    @FXML private TableView<SaleItem> cartTable;
    @FXML private TableColumn<SaleItem, String> colCartName;
    @FXML private TableColumn<SaleItem, Integer> colCartQty;
    @FXML private TableColumn<SaleItem, Double> colCartPrice, colCartDisc, colCartTotal;

    @FXML private Label totalLabel;
    @FXML private TextField amountPaidField;
    @FXML private Label balanceDueLabel;

    @FXML private HBox customerSection;
    @FXML private ComboBox<Customer> customerComboBox;

    private final BatchDAO batchDAO = new BatchDAOImpl();
    private final SaleDAO saleDAO = new SaleDAOImpl();
    private final CustomerDAO customerDAO = new CustomerDAOImpl();

    private final ObservableList<Batch> masterData = FXCollections.observableArrayList();
    private final ObservableList<SaleItem> cartData = FXCollections.observableArrayList();
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();

    private final Customer WALK_IN_CUSTOMER = new Customer(1, "Counter Sale (Walk-in)", "", "", "REGULAR", 0.0, "", "", "");

    @FXML
    public void initialize() {
        setupTableColumns();
        loadStockData();
        setupSearchFilter();
        setupCustomerSelector();
        setupPaymentListeners();
    }

    private void setupTableColumns() {
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getProduct().getName()));
        colGeneric.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getProduct().getGenericName()));
        colBatch.setCellValueFactory(new PropertyValueFactory<>("batchNo"));
        colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));

        // Strictly bound to Wholesale Trade Price
        colPrice.setText("Price (TP)");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("tradePrice"));

        colCartName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colCartDisc.setCellValueFactory(new PropertyValueFactory<>("discountAmount"));
        colCartTotal.setCellValueFactory(new PropertyValueFactory<>("subTotal"));

        productTable.setItems(masterData);
        cartTable.setItems(cartData);
    }

    private void setupCustomerSelector() {
        customerList.clear();
        customerList.add(WALK_IN_CUSTOMER);

        List<Customer> allClients = customerDAO.getAllCustomers().stream()
                .filter(c -> c.getId() != 1)
                .toList();
        customerList.addAll(allClients);

        customerComboBox.setItems(customerList);
        customerComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Customer c) {
                if (c == null) return "";
                if (c.getId() == 1) return c.getName();
                return c.getName() + " (Khata: " + c.getCurrentBalance() + ")";
            }
            @Override public Customer fromString(String s) { return null; }
        });

        customerComboBox.getSelectionModel().selectFirst();
    }

    private void setupPaymentListeners() {
        amountPaidField.textProperty().addListener((obs, oldVal, newVal) -> calculateBalanceDue());
        customerComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> calculateBalanceDue());
    }

    @FXML
    private void loadStockData() {
        masterData.setAll(batchDAO.getAllBatches());
    }

    private void setupSearchFilter() {
        FilteredList<Batch> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(batch -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String query = newVal.toLowerCase().trim();
                return batch.getProduct().getName().toLowerCase().contains(query) ||
                        batch.getProduct().getGenericName().toLowerCase().contains(query);
            });
        });
        SortedList<Batch> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(productTable.comparatorProperty());
        productTable.setItems(sortedData);
    }

    @FXML
    private void handleAddToCart() {
        Batch selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Mode check removed. Directly opens the wholesale box/discount dialog.
        openAddToCartDialog(selected);
    }

    private void openAddToCartDialog(Batch selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddToCart.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Item Details");
            stage.initModality(Modality.APPLICATION_MODAL);

            AddToCartController controller = loader.getController();
            controller.setBatchData(selected);
            stage.showAndWait();

            if (controller.isConfirmed()) {
                SaleItem item = controller.getCreatedItem();
                cartData.add(item);
                updateTotal();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void updateTotal() {
        double total = calculateCartTotal();
        totalLabel.setText(String.format("Total: %.2f", total));
        amountPaidField.setText(String.format("%.2f", total));
        calculateBalanceDue();
    }

    private double calculateCartTotal() {
        return CalculationEngine.calculateGrandTotal(cartData.stream().map(SaleItem::getSubTotal).toList());
    }

    private void calculateBalanceDue() {
        try {
            double total = calculateCartTotal();
            String paidText = amountPaidField.getText().trim();
            double paid = paidText.isEmpty() ? 0.0 : Double.parseDouble(paidText);

            Customer c = customerComboBox.getValue();
            boolean isWalkIn = (c == null || c.getId() == 1);

            if (isWalkIn) {
                if (paid >= total) {
                    balanceDueLabel.setText(String.format("Change Due: %.2f", CalculationEngine.calculateChangeDue(total, paid)));
                } else {
                    balanceDueLabel.setText(String.format("Short Amount: %.2f", CalculationEngine.calculateBalanceDue(total, paid)));
                }
            } else {
                double balance = CalculationEngine.calculateBalanceDue(total, paid);
                if (balance > 0) {
                    balanceDueLabel.setText(String.format("Add to Khata: %.2f", balance));
                } else if (balance < 0) {
                    balanceDueLabel.setText(String.format("Advance Payment: %.2f", Math.abs(balance)));
                } else {
                    balanceDueLabel.setText("Fully Paid");
                }
            }
        } catch (NumberFormatException e) {
            balanceDueLabel.setText("Invalid Amount");
        }
    }

    @FXML
    private void handleCheckout() {
        if (cartData.isEmpty()) return;

        Customer c = customerComboBox.getValue();
        boolean isWalkIn = (c == null || c.getId() == 1);
        int customerId = (c != null) ? c.getId() : 1;

        try {
            double total = calculateCartTotal();
            double paid = Double.parseDouble(amountPaidField.getText());

            if (isWalkIn && paid < total) {
                showAlert(Alert.AlertType.WARNING, "Payment Error", "Walk-in customers must pay the full amount.");
                return;
            }

            double dbBalanceDue = isWalkIn ? 0.0 : CalculationEngine.calculateBalanceDue(total, paid);
            double dbAmountPaid = isWalkIn ? total : paid;

            Sale sale = new Sale(0, new Timestamp(System.currentTimeMillis()), total, "CASH",
                    customerId, 1, dbAmountPaid, dbBalanceDue);

            sale.setItems(cartData);
            saleDAO.saveSale(sale);

            String desktopPath = System.getProperty("user.home") + "/Desktop/Invoice_" + sale.getId() + ".pdf";
            InvoiceGenerator.generateThermalReceipt(sale, c, desktopPath);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Sale Completed! Stock and Ledger updated.");

            cartData.clear();
            loadStockData();
            updateTotal();
            setupCustomerSelector();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please verify the Amount Paid is a valid number.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleAddNewCustomer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddCustomerDialog.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Register New Client");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            setupCustomerSelector();
        } catch (IOException e) { e.printStackTrace(); }
    }
}