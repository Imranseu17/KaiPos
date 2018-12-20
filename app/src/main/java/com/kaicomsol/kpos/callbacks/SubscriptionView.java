package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.model.SubData;

public interface SubscriptionView {

    public void onSuccess(SubData subData);
    public void onError(String error);
}
