package com.kaicomsol.kpos.callbacks;


import com.kaicomsol.kpos.models.CustomerInfo;

public interface CustomerInfoView {

    public void onSuccess(CustomerInfo customerInfo);
    public void onError(String error);
    public void onLogout(int code);

}
