package com.warehouse.model;

import java.time.LocalDateTime;
import java.time.LocalDate;

public class Transaction {
    private int id;
    private LocalDateTime date;
    private int mattressId;
    private int quantity;
    private String type; // vente, transfert, retour
    private Integer storeOwnerId;
    private int userId;
    private double prix;
    private String notes;
    private LocalDate expectedReturnDate;

    public Transaction(int id, LocalDateTime date, int mattressId, int quantity, String type, Integer storeOwnerId, int userId, double prix, String notes, LocalDate expectedReturnDate) {
        this.id = id;
        this.date = date;
        this.mattressId = mattressId;
        this.quantity = quantity;
        this.type = type;
        this.storeOwnerId = storeOwnerId;
        this.userId = userId;
        this.prix = prix;
        this.notes = notes;
        this.expectedReturnDate = expectedReturnDate;
    }

    public Transaction(LocalDateTime date, int mattressId, int quantity, String type, Integer storeOwnerId, int userId, double prix, String notes, LocalDate expectedReturnDate) {
        this(0, date, mattressId, quantity, type, storeOwnerId, userId, prix, notes, expectedReturnDate);
    }

    public int getId() { return id; }
    public LocalDateTime getDate() { return date; }
    public int getMattressId() { return mattressId; }
    public int getQuantity() { return quantity; }
    public String getType() { return type; }
    public Integer getStoreOwnerId() { return storeOwnerId; }
    public int getUserId() { return userId; }
    public double getPrix() { return prix; }
    public String getNotes() { return notes; }
    public LocalDate getExpectedReturnDate() { return expectedReturnDate; }

    public void setId(int id) { this.id = id; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public void setMattressId(int mattressId) { this.mattressId = mattressId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setType(String type) { this.type = type; }
    public void setStoreOwnerId(Integer storeOwnerId) { this.storeOwnerId = storeOwnerId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setPrix(double prix) { this.prix = prix; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setExpectedReturnDate(LocalDate expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }
} 