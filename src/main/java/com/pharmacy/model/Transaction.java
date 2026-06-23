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

    private final SimpleDoubleProperty total;
    private final SimpleIntegerProperty itemCount;
    private final ObservableList<CartItem> items;

    /**
     * Constructs a new Transaction using the current system time.
     *
     * @param txnId      The unique transaction receipt ID.
     * @param items      The list of items purchased.
     * @param total      The grand total monetary value.
     */
    public Transaction(String txnId, ObservableList<CartItem> items, double total) {
        this.txnId = new SimpleStringProperty(txnId);
        this.dateTime = new SimpleStringProperty(
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
        this.total = new SimpleDoubleProperty(total);
        this.itemCount = new SimpleIntegerProperty(items.size());
        this.items = FXCollections.observableArrayList(items);
    }

    /**
     * Constructor with explicit dateTime for loading historical data.
     *
     * @param txnId      The unique transaction receipt ID.
     * @param dateTime   The precise date and time the transaction occurred (yyyy-MM-dd HH:mm).
     * @param items      The list of items purchased.
     * @param total      The grand total monetary value.
     */
    public Transaction(String txnId, String dateTime, ObservableList<CartItem> items, double total) {
        this.txnId = new SimpleStringProperty(txnId);
        this.dateTime = new SimpleStringProperty(dateTime);
        this.total = new SimpleDoubleProperty(total);
        this.itemCount = new SimpleIntegerProperty(items.size());
        this.items = FXCollections.observableArrayList(items);
    }

    /** @return The unique receipt identifier. */
    public String getTxnId() { return txnId.get(); }
    public SimpleStringProperty txnIdProperty() { return txnId; }

    /** @return The formatted date and time of the transaction. */
    public String getDateTime() { return dateTime.get(); }
    public SimpleStringProperty dateTimeProperty() { return dateTime; }



    /** @return The total price paid. */
    public double getTotal() { return total.get(); }
    public SimpleDoubleProperty totalProperty() { return total; }

    /** @return The total number of unique items purchased. */
    public int getItemCount() { return itemCount.get(); }
    public SimpleIntegerProperty itemCountProperty() { return itemCount; }

    /** @return An immutable list of the items within this transaction. */
    public ObservableList<CartItem> getItems() { return items; }
}
