package com.warehouse.model;

/**
 * Represents an item within a pack transaction
 */
public class PackItem {
    private int id;
    private int transactionId;
    private int mattressId;
    private int quantity;
    private String mattressName; // For display purposes
    
    public PackItem(int id, int transactionId, int mattressId, int quantity) {
        this.id = id;
        this.transactionId = transactionId;
        this.mattressId = mattressId;
        this.quantity = quantity;
    }
    
    public PackItem(int transactionId, int mattressId, int quantity) {
        this(0, transactionId, mattressId, quantity);
    }
    
    public int getId() { return id; }
    public int getTransactionId() { return transactionId; }
    public int getMattressId() { return mattressId; }
    public int getQuantity() { return quantity; }
    public String getMattressName() { return mattressName; }
    
    public void setId(int id) { this.id = id; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    public void setMattressId(int mattressId) { this.mattressId = mattressId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setMattressName(String mattressName) { this.mattressName = mattressName; }
}

