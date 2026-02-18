package com.my.pharmacy.controller;

import com.my.pharmacy.dao.*;
import com.my.pharmacy.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddProductController {

    @FXML private TextField nameField, genericField, manufacturerField, packSizeField;
    @FXML private TextField batchField, expiryField, costField;

    // Mode-specific fields
    @FXML private TextField qtyBoxesField;   // Wholesale only
    @FXML private TextField tradePriceField; // Wholesale only
    @FXML private TextField qtyUnitsField;   // Retail only
    @FXML private TextField retailPriceField;// Retail only

    private ProductDAO productDAO = new ProductDAOImpl();
    private BatchDAO batchDAO = new BatchDAOImpl();


    @FXML
    private void handleSave() {
        try {
            int packSize = Integer.parseInt(packSizeField.getText());
            Product product = new Product(0, nameField.getText(), genericField.getText(),
                    manufacturerField.getText(), "", packSize, 10, "");

            // 1. Save product and get ID
            int productId = productDAO.addProduct(product);

            if (productId == -1) {
                System.err.println("❌ Failed to save product to database.");
                return;
            }

            // 2. Logic for Wholesale vs Retail
            int totalUnits;
            double tradePrice = 0.0;
            double retailPrice = 0.0;

            if (qtyUnitsField != null) { // Retail
                totalUnits = Integer.parseInt(qtyUnitsField.getText());
                retailPrice = Double.parseDouble(retailPriceField.getText());
            } else { // Wholesale
                int boxes = Integer.parseInt(qtyBoxesField.getText());
                totalUnits = boxes * packSize;
                tradePrice = Double.parseDouble(tradePriceField.getText());
            }

            // 3. Save Batch
            Batch batch = new Batch(0, productId, batchField.getText(), expiryField.getText(),
                    totalUnits, Double.parseDouble(costField.getText()),
                    tradePrice, retailPrice, 0.0);

            batchDAO.addBatch(batch);

            // 4. Success! Close the window
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