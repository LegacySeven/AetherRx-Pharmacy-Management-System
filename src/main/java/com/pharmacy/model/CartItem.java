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

    /**
     * Constructs a new CartItem.
     *
     * @param code      The unique identifier for the medicine.
     * @param name      The human-readable name of the medicine.
     * @param quantity  The amount of this medicine being purchased.
     * @param unitPrice The price per single unit of this medicine.
     */
    public CartItem(String code, String name, int quantity, double unitPrice) {
        this.code = new SimpleStringProperty(code);
        this.name = new SimpleStringProperty(name);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        // Automatically calculate initial subtotal
        this.subtotal = new SimpleDoubleProperty(quantity * unitPrice);
    }

    /** @return The medicine code. */
    public String getCode() { return code.get(); }
    public SimpleStringProperty codeProperty() { return code; }

    /** @return The medicine name. */
    public String getName() { return name.get(); }
    public SimpleStringProperty nameProperty() { return name; }

    /** @return The quantity of items in the cart. */
    public int getQuantity() { return quantity.get(); }
    
    /**
     * Updates the quantity of this item in the cart.
     * Automatically recalculates the subtotal based on the new quantity.
     *
     * @param val The new quantity.
     */
    public void setQuantity(int val) {
        this.quantity.set(val);
        this.subtotal.set(val * getUnitPrice());
    }
    public SimpleIntegerProperty quantityProperty() { return quantity; }

    /** @return The unit price of the medicine. */
    public double getUnitPrice() { return unitPrice.get(); }
    public SimpleDoubleProperty unitPriceProperty() { return unitPrice; }

    /** @return The calculated subtotal (quantity * unitPrice). */
    public double getSubtotal() { return subtotal.get(); }
    public SimpleDoubleProperty subtotalProperty() { return subtotal; }
}
