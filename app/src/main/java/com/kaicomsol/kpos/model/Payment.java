package com.kaicomsol.kpos.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Payment {

    @SerializedName("paymentId")
    @Expose
    private int paymentId;
    @SerializedName("newHistoryNo")
    @Expose
    private int newHistoryNo;

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public int getNewHistoryNo() {
        return newHistoryNo;
    }

    public void setNewHistoryNo(Integer newHistoryNo) {
        this.newHistoryNo = newHistoryNo;
    }
}
