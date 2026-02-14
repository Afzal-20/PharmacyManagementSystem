package com.my.pharmacy.model;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Sale {

    private final IntegerProperty id;
    private final StringProperty invoiceNumber;
    private final ObjectProperty<LocalDateTime> timestamp;
    private final DoubleProperty totalAmount;
    private final StringProperty paymentMethod;

    // THE FIX: An ObservableList that watches the 'subTotal' of its items
    private final ObservableList<SaleItem> items;

    public Sale() {
        this(0, "INV-NEW", LocalDateTime.now(), 0.0, "CASH");
    }

    public Sale(int id, String invoiceNumber, LocalDateTime timestamp,
                double totalAmount, String paymentMethod) {
        this.id = new SimpleIntegerProperty(id);
        this.invoiceNumber = new SimpleStringProperty(invoiceNumber);
        this.timestamp = new SimpleObjectProperty<>(timestamp);
        this.paymentMethod = new SimpleStringProperty(paymentMethod);

        // 1. Initialize Total (Default 0.0)
        this.totalAmount = new SimpleDoubleProperty(0.0);

        // 2. THE REACTIVE MAGIC (Extractor)
        // This tells the list: "Fire an update event if an item is added/removed OR if an item's subTotal changes."
        this.items = FXCollections.observableArrayList(item ->
                new Observable[]{item.subTotalProperty()}
        );

        // 3. Bind Grand Total to the Sum of Items
        // This line replaces the manual 'recalculateGrandTotal()' method entirely.
        this.totalAmount.bind(Bindings.createDoubleBinding(() ->
                        items.stream().mapToDouble(SaleItem::getSubTotal).sum(),
                this.items // Dependency: Re-run this calculation whenever the list changes
        ));
    }

    // --- Getters & Properties ---
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getInvoiceNumber() { return invoiceNumber.get(); }
    public void setInvoiceNumber(String value) { invoiceNumber.set(value); }
    public StringProperty invoiceNumberProperty() { return invoiceNumber; }

    public LocalDateTime getTimestamp() { return timestamp.get(); }
    public void setTimestamp(LocalDateTime value) { timestamp.set(value); }
    public ObjectProperty<LocalDateTime> timestampProperty() { return timestamp; }

    public double getTotalAmount() { return totalAmount.get(); }
    // No setter needed for totalAmount because it is bound!
    public DoubleProperty totalAmountProperty() { return totalAmount; }

    public String getPaymentMethod() { return paymentMethod.get(); }
    public void setPaymentMethod(String value) { paymentMethod.set(value); }
    public StringProperty paymentMethodProperty() { return paymentMethod; }

    public ObservableList<SaleItem> getItems() { return items; }

    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");
        return timestamp.get().format(formatter);
    }
}