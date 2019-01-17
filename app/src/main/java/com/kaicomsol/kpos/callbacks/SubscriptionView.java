package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.models.SubData;

public interface SubscriptionView {

    public void onSuccess(SubData subData);
    public void onError(String error);
    public void onLogout(int code);
}
