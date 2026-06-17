package com.pharmacy.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Model class representing a medicine in the pharmacy inventory.
 * Employs JavaFX properties for instant two-way bindings with TableView columns.
 */
public class Medicine {
    private final SimpleStringProperty code;
    private final SimpleStringProperty name;
    private final SimpleStringProperty category;
    private final SimpleIntegerProperty stock;
    private final SimpleStringProperty status;
    private final SimpleDoubleProperty price;

    public Medicine(String code, String name, String category, int stock, String status, double price) {
        this.code = new SimpleStringProperty(code);
        this.name = new SimpleStringProperty(name);
        this.category = new SimpleStringProperty(category);
        this.stock = new SimpleIntegerProperty(stock);
        this.status = new SimpleStringProperty(status);
        this.price = new SimpleDoubleProperty(price);
    }

    // Code Property
    public String getCode() { return code.get(); }
    public void setCode(String val) { this.code.set(val); }
    public SimpleStringProperty codeProperty() { return code; }

    // Name Property
    public String getName() { return name.get(); }
    public void setName(String val) { this.name.set(val); }
    public SimpleStringProperty nameProperty() { return name; }

    // Category Property
    public String getCategory() { return category.get(); }
    public void setCategory(String val) { this.category.set(val); }
    public SimpleStringProperty categoryProperty() { return category; }

    // Stock Property
    public int getStock() { return stock.get(); }
    public void setStock(int val) { this.stock.set(val); }
    public SimpleIntegerProperty stockProperty() { return stock; }

    // Status Property
    public String getStatus() { return status.get(); }
    public void setStatus(String val) { this.status.set(val); }
    public SimpleStringProperty statusProperty() { return status; }

    // Price Property
    public double getPrice() { return price.get(); }
    public void setPrice(double val) { this.price.set(val); }
    public SimpleDoubleProperty priceProperty() { return price; }
}
