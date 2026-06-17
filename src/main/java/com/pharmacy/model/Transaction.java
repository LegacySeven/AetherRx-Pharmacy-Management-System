package com.pharmacy.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Represents a completed sales transaction with its line items.
 */
public class Transaction {
    private final SimpleStringProperty txnId;
    private final SimpleStringProperty dateTime;
    private final SimpleStringProperty customerId;
    private final SimpleDoubleProperty total;
    private final SimpleIntegerProperty itemCount;
    private final ObservableList<CartItem> items;

    public Transaction(String txnId, String customerId, ObservableList<CartItem> items, double total) {
        this.txnId = new SimpleStringProperty(txnId);
        this.dateTime = new SimpleStringProperty(
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
        this.customerId = new SimpleStringProperty(customerId == null || customerId.isEmpty() ? "Walk-in" : customerId);
        this.total = new SimpleDoubleProperty(total);
        this.itemCount = new SimpleIntegerProperty(items.size());
        this.items = FXCollections.observableArrayList(items);
    }

    /** Constructor with explicit dateTime for sample/historical data. */
    public Transaction(String txnId, String dateTime, String customerId, ObservableList<CartItem> items, double total) {
        this.txnId = new SimpleStringProperty(txnId);
        this.dateTime = new SimpleStringProperty(dateTime);
        this.customerId = new SimpleStringProperty(customerId == null || customerId.isEmpty() ? "Walk-in" : customerId);
        this.total = new SimpleDoubleProperty(total);
        this.itemCount = new SimpleIntegerProperty(items.size());
        this.items = FXCollections.observableArrayList(items);
    }

    public String getTxnId() { return txnId.get(); }
    public SimpleStringProperty txnIdProperty() { return txnId; }

    public String getDateTime() { return dateTime.get(); }
    public SimpleStringProperty dateTimeProperty() { return dateTime; }

    public String getCustomerId() { return customerId.get(); }
    public SimpleStringProperty customerIdProperty() { return customerId; }

    public double getTotal() { return total.get(); }
    public SimpleDoubleProperty totalProperty() { return total; }

    public int getItemCount() { return itemCount.get(); }
    public SimpleIntegerProperty itemCountProperty() { return itemCount; }

    public ObservableList<CartItem> getItems() { return items; }
}
