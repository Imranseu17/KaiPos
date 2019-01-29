package com.kaicomsol.kpos.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CardData {

    @SerializedName("meterCardList")
    @Expose
    private List<MeterCard> meterCardList = null;
    @SerializedName("unitPrice")
    @Expose
    private double unitPrice;
    @SerializedName("basePrice")
    @Expose
    private int basePrice;

    public List<MeterCard> getMeterCards() {
        return meterCardList;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getBasePrice() {
        return basePrice;
    }


}
