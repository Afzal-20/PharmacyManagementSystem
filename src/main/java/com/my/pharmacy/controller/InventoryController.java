package com.my.pharmacy.controller;

import com.my.pharmacy.dao.ProductDAO;
import com.my.pharmacy.dao.ProductDAOImpl;
import com.my.pharmacy.model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class InventoryController {

    @FXML private TableView<Product> inventoryTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colGeneric;
    @FXML private TableColumn<Product, String> colManufacturer;
    @FXML private TableColumn<Product, Integer> colPackSize;

    private ProductDAO productDAO = new ProductDAOImpl();
    private ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGeneric.setCellValueFactory(new PropertyValueFactory<>("genericName"));
        colManufacturer.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        colPackSize.setCellValueFactory(new PropertyValueFactory<>("packSize"));

        loadInventoryData();
    }

    private void loadInventoryData() {
        productList.clear();
        productList.addAll(productDAO.getAllProducts());
        inventoryTable.setItems(productList);
    }

    @FXML
    private void handleAddNewProduct() {
        // Placeholder for the Add Product Dialog
        System.out.println("Add Product Dialog Triggered");
    }
}