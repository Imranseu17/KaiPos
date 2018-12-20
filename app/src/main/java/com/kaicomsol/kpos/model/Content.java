package com.kaicomsol.kpos.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Content {

    @SerializedName("paymentId")
    @Expose
    private String paymentId;
    @SerializedName("saleDateTimeInLong")
    @Expose
    private long saleDateTimeInLong;
    @SerializedName("customerAccNo")
    @Expose
    private String customerAccNo;
    @SerializedName("meterSerialNo")
    @Expose
    private String meterSerialNo;
    @SerializedName("posId")
    @Expose
    private String posId;
    @SerializedName("posUser")
    @Expose
    private String posUser;
    @SerializedName("amount")
    @Expose
    private int amount;
    @SerializedName("paymentCharge")
    @Expose
    private double paymentCharge;
    @SerializedName("totalAmount")
    @Expose
    private int totalAmount;
    @SerializedName("paymentMethod")
    @Expose
    private String paymentMethod;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public long getSaleDateTimeInLong() {
        return saleDateTimeInLong;
    }

    public void setSaleDateTimeInLong(long saleDateTimeInLong) {
        this.saleDateTimeInLong = saleDateTimeInLong;
    }

    public String getCustomerAccNo() {
        return customerAccNo;
    }

    public void setCustomerAccNo(String customerAccNo) {
        this.customerAccNo = customerAccNo;
    }

    public String getMeterSerialNo() {
        return meterSerialNo;
    }

    public void setMeterSerialNo(String meterSerialNo) {
        this.meterSerialNo = meterSerialNo;
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

    public String getPosUser() {
        return posUser;
    }

    public void setPosUser(String posUser) {
        this.posUser = posUser;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getPaymentCharge() {
        return paymentCharge;
    }

    public void setPaymentCharge(double paymentCharge) {
        this.paymentCharge = paymentCharge;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
