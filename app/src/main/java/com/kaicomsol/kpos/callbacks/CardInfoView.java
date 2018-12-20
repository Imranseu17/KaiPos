package com.kaicomsol.kpos.callbacks;


import com.kaicomsol.kpos.model.CardData;

public interface CardInfoView {

    public void onCard(CardData cardData);
    public void onAddCard(boolean isAdded);
    public void onActiveCard(String active);
    public void onDeleteCard(boolean isDelete);
    public void onLostCard(String lost);
    public void onError(String error);
}
