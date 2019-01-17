package com.kaicomsol.kpos.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Subscription {

    @SerializedName("itemNo")
    @Expose
    private String itemNo;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("amount")
    @Expose
    private int amount;
    @SerializedName("cycle")
    @Expose
    private String cycle;
    @SerializedName("status")
    @Expose
    private String status;

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
