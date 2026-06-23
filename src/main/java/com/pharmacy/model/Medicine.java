package com.pharmacy.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Model class representing a medicine in the pharmacy inventory.
 * Employs JavaFX properties for instant two-way bindings with TableView columns.
 */
public class Medicine {
    // 1. Immutable property wrappers for data binding to JavaFX UI components
    private final SimpleStringProperty code;
    private final SimpleStringProperty name;
    private final SimpleStringProperty category;
    private final SimpleIntegerProperty stock;
    private final SimpleStringProperty status;
    private final SimpleDoubleProperty price;

    /**
     * Constructs a new Medicine instance.
     *
     * @param code       The unique alphanumeric identifier for the medicine.
     * @param name       The display name of the medicine.
     * @param category   The classification or type of the medicine.
     * @param stock      The initial quantity available in inventory.
     * @param status     The textual status (e.g., "In Stock", "Low Stock", "Out of Stock").
     * @param price      The retail price per unit.
     */
    public Medicine(String code, String name, String category, int stock, String status, double price) {
        // 2. Initialize the property wrappers with the provided raw values
        this.code = new SimpleStringProperty(code);
        this.name = new SimpleStringProperty(name);
        this.category = new SimpleStringProperty(category);
        this.stock = new SimpleIntegerProperty(stock);
        this.status = new SimpleStringProperty(status);
        this.price = new SimpleDoubleProperty(price);
    }

    /** @return The raw medicine's unique code string. */
    public String getCode() { return code.get(); }
    
    /** @param val The new medicine code to set. */
    public void setCode(String val) { this.code.set(val); }
    
    /** @return The underlying JavaFX property object for the code, used for UI binding. */
    public SimpleStringProperty codeProperty() { return code; }

    /** @return The raw medicine's display name string. */
    public String getName() { return name.get(); }
    
    /** @param val The new medicine name to set. */
    public void setName(String val) { this.name.set(val); }
    
    /** @return The underlying JavaFX property object for the name, used for UI binding. */
    public SimpleStringProperty nameProperty() { return name; }

    /** @return The raw pharmaceutical category string. */
    public String getCategory() { return category.get(); }
    
    /** @param val The new category to set. */
    public void setCategory(String val) { this.category.set(val); }
    
    /** @return The underlying JavaFX property object for the category, used for UI binding. */
    public SimpleStringProperty categoryProperty() { return category; }

    /** @return The current raw available quantity in inventory. */
    public int getStock() { return stock.get(); }
    
    /** @param val The new stock quantity to set. */
    public void setStock(int val) { this.stock.set(val); }
    
    /** @return The underlying JavaFX property object for stock, used for UI binding. */
    public SimpleIntegerProperty stockProperty() { return stock; }

    /** @return The raw availability status string. */
    public String getStatus() { return status.get(); }
    
    /** @param val The new textual status to set. */
    public void setStatus(String val) { this.status.set(val); }
    
    /** @return The underlying JavaFX property object for status, used for UI binding. */
    public SimpleStringProperty statusProperty() { return status; }

    /** @return The raw retail price per unit. */
    public double getPrice() { return price.get(); }
    
    /** @param val The new price to set. */
    public void setPrice(double val) { this.price.set(val); }
    
    /** @return The underlying JavaFX property object for price, used for UI binding. */
    public SimpleDoubleProperty priceProperty() { return price; }
}
