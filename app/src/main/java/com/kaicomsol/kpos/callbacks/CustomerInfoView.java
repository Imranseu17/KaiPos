package com.kaicomsol.kpos.callbacks;


import com.kaicomsol.kpos.model.CustomerInfo;

public interface CustomerInfoView {

    public void onSuccess(CustomerInfo customerInfo);
    public void onError(String error);
}
