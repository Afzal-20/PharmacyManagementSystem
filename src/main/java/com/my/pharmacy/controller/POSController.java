package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.*;
import com.my.pharmacy.util.ConfigUtil;
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
    @FXML private TableColumn<SaleItem, Double> colCartPrice, colCartTotal;

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

    private String currentMode;

    // Default Walk-in Customer (ID 1 is standard for Counter Sales)
    private final Customer WALK_IN_CUSTOMER = new Customer(1, "Counter Sale (Walk-in)", "", "", "RETAIL", 0.0, "", "");

    @FXML
    public void initialize() {
        this.currentMode = ConfigUtil.getAppMode();
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

        if ("WHOLESALE".equals(currentMode)) {
            colPrice.setText("Price (TP)");
            colPrice.setCellValueFactory(new PropertyValueFactory<>("tradePrice"));
        } else {
            colPrice.setText("Price (RP)");
            colPrice.setCellValueFactory(new PropertyValueFactory<>("retailPrice"));
        }

        colCartName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colCartTotal.setCellValueFactory(new PropertyValueFactory<>("subTotal"));

        productTable.setItems(masterData);
        cartTable.setItems(cartData);
    }

    private void setupCustomerSelector() {
        customerList.clear();

        // Always add Walk-in as the first option
        customerList.add(WALK_IN_CUSTOMER);

        if ("WHOLESALE".equals(currentMode)) {
            List<Customer> wholesaleClients = customerDAO.getAllCustomers().stream()
                    .filter(c -> "WHOLESALE".equals(c.getType()))
                    .toList();
            customerList.addAll(wholesaleClients);
        }

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

        if ("WHOLESALE".equals(currentMode)) {
            openWholesaleDialog(selected);
        } else {
            openRetailDialog(selected);
        }
    }

    private void openWholesaleDialog(Batch selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddToCartWholesale.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Wholesale Item Details");
            stage.initModality(Modality.APPLICATION_MODAL);

            AddToCartWholesaleController controller = loader.getController();
            controller.setBatchData(selected);
            stage.showAndWait();

            if (controller.isConfirmed()) {
                SaleItem item = controller.getCreatedItem();
                if ((item.getQuantity() + item.getBonusQty()) > selected.getQtyOnHand()) {
                    showAlert(Alert.AlertType.ERROR, "Stock Error", "Insufficient stock including bonus units.");
                    return;
                }
                cartData.add(item);
                updateTotal();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openRetailDialog(Batch selected) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Retail Entry");
        dialog.setHeaderText(selected.getProduct().getName());
        dialog.setContentText("Enter Units:");
        dialog.showAndWait().ifPresent(input -> {
            try {
                int qty = Integer.parseInt(input);
                if (qty > selected.getQtyOnHand()) {
                    showAlert(Alert.AlertType.ERROR, "Stock Error", "Insufficient stock.");
                    return;
                }
                SaleItem item = new SaleItem(selected.getProductId(), selected.getBatchId(), qty, selected.getRetailPrice(), 0, 0.0);
                item.setProductName(selected.getProduct().getName());
                cartData.add(item);
                updateTotal();
            } catch (NumberFormatException e) { showAlert(Alert.AlertType.ERROR, "Error", "Invalid quantity."); }
        });
    }

    private void updateTotal() {
        double total = calculateCartTotal();
        totalLabel.setText(String.format("Total: %.2f", total));
        amountPaidField.setText(String.format("%.2f", total));
        calculateBalanceDue();
    }

    private void calculateBalanceDue() {
        try {
            double total = calculateCartTotal();
            String paidText = amountPaidField.getText().trim();
            double paid = paidText.isEmpty() ? 0.0 : Double.parseDouble(paidText);

            Customer c = customerComboBox.getValue();
            boolean isRetail = (c == null || "RETAIL".equals(c.getType()));

            if (isRetail) {
                if (paid >= total) {
                    balanceDueLabel.setText(String.format("Change Due: %.2f", paid - total));
                } else {
                    balanceDueLabel.setText(String.format("Short Amount: %.2f", total - paid));
                }
            } else { // Wholesale
                double balance = total - paid;
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

    private double calculateCartTotal() { return cartData.stream().mapToDouble(SaleItem::getSubTotal).sum(); }

    @FXML
    private void handleCheckout() {
        if (cartData.isEmpty()) return;

        Customer c = customerComboBox.getValue();
        boolean isRetail = (c == null || "RETAIL".equals(c.getType()));
        int customerId = (c != null) ? c.getId() : 1;

        try {
            double total = calculateCartTotal();
            double paid = Double.parseDouble(amountPaidField.getText());

            // Strict Retail Validation
            if (isRetail && paid < total) {
                showAlert(Alert.AlertType.WARNING, "Payment Error", "Retail/Walk-in customers must pay the full amount.");
                return;
            }

            // DB Logic: Retail leaves no Khata trace, Wholesale calculates difference
            double dbBalanceDue = isRetail ? 0.0 : (total - paid);
            double dbAmountPaid = isRetail ? total : paid;

            Sale sale = new Sale(0, new Timestamp(System.currentTimeMillis()), total, "CASH",
                    customerId, 1, dbAmountPaid, dbBalanceDue);

            sale.setItems(cartData);
            saleDAO.saveSale(sale);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Sale Completed! Stock and Ledger updated.");

            cartData.clear();
            loadStockData();
            updateTotal();

            if ("WHOLESALE".equals(currentMode)) {
                setupCustomerSelector();
            }
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