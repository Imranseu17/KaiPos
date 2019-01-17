package com.kaicomsol.kpos.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Invoice {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("invoiceId")
    @Expose
    private int invoiceId;
    @SerializedName("itemId")
    @Expose
    private int itemId;
    @SerializedName("invoiceType")
    @Expose
    private String invoiceType;
    @SerializedName("itemName")
    @Expose
    private String itemName;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("quantity")
    @Expose
    private double quantity;
    @SerializedName("price")
    @Expose
    private double price;
    @SerializedName("amount")
    @Expose
    private double amount;
    @SerializedName("createDateTime")
    @Expose
    private long createDateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(long createDateTime) {
        this.createDateTime = createDateTime;
    }
}
