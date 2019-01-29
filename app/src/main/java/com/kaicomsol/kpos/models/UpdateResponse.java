package com.kaicomsol.kpos.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateResponse {

    @SerializedName("unitPrice")
    @Expose
    private double unitPrice;
    @SerializedName("baseFee")
    @Expose
    private int baseFee;
    @SerializedName("emergencyValue")
    @Expose
    private double emergencyValue;

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getBaseFee() {
        return baseFee;
    }

    public double getEmergencyValue() {
        return emergencyValue;
    }
}
