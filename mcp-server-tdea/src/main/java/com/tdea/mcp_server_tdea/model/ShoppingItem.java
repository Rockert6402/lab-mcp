package com.tdea.mcp_server_tdea.model;

public class ShoppingItem {
    private String name;
    private int quantity;
    private String category;
    private double price;
    private String brand;
    private String description;

    public ShoppingItem() { }

    public ShoppingItem(String name, int quantity, String category, double price, String brand, String description) {
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.price = price;
        this.brand = brand;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
