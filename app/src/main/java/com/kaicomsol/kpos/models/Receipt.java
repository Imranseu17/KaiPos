package com.kaicomsol.kpos.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Receipt {

    @SerializedName("prePaidCode")
    @Expose
    private String prePaidCode;
    @SerializedName("posId")
    @Expose
    private long posId;
    @SerializedName("paymentId")
    @Expose
    private long paymentId;
    @SerializedName("paymentStatus")
    @Expose
    private String paymentStatus;
    @SerializedName("operatorName")
    @Expose
    private String operatorName;
    @SerializedName("meterSerialNo")
    @Expose
    private String meterSerialNo;
    @SerializedName("gasUnit")
    @Expose
    private double gasUnit;
    @SerializedName("items")
    @Expose
    private Items items;
    @SerializedName("emergencyValue")
    @Expose
    private Integer emergencyValue;
    @SerializedName("amountPaid")
    @Expose
    private double amountPaid;
    @SerializedName("paymentMethodName")
    @Expose
    private String paymentMethodName;
    @SerializedName("cardNo")
    @Expose
    private String cardNo;
    @SerializedName("paymentDate")
    @Expose
    private long paymentDate;

    public long getPosId() {
        return posId;
    }

    public long getPaymentId() {
        return paymentId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public String getMeterSerialNo() {
        return meterSerialNo;
    }

    public double getGasUnit() {
        return gasUnit;
    }

    public Items getItems() {
        return items;
    }

    public Integer getEmergencyValue() {
        return emergencyValue;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public String getPaymentMethodName() {
        return paymentMethodName;
    }

    public String getCardNo() {
        return cardNo;
    }

    public long getPaymentDate() {
        return paymentDate;
    }

    public String getPrePaidCode() {
        return prePaidCode;
    }
}
