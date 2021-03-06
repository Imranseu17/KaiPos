package com.kaicomsol.kpos.callbacks;


import com.kaicomsol.kpos.models.CardData;

public interface CardInfoView {

    public void onEmergencyValue(int emergencyValue);
    public void onCard(CardData cardData);
    public void onAddCard(boolean isAdded);
    public void onActiveCard(String active);
    public void onDeleteCard(boolean isDelete);
    public void onLostCard(String lost);
    public void onDamageCard(String damage);
    public void onError(String error,int code);
    public void onLogout(int code);
}
