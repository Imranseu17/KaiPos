package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.model.Login;

public interface PosDeviceView {

    public void onSuccess(String success);
    public void onError(String error);
}
