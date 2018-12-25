package com.binzosoft.lib.recyclerview.model;

public class Item {
    private int itemId;
    private float itemPrice;
    private String itemName;

    public Item() {

    }

    public Item(int itemId, float itemPrice, String itemName) {
        this.itemId = itemId;
        this.itemPrice = itemPrice;
        this.itemName = itemName;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public float getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(float itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}
