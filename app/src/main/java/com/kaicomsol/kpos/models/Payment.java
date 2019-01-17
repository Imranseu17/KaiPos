package com.kaicomsol.kpos.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Payment {

    @SerializedName("emergencyValue")
    @Expose
    private Integer emergencyValue;
    @SerializedName("paymentId")
    @Expose
    private Integer paymentId;
    @SerializedName("receipt")
    @Expose
    private Receipt receipt;
    @SerializedName("newHistoryNo")
    @Expose
    private Integer newHistoryNo;

    public Integer getEmergencyValue() {
        return emergencyValue;
    }

    public void setEmergencyValue(Integer emergencyValue) {
        this.emergencyValue = emergencyValue;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public Integer getNewHistoryNo() {
        return newHistoryNo;
    }

    public void setNewHistoryNo(Integer newHistoryNo) {
        this.newHistoryNo = newHistoryNo;
    }
}
