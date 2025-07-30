package com.warehouse.model;

public class StoreOwner {
    private int id;
    private String name;
    private String contact;

    public StoreOwner(int id, String name, String contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
    }

    public StoreOwner(String name, String contact) {
        this(0, name, contact);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getContact() { return contact; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setContact(String contact) { this.contact = contact; }
} 