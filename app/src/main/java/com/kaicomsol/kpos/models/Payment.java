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
    @SerializedName("unitPrice")
    @Expose
    private Double unitPrice;
    @SerializedName("baseFee")
    @Expose
    private Integer baseFee;

    public Integer getEmergencyValue() {
        return emergencyValue;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public Integer getNewHistoryNo() {
        return newHistoryNo;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public Integer getBaseFee() {
        return baseFee;
    }
}
