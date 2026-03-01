package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

public class ItemLedgerController {

    @FXML private ComboBox<Product> productComboBox;

    // Sales Tab
    @FXML private TableView<SaleLedgerRecord> salesTable;
    @FXML private TableColumn<SaleLedgerRecord, String> colSaleDate;
    @FXML private TableColumn<SaleLedgerRecord, Integer> colInvoiceNo, colSaleQty;
    @FXML private TableColumn<SaleLedgerRecord, Double> colSaleRate, colSaleTotal;

    // Purchase History (Audit) Tab
    @FXML private TableView<PurchaseHistoryRecord> purchaseHistoryTable;
    @FXML private TableColumn<PurchaseHistoryRecord, String> colPurchDate, colDealerName, colInvoiceNoPurch;
    @FXML private TableColumn<PurchaseHistoryRecord, Integer> colPurchasedQty;
    @FXML private TableColumn<PurchaseHistoryRecord, Double> colPurchCost, colPurchTrade;

    private final ProductDAO productDAO = new ProductDAOImpl();
    private final SaleDAO saleDAO = new SaleDAOImpl();
    private final BatchDAO batchDAO = new BatchDAOImpl();

    @FXML
    public void initialize() {
        setupComboBox();
        setupColumns();
    }

    private void setupComboBox() {
        productComboBox.setItems(FXCollections.observableArrayList(productDAO.getAllProducts()));
        productComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Product p) { return p == null ? "" : p.getName(); }
            @Override public Product fromString(String s) { return null; }
        });
    }

    private void setupColumns() {
        // Sales Mapping
        colSaleDate.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        colInvoiceNo.setCellValueFactory(new PropertyValueFactory<>("invoiceNo"));
        colSaleQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colSaleRate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        colSaleTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Purchase Audit Mapping
        colPurchDate.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));
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