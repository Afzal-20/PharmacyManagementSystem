package com.my.pharmacy.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

// We implement 'Initializable' to run code when the screen loads
public class POSController implements Initializable {

    @FXML private TextField searchField;
    @FXML private TableView<?> cartTable;
    @FXML private Label lblGrandTotal;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // FIX: Set the resize policy here instead of FXML to avoid errors
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    public void addToCart() {
        System.out.println("Adding product: " + searchField.getText());
    }

    @FXML
    public void handleCheckout() {
        System.out.println("Checkout clicked! Processing Sale...");
    }
}