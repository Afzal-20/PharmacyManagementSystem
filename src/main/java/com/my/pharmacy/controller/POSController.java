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
    @FXML private HBox customerSection;
    @FXML private ComboBox<Customer> customerComboBox;

    private final BatchDAO batchDAO = new BatchDAOImpl();
    private final SaleDAO saleDAO = new SaleDAOImpl();
    private final CustomerDAO customerDAO = new CustomerDAOImpl();

    private final ObservableList<Batch> masterData = FXCollections.observableArrayList();
    private final ObservableList<SaleItem> cartData = FXCollections.observableArrayList();
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();

    private String currentMode;

    @FXML
    public void initialize() {
        this.currentMode = ConfigUtil.getAppMode();
        setupTableColumns();
        loadStockData();
        setupSearchFilter();
        setupCustomerSelector();
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
        if ("RETAIL".equals(currentMode)) {
            customerSection.setVisible(false);
            customerSection.setManaged(false);
            return; // Exit early for retail
        }

        // Filter to only show wholesale clients (customers who buy from you)
        List<Customer> all = customerDAO.getAllCustomers();
        List<Customer> wholesaleClients = all.stream()
                .filter(c -> "WHOLESALE".equals(c.getType()))
                .toList();

        customerList.setAll(wholesaleClients);
        customerComboBox.setItems(customerList);

        customerComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Customer c) { return c == null ? "" : c.getName() + " (" + c.getPhone() + ")"; }
            @Override public Customer fromString(String s) { return null; }
        });

        if (!customerList.isEmpty()) customerComboBox.getSelectionModel().select(0);
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
                    showAlert("Stock Error", "Insufficient stock including bonus units.");
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
                    showAlert("Stock Error", "Insufficient stock.");
                    return;
                }
                SaleItem item = new SaleItem(selected.getProductId(), selected.getBatchId(), qty, selected.getRetailPrice(), 0, 0.0);
                item.setProductName(selected.getProduct().getName());
                cartData.add(item);
                updateTotal();
            } catch (NumberFormatException e) { showAlert("Error", "Invalid quantity."); }
        });
    }

    @FXML
    private void handleCheckout() {
        if (cartData.isEmpty()) return;
        Customer c = customerComboBox.getSelectionModel().getSelectedItem();
        int customerId = (c != null) ? c.getId() : 1;

        Sale sale = new Sale();
        sale.setTotalAmount(calculateCartTotal());
        sale.setPaymentMode("CASH");
        sale.setCustomerId(customerId);
        sale.setSalesmanId(1);
        sale.setSaleDate(new Timestamp(System.currentTimeMillis()));
        sale.setItems(cartData);

        saleDAO.saveSale(sale);
        showAlert("Success", "Sale Completed!");
        cartData.clear();
        loadStockData();
        updateTotal();
    }

    private void updateTotal() { totalLabel.setText(String.format("Total: %.2f", calculateCartTotal())); }
    private double calculateCartTotal() { return cartData.stream().mapToDouble(SaleItem::getSubTotal).sum(); }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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
            stage.setTitle("Register New Dealer");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            AddCustomerController controller = loader.getController();
            if (controller.getSavedCustomer() != null) {
                customerList.setAll(customerDAO.getAllCustomers());
                customerComboBox.getSelectionModel().select(controller.getSavedCustomer());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}