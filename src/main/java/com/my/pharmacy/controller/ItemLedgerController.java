package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.*;
import com.my.pharmacy.util.TimeUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

public class ItemLedgerController {

    @FXML private TextField searchField; // New Search Field
    @FXML private ComboBox<Product> productComboBox;

    @FXML private TableView<SaleLedgerRecord> salesTable;
    @FXML private TableColumn<SaleLedgerRecord, String> colSaleDate;
    @FXML private TableColumn<SaleLedgerRecord, Integer> colInvoiceNo, colSaleQty;
    @FXML private TableColumn<SaleLedgerRecord, Double> colSaleRate, colSaleTotal;
    @FXML private TableColumn<SaleLedgerRecord, String> colSaleCustomer; // Issue 1

    @FXML private TableView<PurchaseHistoryRecord> purchaseHistoryTable;
    @FXML private TableColumn<PurchaseHistoryRecord, String> colPurchDate, colDealerName, colInvoiceNoPurch;
    @FXML private TableColumn<PurchaseHistoryRecord, Integer> colPurchasedQty;
    @FXML private TableColumn<PurchaseHistoryRecord, Double> colPurchCost, colPurchTrade;

    private final ProductDAO productDAO = new ProductDAOImpl();
    private final SaleDAO saleDAO = new SaleDAOImpl();
    private final BatchDAO batchDAO = new BatchDAOImpl();

    private final ObservableList<Product> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupComboBox();
        setupColumns();
    }

    private void setupComboBox() {
        masterData.setAll(productDAO.getAllProducts());
        FilteredList<Product> filteredData = new FilteredList<>(masterData, p -> true);

        // Connect TextField to filter the ComboBox
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(p -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return p.getName().toLowerCase().contains(lower) ||
                        (p.getGenericName() != null && p.getGenericName().toLowerCase().contains(lower));
            });
            // Auto-select first match for speed
            if (!filteredData.isEmpty()) productComboBox.getSelectionModel().selectFirst();
        });

        productComboBox.setItems(filteredData);
        productComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Product p) { return p == null ? "" : p.getName(); }
            @Override public Product fromString(String s) { return null; }
        });
    }

    private void setupColumns() {
        // Format UTC Timestamps from DB into local display strings via TimeUtil
        colSaleDate.setCellValueFactory(data ->
                new SimpleStringProperty(TimeUtil.format(data.getValue().getSaleDate(), TimeUtil.PATTERN_FULL)));
        colInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoiceNo"));
        colSaleQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colSaleRate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        colSaleTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colSaleCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        colPurchDate.setCellValueFactory(data ->
                new SimpleStringProperty(TimeUtil.format(data.getValue().getPurchaseDate(), TimeUtil.PATTERN_FULL)));
        colDealerName.setCellValueFactory(new PropertyValueFactory<>("dealerName"));
        colInvoiceNoPurch.setCellValueFactory(new PropertyValueFactory<>("invoiceNo"));
        colPurchasedQty.setCellValueFactory(new PropertyValueFactory<>("initialBoxes"));
        colPurchCost.setCellValueFactory(new PropertyValueFactory<>("costPrice"));
        colPurchTrade.setCellValueFactory(new PropertyValueFactory<>("tradePrice"));
    }

    @FXML
    private void loadLedgerData() {
        Product selected = productComboBox.getValue();
        if (selected == null) return;
        salesTable.setItems(FXCollections.observableArrayList(saleDAO.getSalesHistoryByProductId(selected.getId())));
        purchaseHistoryTable.setItems(FXCollections.observableArrayList(batchDAO.getPurchaseHistoryByProductId(selected.getId())));
    }
}