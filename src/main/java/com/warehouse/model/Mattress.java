package com.warehouse.model;

public class Mattress {
    private int id;
    private String type;
    private String size;
    private String reference;
    private int quantity;
    private int initialStock;
    private double unitPrice;
    private double salePrice;
    private int sortOrder;

    public Mattress(int id, String type, String size, String reference, int quantity, double unitPrice, double salePrice, int sortOrder) {
        this(id, type, size, reference, quantity, quantity, unitPrice, salePrice, sortOrder);
    }
    
    public Mattress(int id, String type, String size, String reference, int quantity, int initialStock, double unitPrice, double salePrice, int sortOrder) {
        this.id = id;
        this.type = type;
        this.size = size;
        this.reference = reference;
        this.quantity = quantity;
        this.initialStock = initialStock;
        this.unitPrice = unitPrice;
        this.salePrice = salePrice;
        this.sortOrder = sortOrder;
    }

    public Mattress(String type, String size, String reference, int quantity, double unitPrice, double salePrice) {
        this(0, type, size, reference, quantity, quantity, unitPrice, salePrice, 0);
    }

    public int getId() { return id; }
    public String getType() { return type; }
    public String getSize() { return size; }
    public String getReference() { return reference; }
    public int getQuantity() { return quantity; }
    public int getInitialStock() { return initialStock; }
    public double getUnitPrice() { return unitPrice; }
    public double getSalePrice() { return salePrice; }
    public double getPrix() { return salePrice; } // backward compatibility
    public int getSortOrder() { return sortOrder; }

    public void setId(int id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setSize(String size) { this.size = size; }
    public void setReference(String reference) { this.reference = reference; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setInitialStock(int initialStock) { this.initialStock = initialStock; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setSalePrice(double salePrice) { this.salePrice = salePrice; }
    public void setPrix(double prix) { this.salePrice = prix; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
