package com.kaicomsol.kpos.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MeterList {

    @SerializedName("meterSerialNo")
    @Expose
    private String meterSerialNo;
    @SerializedName("installationDate")
    @Expose
    private long installationDate;
    @SerializedName("meterTypeId")
    @Expose
    private int meterTypeId;
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

    public long getInstallationDate() {
        return installationDate;
    }

    public int getMeterTypeId() {
        return meterTypeId;
    }

    public String getMeterType() {
        return meterType;
    }

    public String getStatus() {
        return status;
    }

    public int getTariffId() {
        return tariffId;
    }

    public String getTariff() {
        return tariff;
    }

    public boolean isPendingInvoice() {
        return pendingInvoice;
    }
}
