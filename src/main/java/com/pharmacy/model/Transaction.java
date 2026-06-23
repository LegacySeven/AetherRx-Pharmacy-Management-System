package com.pharmacy.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Represents a completed sales transaction with its line items.
 * Employs JavaFX properties to allow direct binding to TableView columns in the UI.
 */
public class Transaction {
    // 1. Immutable property wrappers for transaction metadata
    private final SimpleStringProperty txnId;
    private final SimpleStringProperty dateTime;
    private final SimpleDoubleProperty total;
    private final SimpleIntegerProperty itemCount;
    
    // 2. Observable list to hold the specific items purchased in this transaction
    private final ObservableList<CartItem> items;

    /**
     * Constructs a new Transaction using the current system time.
     * Commonly used when checking out a new sale.
     *
     * @param txnId      The unique transaction receipt ID.
     * @param items      The list of items purchased.
     * @param total      The grand total monetary value.
     */
    public Transaction(String txnId, ObservableList<CartItem> items, double total) {
        // 3. Bind the passed ID and total
        this.txnId = new SimpleStringProperty(txnId);
        
        // 4. Capture the exact current moment and format it uniformly
        this.dateTime = new SimpleStringProperty(
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
        this.total = new SimpleDoubleProperty(total);
        
        // 5. Extract the count of unique items
        this.itemCount = new SimpleIntegerProperty(items.size());
        
        // 6. Copy the items into a new observable list to prevent external mutation
        this.items = FXCollections.observableArrayList(items);
    }

    /**
     * Constructor with explicit dateTime for loading historical data.
     * Commonly used when restoring records from the SQLite database.
     *
     * @param txnId      The unique transaction receipt ID.
     * @param dateTime   The precise date and time the transaction occurred (yyyy-MM-dd HH:mm).
     * @param items      The list of items purchased.
     * @param total      The grand total monetary value.
     */
    public Transaction(String txnId, String dateTime, ObservableList<CartItem> items, double total) {
        // 7. Bind all historical parameters directly
        this.txnId = new SimpleStringProperty(txnId);
        this.dateTime = new SimpleStringProperty(dateTime);
        this.total = new SimpleDoubleProperty(total);
        this.itemCount = new SimpleIntegerProperty(items.size());
        this.items = FXCollections.observableArrayList(items);
    }

    /** @return The raw unique receipt identifier. */
    public String getTxnId() { return txnId.get(); }
    
    /** @return The underlying JavaFX property object for the ID, used for UI binding. */
    public SimpleStringProperty txnIdProperty() { return txnId; }

    /** @return The raw formatted date and time string of the transaction. */
    public String getDateTime() { return dateTime.get(); }
    
    /** @return The underlying JavaFX property object for the date, used for UI binding. */
    public SimpleStringProperty dateTimeProperty() { return dateTime; }

    /** @return The raw total price paid. */
    public double getTotal() { return total.get(); }
    
    /** @return The underlying JavaFX property object for the total, used for UI binding. */
    public SimpleDoubleProperty totalProperty() { return total; }

    /** @return The raw total number of unique items purchased. */
    public int getItemCount() { return itemCount.get(); }
    
    /** @return The underlying JavaFX property object for the item count, used for UI binding. */
    public SimpleIntegerProperty itemCountProperty() { return itemCount; }

    /** @return An immutable list of the items within this transaction. */
    public ObservableList<CartItem> getItems() { return items; }
}
