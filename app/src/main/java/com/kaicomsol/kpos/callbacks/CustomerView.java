package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.models.CustomerData;

public interface CustomerView {

    public void onSuccess(CustomerData customerData, int currentPage);
    public void onError(String error);
    public void onLogout(int code);
}
