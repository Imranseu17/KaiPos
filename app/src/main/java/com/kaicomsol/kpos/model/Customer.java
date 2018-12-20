package com.kaicomsol.kpos.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Customer {

    @SerializedName("customerCode")
    @Expose
    private String customerCode;
    @SerializedName("accountNo")
    @Expose
    private String accountNo;
    @SerializedName("cardNo")
    @Expose
    private String cardNo;
    @SerializedName("erpCode")
    @Expose
    private String erpCode;
    @SerializedName("metro")
    @Expose
    private String metro;
    @SerializedName("zone")
    @Expose
    private String zone;
    @SerializedName("area")
    @Expose
    private String area;
    @SerializedName("subArea")
    @Expose
    private String subArea;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("apartment")
    @Expose
    private String apartment;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("balance")
    @Expose
    private int balance;
    @SerializedName("meterSerial")
    @Expose
    private String meterSerial;

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getErpCode() {
        return erpCode;
    }

    public void setErpCode(String erpCode) {
        this.erpCode = erpCode;
    }

    public String getMetro() {
        return metro;
    }

    public void setMetro(String metro) {
        this.metro = metro;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getSubArea() {
        return subArea;
    }

    public void setSubArea(String subArea) {
        this.subArea = subArea;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getMeterSerial() {
        return meterSerial;
    }

    public void setMeterSerial(String meterSerial) {
        this.meterSerial = meterSerial;
    }
}
