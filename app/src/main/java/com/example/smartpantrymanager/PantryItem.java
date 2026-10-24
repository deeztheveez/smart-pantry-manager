package com.example.smartpantrymanager;

/**
 * One ingredient the user has at home.
 * This is a simple "data holder" class (a model).
 */
public class PantryItem {

    private final long id;            // database id (-1 if not saved yet)
    private final String name;        // e.g. "Tomatoes"
    private final double quantity;    // e.g. 4
    private final String unit;        // e.g. "pcs", "g", "ml"
    private final String expiryDate;  // "yyyy-MM-dd", or null if there is none

    public PantryItem(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiryDate() { return expiryDate; }
}