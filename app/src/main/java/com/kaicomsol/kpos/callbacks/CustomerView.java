package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.model.CustomerData;

public interface CustomerView {

    public void onSuccess(CustomerData customerData);
    public void onError(String error);
}
