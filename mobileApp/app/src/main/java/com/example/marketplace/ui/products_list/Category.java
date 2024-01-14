package com.example.marketplace.ui.products_list;

import androidx.annotation.NonNull;

public class Category {
    private int ID;
    private  String name;

    public Category(int id, String name) {
        ID = id;
        this.name = name;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }
}
