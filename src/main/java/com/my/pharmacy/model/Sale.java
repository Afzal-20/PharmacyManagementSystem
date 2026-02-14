package com.my.pharmacy.model;

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
    private final StringProperty paymentMethod;  //Cash or Card

    //This list holds the items for Cart
    // Observable list so tableview updates Instantly
    private final ObservableList<SaleItem> items;

    public Sale(){
        this(0, "INV-TEMP", LocalDateTime.now(), 0.0, "CASH");
    }
    public Sale(int id, String invoiceNumber, LocalDateTime timestamp, double totalAmount, String paymentMethod){
        this.id=new SimpleIntegerProperty(id);
        this.invoiceNumber=new SimpleStringProperty(invoiceNumber);
        this.timestamp=new SimpleObjectProperty<>(timestamp);
        this.totalAmount=new SimpleDoubleProperty(totalAmount);
        this.paymentMethod=new SimpleStringProperty(paymentMethod);
        this.items= FXCollections.observableArrayList();
    }

    // --- Getters & Properties ---

    public int getID(){ return id.get();}
    public void setId(int value){id.set(value);}
    public IntegerProperty idProperty(){return id;}

    public String getInvoiceNumber(){return invoiceNumber.get();}
    public void setInvoiceNumber(String value){invoiceNumber.set(value);}
    public StringProperty invoiceNumberProperty(){return invoiceNumber;}

    public LocalDateTime getTimeStamp(){return timestamp.get();}
    public void setTimestamp(LocalDateTime value){timestamp.set(value);}
    public ObjectProperty<LocalDateTime> timeStampProperty(){return timestamp;}

    public double getTotalAmount(){return totalAmount.get();}
    public void setTotalAmount(double value){totalAmount.set(value);}
    public DoubleProperty totalAmountProperty(){return totalAmount;}

    public String getPaymentMethod() { return paymentMethod.get(); }
    public void setPaymentMethod(String value) { paymentMethod.set(value); }
    public StringProperty paymentMethodProperty() { return paymentMethod; }

    public ObservableList<SaleItem> getItems() { return items; }

    /**
     * Helper: Updates the Grand Total based on the items in the cart.
     */
    public void recalculateGrandTotal() {
        double sum = 0;
        for (SaleItem item : items) {
            sum += item.getSubTotal();
        }
        this.totalAmount.set(sum);
    }

    /**
     * Formats the date nicely for the UI (e.g., "15-Feb-2026 10:30 AM")
     */
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");
        return timestamp.get().format(formatter);
    }

}
