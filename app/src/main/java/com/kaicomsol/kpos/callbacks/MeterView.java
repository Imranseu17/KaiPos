package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.models.Meter;

public interface MeterView {

    public void onSuccess(Meter meter);
    public void onError(String error);
    public void onLogout(int code);
}
