package com.kaicomsol.kpos.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PaymentID {

    @SerializedName("paymentId")
    @Expose
    private Integer paymentId;

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }
}
