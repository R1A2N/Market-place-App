package com.example.marketplace.ui.products_list;

import com.google.gson.annotations.SerializedName;

// Product.java
public class Product {
    @SerializedName("ID")
    private int ID;

    @SerializedName("CreatedAt")
    private String createdAt;

    @SerializedName("UpdatedAt")
    private String updatedAt;

    @SerializedName("DeletedAt")
    private String deletedAt;

    @SerializedName("name")
    private String name;

    @SerializedName("desc")
    private String description;

    @SerializedName("image")
    private String image;

    @SerializedName("price")
    private double price;


    @SerializedName("is_available")
    private boolean isAvailable;

    @SerializedName("seller_id")
    private int sellerId;

    @SerializedName("category_id")
    private int categoryId;
    @SerializedName("phone")
    private  String phone;
    public Product(String name, String description, double price, String sellerPhoneNumber) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.phone = sellerPhoneNumber;
    }

    public int getId() {
        return ID;
    }

    public void setId(int id) {
        this.ID = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getSellerPhoneNumber() {
        return phone;
    }

    public void setSellerPhoneNumber(String sellerPhoneNumber) {
        this.phone = sellerPhoneNumber;
    }
// Add getters and setters as needed
}
