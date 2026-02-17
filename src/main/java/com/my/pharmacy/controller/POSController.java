package com.my.pharmacy.controller;

import com.my.pharmacy.dao.BatchDAO;
import com.my.pharmacy.dao.SaleDAO;
import com.my.pharmacy.dao.BatchDAOImpl;
import com.my.pharmacy.dao.SaleDAOImpl;
import com.my.pharmacy.model.Batch;
import com.my.pharmacy.model.Sale;
import com.my.pharmacy.model.SaleItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Timestamp;
import java.util.List;

public class POSController {

    // --- FXML UI Components ---
    @FXML private TextField searchField;
    @FXML private TableView<Batch> productTable;
    @FXML private TableColumn<Batch, String> colName;
    @FXML private TableColumn<Batch, String> colGeneric; // NEW
    @FXML private TableColumn<Batch, String> colBatch;
    @FXML private TableColumn<Batch, String> colExpiry;
    @FXML private TableColumn<Batch, Double> colPrice;
    @FXML private TableColumn<Batch, Integer> colStock;

    @FXML private TableView<SaleItem> cartTable;
    @FXML private TableColumn<SaleItem, String> colCartName;
    @FXML private TableColumn<SaleItem, Integer> colCartQty;
    @FXML private TableColumn<SaleItem, Double> colCartPrice;
    @FXML private TableColumn<SaleItem, Double> colCartTotal;

    @FXML private Label totalLabel;
    @FXML private Button checkoutButton;

    // --- Data & DAOs ---
    private BatchDAO batchDAO = new BatchDAOImpl();
    private SaleDAO saleDAO = new SaleDAOImpl();

    private ObservableList<Batch> masterData = FXCollections.observableArrayList();
    private ObservableList<SaleItem> cartData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadStockData();
        setupSearchFilter();
    }

    private void setupTableColumns() {
        // Product Table (Left Side)
        // Note: 'product' is a nested object in Batch, so we need a custom cell value factory
        colName.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProduct().getName()));

        colGeneric.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProduct().getGenericName()));

        colBatch.setCellValueFactory(new PropertyValueFactory<>("batchNo"));
        colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("retailPrice")); // Default to Retail for now
        colStock.setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));

        // Cart Table (Right Side)
        colCartName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colCartTotal.setCellValueFactory(new PropertyValueFactory<>("subTotal"));

        productTable.setItems(masterData);
        cartTable.setItems(cartData);
    }
    @FXML
    private void loadStockData() {
        masterData.clear();
        List<Batch> batches = batchDAO.getAllBatches();
        masterData.addAll(batches);
    }

    private void setupSearchFilter() {
        FilteredList<Batch> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(batch -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String query = newValue.toLowerCase().trim();

                // Access product details safely
                String name = batch.getProduct().getName().toLowerCase();
                String generic = (batch.getProduct().getGenericName() != null)
                        ? batch.getProduct().getGenericName().toLowerCase()
                        : "";

                // 1. Check for Panadol (Name) or Paracetamol (Generic)
                if (name.contains(query) || generic.contains(query)) {
                    return true;
                }

                // 2. Fuzzy Match (Handles typos like "Pandol" or "Pracetamol")
                if (isSmartMatch(query, name) || isSmartMatch(query, generic)) {
                    return true;
                }

                return false;
            });
        });

        SortedList<Batch> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(productTable.comparatorProperty());
        productTable.setItems(sortedData);
    }

    // --- Actions ---

    @FXML
    private void handleAddToCart() {
        Batch selectedBatch = productTable.getSelectionModel().getSelectedItem();
        if (selectedBatch == null) {
            showAlert("No Selection", "Please select a medicine to add.");
            return;
        }

        // Ask for Quantity
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Quantity");
        dialog.setHeaderText("Enter Quantity for " + selectedBatch.getProduct().getName());
        dialog.setContentText("Qty:");

        dialog.showAndWait().ifPresent(qtyStr -> {
            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty > selectedBatch.getQtyOnHand()) {
                    showAlert("Stock Warning", "Not enough stock! Available: " + selectedBatch.getQtyOnHand());
                    return;
                }

                addBatchToCart(selectedBatch, qty);
                updateTotal();

            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number.");
            }
        });
    }

    private void addBatchToCart(Batch batch, int qty) {
        // Create SaleItem (Using RETAIL Price by default for now)
        // Params: ProductId, BatchId, Qty, Price, Bonus, Discount
        SaleItem item = new SaleItem(
                batch.getProductId(),
                batch.getBatchId(),
                qty,
                batch.getRetailPrice(),
                0, // No bonus yet
                0.0 // No discount yet
        );
        item.setProductName(batch.getProduct().getName()); // For Display

        cartData.add(item);
    }

    @FXML
    private void handleCheckout() {
        if (cartData.isEmpty()) {
            showAlert("Empty Cart", "Cannot checkout an empty cart.");
            return;
        }

        // Create Sale Object
        Sale sale = new Sale();
        sale.setTotalAmount(calculateCartTotal());
        sale.setPaymentMode("CASH");
        sale.setCustomerId(1); // Default Customer (Walk-in)
        sale.setSalesmanId(1); // Default User (Admin)
        sale.setSaleDate(new Timestamp(System.currentTimeMillis()));

        // Add items to sale
        sale.setItems(cartData);

        // Save to Database
        saleDAO.saveSale(sale);

        // Success & Reset
        showAlert("Success", "Sale Completed Successfully!");
        cartData.clear();
        loadStockData(); // Refresh stock levels
        updateTotal();
    }

    private void updateTotal() {
        totalLabel.setText(String.format("Total: %.2f", calculateCartTotal()));
    }

    private double calculateCartTotal() {
        return cartData.stream().mapToDouble(SaleItem::getSubTotal).sum();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    // --- SMART SEARCH HELPERS ---

    // 1. Main logic: Decides if a product matches the search query
    private boolean isSmartMatch(String query, String text) {
        if (text == null || query == null || query.isEmpty()) return false;

        text = text.toLowerCase();
        query = query.toLowerCase();

        // Level 1: Exact Substring (Fastest & Most Common)
        if (text.contains(query)) return true;

        // Level 2: Fuzzy Word Matching (Handles Typos like "Pandol" -> "Panadol")
        // We split the product name into words (e.g., "Panadol Extra" -> ["Panadol", "Extra"])
        String[] textWords = text.split(" ");
        for (String word : textWords) {
            // If the query is similar to any single word in the name
            if (calculateLevenshteinDistance(query, word) <= getTolerance(query.length())) {
                return true;
            }
        }

        return false;
    }

    // 2. Tolerance: Shorter words need stricter matching, longer words allow more typos
    private int getTolerance(int queryLength) {
        if (queryLength <= 3) return 0; // "Pan" must be exact
        if (queryLength <= 6) return 1; // "Pando" (5 chars) allows 1 mistake
        return 2;                       // "Paracitamol" allows 2 mistakes
    }

    // 3. The Math: Calculates how many edits (typos) valid to change one word to another
    private int calculateLevenshteinDistance(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= y.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= x.length(); i++) {
            for (int j = 1; j <= y.length(); j++) {
                int cost = (x.charAt(i - 1) == y.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[x.length()][y.length()];
    }
}