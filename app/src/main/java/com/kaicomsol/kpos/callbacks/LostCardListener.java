package com.kaicomsol.kpos.callbacks;

public interface LostCardListener {

    public void onDialogClose();
    public void onLostInfo(String gdNo, String cardNo);
}
