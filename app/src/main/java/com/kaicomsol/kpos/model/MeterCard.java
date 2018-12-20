package com.kaicomsol.kpos.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MeterCard {

    @SerializedName("cardNumber")
    @Expose
    private String cardNumber;
    @SerializedName("meterSerialNo")
    @Expose
    private String meterSerialNo;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("issueDate")
    @Expose
    private long issueDate;
    @SerializedName("lostDate")
    @Expose
    private String lostDate;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getMeterSerialNo() {
        return meterSerialNo;
    }

    public void setMeterSerialNo(String meterSerialNo) {
        this.meterSerialNo = meterSerialNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(long issueDate) {
        this.issueDate = issueDate;
    }

    public String getLostDate() {
        return lostDate;
    }

    public void setLostDate(String lostDate) {
        this.lostDate = lostDate;
    }
}
