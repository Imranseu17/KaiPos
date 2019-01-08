package com.kaicomsol.kpos.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Emergency {

    @SerializedName("emergencyValue")
    @Expose
    private int emergencyValue;

    public int getEmergencyValue() {
        return emergencyValue;
    }

    public void setEmergencyValue(int emergencyValue) {
        this.emergencyValue = emergencyValue;
    }
}
