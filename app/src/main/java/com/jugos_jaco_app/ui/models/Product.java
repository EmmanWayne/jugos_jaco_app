package com.jugos_jaco_app.ui.models;

import java.io.Serializable;

public class Product implements Serializable {
    private String id;
    private String name;
    private String code;
    private String content;
    private String categoryId;
    private String categoryName;
    private double price;  // Aunque no está en tu schema, probablemente lo necesites
    private String imageUrl;
    
    // Constructor
    public Product(String id, String name, String code, String content, 
                  String categoryId, String categoryName, double price, String imageUrl) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.content = content;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getContent() {
        return content;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
} 