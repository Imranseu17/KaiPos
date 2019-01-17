package com.kaicomsol.kpos.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CardData {

    @SerializedName("meterCardList")
    @Expose
    private List<MeterCard> meterCards = null;

    public List<MeterCard> getMeterCards() {
        return meterCards;
    }

    public void setMeterCards(List<MeterCard> meterCards) {
        this.meterCards = meterCards;
    }
}
