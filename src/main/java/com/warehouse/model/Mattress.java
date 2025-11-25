package com.warehouse.model;

public class Mattress {
    private int id;
    private String type;
    private String size;
    private String reference;
    private int quantity;
    private int initialStock;
    private int quantitySold;
    private double unitPrice;
    private int sortOrder;

    public Mattress(int id, String type, String size, String reference, int quantity, double unitPrice, int sortOrder) {
        this(id, type, size, reference, quantity, quantity, 0, unitPrice, sortOrder);
    }
    
    public Mattress(int id, String type, String size, String reference, int quantity, int initialStock, double unitPrice, int sortOrder) {
        this(id, type, size, reference, quantity, initialStock, 0, unitPrice, sortOrder);
    }
    
    public Mattress(int id, String type, String size, String reference, int quantity, int initialStock, int quantitySold, double unitPrice, int sortOrder) {
        this.id = id;
        this.type = type;
        this.size = size;
        this.reference = reference;
        this.quantity = quantity;
        this.initialStock = initialStock;
        this.quantitySold = quantitySold;
        this.unitPrice = unitPrice;
        this.sortOrder = sortOrder;
    }

    public Mattress(String type, String size, String reference, int quantity, double unitPrice) {
        this(0, type, size, reference, quantity, quantity, 0, unitPrice, 0);
    }

    public int getId() { return id; }
    public String getType() { return type; }
    public String getSize() { return size; }
    public String getReference() { return reference; }
    public int getQuantity() { return quantity; }
    public int getInitialStock() { return initialStock; }
    public int getQuantitySold() { return quantitySold; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalPrice() { return unitPrice * quantity; } // Total value = unit price * quantity
    public int getSortOrder() { return sortOrder; }

    public void setId(int id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setSize(String size) { this.size = size; }
    public void setReference(String reference) { this.reference = reference; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setInitialStock(int initialStock) { this.initialStock = initialStock; }
    public void setQuantitySold(int quantitySold) { this.quantitySold = quantitySold; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
