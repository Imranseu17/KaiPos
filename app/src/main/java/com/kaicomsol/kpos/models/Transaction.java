package com.kaicomsol.kpos.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Transaction {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("invoiceType")
    @Expose
    private String invoiceType;
    @SerializedName("quantity")
    @Expose
    private double quantity;
    @SerializedName("price")
    @Expose
    private double price;
    @SerializedName("amount")
    @Expose
    private double amount;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("percentage")
    @Expose
    private boolean percentage;
    @SerializedName("paymentDate")
    @Expose
    private long paymentDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getPercentage() {
        return percentage;
    }

    public void setPercentage(boolean percentage) {
        this.percentage = percentage;
    }

    public long getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(long paymentDate) {
        this.paymentDate = paymentDate;
    }
}
