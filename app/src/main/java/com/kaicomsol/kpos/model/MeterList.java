package com.kaicomsol.kpos.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MeterList {

    @SerializedName("meterSerialNo")
    @Expose
    private String meterSerialNo;
    @SerializedName("installationDate")
    @Expose
    private long installationDate;
    @SerializedName("meterType")
    @Expose
    private String meterType;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("tariffId")
    @Expose
    private int tariffId;
    @SerializedName("tariff")
    @Expose
    private String tariff;
    @SerializedName("pendingInvoice")
    @Expose
    private boolean pendingInvoice;

    public String getMeterSerialNo() {
        return meterSerialNo;
    }

    public void setMeterSerialNo(String meterSerialNo) {
        this.meterSerialNo = meterSerialNo;
    }

    public long getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(long installationDate) {
        this.installationDate = installationDate;
    }

    public String getMeterType() {
        return meterType;
    }

    public void setMeterType(String meterType) {
        this.meterType = meterType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTariffId() {
        return tariffId;
    }

    public void setTariffId(int tariffId) {
        this.tariffId = tariffId;
    }

    public String getTariff() {
        return tariff;
    }

    public void setTariff(String tariff) {
        this.tariff = tariff;
    }

    public boolean getPendingInvoice() {
        return pendingInvoice;
    }

    public void setPendingInvoice(boolean pendingInvoice) {
        this.pendingInvoice = pendingInvoice;
    }

}
