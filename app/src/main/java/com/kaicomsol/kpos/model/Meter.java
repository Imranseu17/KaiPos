package com.kaicomsol.kpos.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Meter {

    @SerializedName("meterList")
    @Expose
    private List<MeterList> meterList = null;

    public List<MeterList> getMeterList() {
        return meterList;
    }

    public void setMeterList(List<MeterList> meterList) {
        this.meterList = meterList;
    }
}
