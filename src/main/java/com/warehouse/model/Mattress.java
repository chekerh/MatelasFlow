package com.warehouse.model;

public class Mattress {
    private int id;
    private String type;
    private String size;
    private String brand;
    private int quantity;
    private double prix;

    public Mattress(int id, String type, String size, String brand, int quantity, double prix) {
        this.id = id;
        this.type = type;
        this.size = size;
        this.brand = brand;
        this.quantity = quantity;
        this.prix = prix;
    }

    public Mattress(String type, String size, String brand, int quantity, double prix) {
        this(0, type, size, brand, quantity, prix);
    }

    public int getId() { return id; }
    public String getType() { return type; }
    public String getSize() { return size; }
    public String getBrand() { return brand; }
    public int getQuantity() { return quantity; }
    public double getPrix() { return prix; }

    public void setId(int id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setSize(String size) { this.size = size; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrix(double prix) { this.prix = prix; }
} 