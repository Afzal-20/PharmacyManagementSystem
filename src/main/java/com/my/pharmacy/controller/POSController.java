package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.*;
import com.my.pharmacy.service.CartService;
import com.my.pharmacy.util.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Timestamp;
import com.my.pharmacy.util.TimeUtil;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class POSController {

    private static final Logger log = LoggerFactory.getLogger(POSController.class);

    @FXML private TextField searchField;
    @FXML private TableView<Batch> productTable;
    @FXML private TableColumn<Batch, String> colName, colGeneric, colBatch, colExpiry;
    @FXML private TableColumn<Batch, Double> colPrice;
    @FXML private TableColumn<Batch, Integer> colStock;

    @FXML private TableView<SaleItem> cartTable;
    @FXML private TableColumn<SaleItem, String> colCartName;
    @FXML private TableColumn<SaleItem, Integer> colCartQty;
    @FXML private TableColumn<SaleItem, Double> colCartPrice, colCartDisc, colCartTotal;
    @FXML private Button btnRemoveFromCart;

    @FXML private Label totalLabel;
    @FXML private TextField amountPaidField;
    @FXML private Label balanceDueLabel;
    @FXML private HBox customerSection;
    @FXML private ComboBox<Customer> customerComboBox;

    private final BatchDAO batchDAO = new BatchDAOImpl();
    private final SaleDAO saleDAO = new SaleDAOImpl();
    private final CustomerDAO customerDAO = new CustomerDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();

    private final ObservableList<Batch> masterData = FXCollections.observableArrayList();
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private final CartService cartService = new CartService();

    private final Customer WALK_IN_CUSTOMER = new Customer(1, "Counter Sale (Walk-in)", "", "", "REGULAR", "");

    @FXML
    public void initialize() {
        log.info("POSController initializing");
        setupTableColumns();
        loadStockData();
        setupSearchFilter();
        setupCustomerSelector(null);
        setupPaymentListeners();
        log.info("POSController ready");

        // Register screen-specific shortcuts via global ShortcutManager registry
        ShortcutManager.setCheckoutAction(this::handleCheckout);
        ShortcutManager.setAddToCartAction(this::handleAddToCart);
    }

    private void setupTableColumns() {
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getProduct().getName()));
        colGeneric.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getProduct().getGenericName()));
        colBatch.setCellValueFactory(new PropertyValueFactory<>("batchNo"));
        colExpiry.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
        colPrice.setText("Price (TP)");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("tradePrice"));

        colCartName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colCartDisc.setCellValueFactory(new PropertyValueFactory<>("discountAmount"));
        colCartTotal.setCellValueFactory(new PropertyValueFactory<>("subTotal"));

        productTable.setItems(masterData);
        cartTable.setItems(cartService.getCartData());

        // Disable remove button when no cart row is selected
        btnRemoveFromCart.setDisable(true);
        cartTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> btnRemoveFromCart.setDisable(selected == null));
    }

    private void setupCustomerSelector(Integer selectId) {
        customerList.clear();
        customerList.add(WALK_IN_CUSTOMER);
        List<Customer> allClients = customerDAO.getAllCustomers().stream()
                .filter(c -> c.getId() != 1).toList();
        customerList.addAll(allClients);
        customerComboBox.setItems(customerList);
        customerComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Customer c) {
                if (c == null) return "";
                if (c.getId() == 1) return c.getName();
                // FIX: use dynamic balance calculated live from the payments ledger,
                // not the stored current_balance which can drift out of sync
                double balance = paymentDAO.getDynamicCustomerBalance(c.getId());
                return c.getName() + " (Khata: " + String.format("%.0f", balance) + ")";
            }
            @Override public Customer fromString(String s) { return null; }
        });
        if (selectId != null) {
            customerList.stream().filter(c -> c.getId() == selectId).findFirst()
                    .ifPresent(c -> customerComboBox.getSelectionModel().select(c));
        } else {
            customerComboBox.getSelectionModel().selectFirst();
        }
    }

    private void setupPaymentListeners() {
        amountPaidField.textProperty().addListener((obs, oldVal, newVal) -> calculateBalanceDue());
        customerComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> calculateBalanceDue());
    }

    @FXML
    private void loadStockData() {
        log.debug("Loading stock data");
        masterData.setAll(batchDAO.getAllBatches());
        log.debug("Loaded {} batches", masterData.size());
    }

    private void setupSearchFilter() {
        FilteredList<Batch> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(batch -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String query = newVal.toLowerCase().trim();
                String productName = batch.getProduct().getName().toLowerCase();
                String genericName = batch.getProduct().getGenericName().toLowerCase();
                return FuzzySearchUtil.isFuzzyMatch(query, productName) ||
                        FuzzySearchUtil.isFuzzyMatch(query, genericName);
            });
        });
        SortedList<Batch> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(productTable.comparatorProperty());
        productTable.setItems(sortedData);
    }

    @FXML
    private void handleAddToCart() {
        Batch selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            log.debug("Add to cart attempted with no product selected");
            return;
        }
        openAddToCartDialog(selected);
    }

    private void openAddToCartDialog(Batch selected) {
        // ── Expiry Check ─────────────────────────────────────────────────────
        try {
            LocalDate expiry = LocalDate.parse(selected.getExpiryDate());
            LocalDate today  = TimeUtil.today();

            if (!expiry.isAfter(today)) {
                // Hard block — expired medicine cannot be sold
                NotificationService.error("❌ Cannot sell expired medicine — "
                        + selected.getProduct().getName()
                        + " expired on " + selected.getExpiryDate());
                log.warn("Blocked sale of expired batch: {} expiry={}", selected.getBatchNo(), selected.getExpiryDate());
                return;
            }

            long daysLeft = ChronoUnit.DAYS.between(today, expiry);
            int warnDays = 0;
            try {
                warnDays = Integer.parseInt(ConfigUtil.get("expiry.warn_days", "30"));
            } catch (NumberFormatException ignored) { warnDays = 30; }

            if (daysLeft <= warnDays) {
                // Soft warning — cashier can still choose to sell
                boolean proceed = DialogUtil.confirm(
                        "⚠ Expiry Warning",
                        selected.getProduct().getName() + " expires in " + daysLeft + " day(s)",
                        "Expiry date: " + selected.getExpiryDate() + ". Sell anyway?"
                );
                if (!proceed) return;
                log.warn("Cashier chose to sell near-expiry batch: {} daysLeft={}", selected.getBatchNo(), daysLeft);
            }
        } catch (Exception e) {
            log.warn("Could not parse expiry date for batch {}: {}", selected.getBatchNo(), e.getMessage());
        }
        // ─────────────────────────────────────────────────────────────────────

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
                SaleItem newItem = controller.getCreatedItem();
                String error = cartService.addOrMerge(newItem, selected);
                if (error != null) {
                    NotificationService.error(error);
                    return;
                }
                cartTable.refresh();
                updateTotal();
            }
        } catch (IOException e) {
            log.error("Failed to open AddToCart dialog: {}", e.getMessage(), e);
            NotificationService.error("Could not open item dialog.");
        }
    }

    private void updateTotal() {
        double total = cartService.getGrandTotal();
        totalLabel.setText(String.format("Total: %.2f", total));
        amountPaidField.setText(String.format("%.2f", total));
        calculateBalanceDue();
    }

    private void calculateBalanceDue() {
        try {
            double total = cartService.getGrandTotal();
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
                if (balance > 0) balanceDueLabel.setText(String.format("Add to Khata: %.2f", balance));
                else if (balance < 0) balanceDueLabel.setText(String.format("Advance Payment: %.2f", Math.abs(balance)));
                else balanceDueLabel.setText("Fully Paid");
            }
        } catch (NumberFormatException e) {
            balanceDueLabel.setText("Invalid Amount");
        }
    }

    @FXML
    private void handleCheckout() {
        if (cartService.isEmpty()) {
            log.debug("Checkout attempted with empty cart");
            return;
        }

        Customer currentCustomer = customerComboBox.getValue();
        int customerId = (currentCustomer != null) ? currentCustomer.getId() : 1;
        boolean isWalkIn = (customerId == 1);

        try {
            double total = cartService.getGrandTotal();
            double paid = Double.parseDouble(amountPaidField.getText());

            if (isWalkIn && paid < total) {
                NotificationService.warn("Walk-in customers must pay the full amount.");
                return;
            }

            double dbBalanceDue = isWalkIn ? 0.0 : CalculationEngine.calculateBalanceDue(total, paid);
            double dbAmountPaid = isWalkIn ? total : paid;
            int loggedInUserId = UserSession.getInstance().getUser().getId();

            log.info("Processing checkout — customer={} total={} paid={} user={}", customerId, total, paid, loggedInUserId);

            Sale sale = new Sale(0, TimeUtil.nowTimestamp(), total, "CASH",
                    customerId, loggedInUserId, dbAmountPaid, dbBalanceDue);
            sale.setItems(cartService.getCartData());
            saleDAO.saveSale(sale);
            log.info("Sale saved with id={}", sale.getId());

            // Save PDF soft copy
            sale.setItems(new java.util.ArrayList<>(cartService.getCartData()));
            String invoicePath = AppPaths.invoicePath(sale.getId());
            try {
                InvoiceGenerator.generateThermalReceipt(sale, currentCustomer, invoicePath);
                log.info("Invoice PDF saved: {}", invoicePath);
            } catch (Exception e) {
                log.error("Failed to save invoice PDF: {}", e.getMessage(), e);
                NotificationService.warn("Sale saved but PDF invoice could not be generated.");
            }

            // Ask to print thermal
            if (DialogUtil.confirm("Print Invoice", "Print receipt for Invoice #" + sale.getId() + "?", "")) {
                {
                    try {
                        ThermalPrinter.printInvoice(sale, currentCustomer, "Invoice #" + sale.getId());
                        log.info("Thermal receipt printed for sale {}", sale.getId());
                    } catch (Exception e) {
                        log.error("Thermal print failed for sale {}: {}", sale.getId(), e.getMessage(), e);
                        NotificationService.error("Printer error: " + e.getMessage());
                    }
                }
            }

            NotificationService.success("Sale #" + sale.getId() + " completed successfully.");
            cartService.clear();
            loadStockData();
            updateTotal();
            setupCustomerSelector(customerId);
            searchField.requestFocus();
            searchField.clear();

        } catch (NumberFormatException e) {
            log.warn("Checkout failed — invalid amount paid: {}", amountPaidField.getText());
            NotificationService.error("Please enter a valid amount in the Amount Paid field.");
        }
    }

    @FXML
    private void handleRemoveFromCart() {
        SaleItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        cartService.removeItem(selected);
        cartTable.refresh();
        updateTotal();
        log.info("Removed from cart: {}", selected.getProductName());
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
            setupCustomerSelector(null);
        } catch (IOException e) {
            log.error("Failed to open AddCustomerDialog: {}", e.getMessage(), e);
            NotificationService.error("Could not open customer registration.");
        }
    }
}