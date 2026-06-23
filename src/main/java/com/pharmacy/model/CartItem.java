package com.pharmacy.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Represents a single item added to the shopping cart during a sale.
 * Employs JavaFX properties to allow direct binding to TableView columns in the UI.
 */
public class CartItem {
    // 1. Immutable property wrappers for cart item details
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
        // 2. Initialize properties with provided values
        this.code = new SimpleStringProperty(code);
        this.name = new SimpleStringProperty(name);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        
        // 3. Automatically calculate initial subtotal (quantity * unitPrice)
        this.subtotal = new SimpleDoubleProperty(quantity * unitPrice);
    }

    /** @return The raw medicine code. */
    public String getCode() { return code.get(); }
    
    /** @return The underlying JavaFX property for the code, used for UI binding. */
    public SimpleStringProperty codeProperty() { return code; }

    /** @return The raw medicine name. */
    public String getName() { return name.get(); }
    
    /** @return The underlying JavaFX property for the name, used for UI binding. */
    public SimpleStringProperty nameProperty() { return name; }

    /** @return The raw quantity of items in the cart. */
    public int getQuantity() { return quantity.get(); }
    
    /**
     * Updates the quantity of this item in the cart.
     * Automatically recalculates the subtotal based on the new quantity.
     *
     * @param val The new quantity.
     */
    public void setQuantity(int val) {
        // 4. Update the quantity property
        this.quantity.set(val);
        // 5. Mathematically derive and set the new subtotal
        this.subtotal.set(val * getUnitPrice());
    }
    
    /** @return The underlying JavaFX property for the quantity, used for UI binding. */
    public SimpleIntegerProperty quantityProperty() { return quantity; }

    /** @return The raw unit price of the medicine. */
    public double getUnitPrice() { return unitPrice.get(); }
    
    /** @return The underlying JavaFX property for the unit price, used for UI binding. */
    public SimpleDoubleProperty unitPriceProperty() { return unitPrice; }

    /** @return The raw calculated subtotal (quantity * unitPrice). */
    public double getSubtotal() { return subtotal.get(); }
    
    /** @return The underlying JavaFX property for the subtotal, used for UI binding. */
    public SimpleDoubleProperty subtotalProperty() { return subtotal; }
}
