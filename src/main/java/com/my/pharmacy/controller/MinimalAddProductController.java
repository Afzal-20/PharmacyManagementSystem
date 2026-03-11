package com.my.pharmacy.controller;

import com.my.pharmacy.dao.ProductDAO;
import com.my.pharmacy.dao.ProductDAOImpl;
import com.my.pharmacy.model.Product;
import javafx.fxml.FXML;
import com.my.pharmacy.util.DialogUtil;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class MinimalAddProductController {
    @FXML private TextField nameField, genericField, manufacturerField, packSizeField;
    private final ProductDAO productDAO = new ProductDAOImpl();

    @FXML
    private void handleSave() {
        try {
            int packSize = Integer.parseInt(packSizeField.getText());
            Product product = new Product(0, nameField.getText(), genericField.getText(), manufacturerField.getText(), "", packSize, 10, "");

            if (!DialogUtil.confirm("Add Product", "Are you sure you want to add this product?", "")) return;

            productDAO.addProduct(product);
            closeWindow();
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Pack size must be a number.").show();
        }
    }
    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) nameField.getScene().getWindow()).close(); }
}