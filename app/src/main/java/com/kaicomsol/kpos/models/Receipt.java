package com.kaicomsol.kpos.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Receipt {

    @SerializedName("posId")
    @Expose
    private Integer posId;
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
    private Integer gasUnit;
    @SerializedName("items")
    @Expose
    private Items items;
    @SerializedName("emergencyValue")
    @Expose
    private Integer emergencyValue;
    @SerializedName("amountPaid")
    @Expose
    private Integer amountPaid;
    @SerializedName("paymentMethodName")
    @Expose
    private String paymentMethodName;
    @SerializedName("cardNo")
    @Expose
    private String cardNo;
    @SerializedName("paymentDate")
    @Expose
    private long paymentDate;

    public Integer getPosId() {
        return posId;
    }

    public void setPosId(Integer posId) {
        this.posId = posId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getMeterSerialNo() {
        return meterSerialNo;
    }

    public void setMeterSerialNo(String meterSerialNo) {
        this.meterSerialNo = meterSerialNo;
    }

    public Integer getGasUnit() {
        return gasUnit;
    }

    public void setGasUnit(Integer gasUnit) {
        this.gasUnit = gasUnit;
    }

    public Items getItems() {
        return items;
    }

    public void setItems(Items items) {
        this.items = items;
    }

    public Integer getEmergencyValue() {
        return emergencyValue;
    }

    public void setEmergencyValue(Integer emergencyValue) {
        this.emergencyValue = emergencyValue;
    }

    public Integer getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Integer amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getPaymentMethodName() {
        return paymentMethodName;
    }

    public void setPaymentMethodName(String paymentMethodName) {
        this.paymentMethodName = paymentMethodName;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public long getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(long paymentDate) {
        this.paymentDate = paymentDate;
    }
}
