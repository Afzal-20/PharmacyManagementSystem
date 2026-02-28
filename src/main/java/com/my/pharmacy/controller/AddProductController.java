package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddProductController {

    @FXML private TextField nameField, genericField, manufacturerField, packSizeField;
    @FXML private TextField batchField, expiryField, costField;

    @FXML private TextField qtyBoxesField;
    @FXML private TextField tradePriceField;
    @FXML private TextField qtyUnitsField;
    @FXML private TextField retailPriceField;

    private ProductDAO productDAO = new ProductDAOImpl();
    private BatchDAO batchDAO = new BatchDAOImpl();

    @FXML
    private void handleSave() {
        try {
            int packSize = Integer.parseInt(packSizeField.getText());
            Product product = new Product(0, nameField.getText(), genericField.getText(),
                    manufacturerField.getText(), "", packSize, 10, "");

            int productId = productDAO.addProduct(product);

            if (productId == -1) {
                System.err.println("❌ Failed to save product to database.");
                return;
            }

            int totalBoxes;
            double tradePrice = 0.0;
            double retailPrice = 0.0;

            // PURE BOX-CENTRIC LOGIC
            if (qtyUnitsField != null) {
                // Note: Even if the UI says 'qtyUnits', we are treating it as Boxes now
                totalBoxes = Integer.parseInt(qtyUnitsField.getText());
                retailPrice = Double.parseDouble(retailPriceField.getText());
            } else {
                totalBoxes = Integer.parseInt(qtyBoxesField.getText());
                tradePrice = Double.parseDouble(tradePriceField.getText());
            }

            Batch batch = new Batch(0, productId, batchField.getText(), expiryField.getText(),
                    totalBoxes, Double.parseDouble(costField.getText()),
                    tradePrice, retailPrice, 0.0, 0.0, 0.0);

            batchDAO.addBatch(batch);
            closeWindow();

        } catch (NumberFormatException e) {
            System.err.println("❌ Invalid number input. Check prices and quantities.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleCancel() { closeWindow(); }
    private void closeWindow() { ((Stage) nameField.getScene().getWindow()).close(); }
}