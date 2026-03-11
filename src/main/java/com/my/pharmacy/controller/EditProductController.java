package com.my.pharmacy.controller;

import com.my.pharmacy.dao.ProductDAO;
import com.my.pharmacy.dao.ProductDAOImpl;
import com.my.pharmacy.model.Product;
import com.my.pharmacy.util.NotificationService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditProductController {
    @FXML private TextField nameField, genericField, manufacturerField, packSizeField, minStockField;

    private final ProductDAO productDAO = new ProductDAOImpl();
    private Product productToEdit;

    public void setProductData(Product product) {
        this.productToEdit = product;
        nameField.setText(product.getName());
        genericField.setText(product.getGenericName());
        manufacturerField.setText(product.getManufacturer());
        packSizeField.setText(String.valueOf(product.getPackSize()));
        minStockField.setText(String.valueOf(product.getMinStockLevel()));
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().trim().isEmpty()) {
            NotificationService.warn("Medicine Name is required.");
            return;
        }
        try {
            int packSize = Integer.parseInt(packSizeField.getText().trim());
            int minStock = Integer.parseInt(minStockField.getText().trim());

            productToEdit.setName(nameField.getText().trim());
            productToEdit.setGenericName(genericField.getText().trim());
            productToEdit.setManufacturer(manufacturerField.getText().trim());
            productToEdit.setPackSize(packSize);
            productToEdit.setMinStockLevel(minStock);

            productDAO.updateProduct(productToEdit);
            NotificationService.success("Product updated successfully.");
            closeWindow();
        } catch (NumberFormatException e) {
            NotificationService.error("Pack Size and Min Stock must be valid numbers.");
        }
    }

    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) nameField.getScene().getWindow()).close(); }
}
