package com.kaicomsol.kpos.models;

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

    public String getAccountNo() {
        return accountNo;
    }

    public String getCardNo() {
        return cardNo;
    }

    public String getErpCode() {
        return erpCode;
    }

    public String getMetro() {
        return metro;
    }

    public String getZone() {
        return zone;
    }

    public String getArea() {
        return area;
    }

    public String getSubArea() {
        return subArea;
    }

    public String getAddress() {
        return address;
    }

    public String getApartment() {
        return apartment;
    }

    public String getStatus() {
        return status;
    }

    public int getBalance() {
        return balance;
    }

    public String getMeterSerial() {
        return meterSerial;
    }
}
