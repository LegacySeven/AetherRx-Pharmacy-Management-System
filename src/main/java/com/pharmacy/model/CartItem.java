package com.pharmacy.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Represents a single item added to the shopping cart during a sale.
 */
public class CartItem {
    private final SimpleStringProperty code;
    private final SimpleStringProperty name;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty unitPrice;
    private final SimpleDoubleProperty subtotal;

    public CartItem(String code, String name, int quantity, double unitPrice) {
        this.code = new SimpleStringProperty(code);
        this.name = new SimpleStringProperty(name);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.subtotal = new SimpleDoubleProperty(quantity * unitPrice);
    }

    public String getCode() { return code.get(); }
    public SimpleStringProperty codeProperty() { return code; }

    public String getName() { return name.get(); }
    public SimpleStringProperty nameProperty() { return name; }

    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int val) {
        this.quantity.set(val);
        this.subtotal.set(val * getUnitPrice());
    }
    public SimpleIntegerProperty quantityProperty() { return quantity; }

    public double getUnitPrice() { return unitPrice.get(); }
    public SimpleDoubleProperty unitPriceProperty() { return unitPrice; }

    public double getSubtotal() { return subtotal.get(); }
    public SimpleDoubleProperty subtotalProperty() { return subtotal; }
}
