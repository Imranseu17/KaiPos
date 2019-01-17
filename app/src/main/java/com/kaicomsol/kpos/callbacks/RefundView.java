package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.models.Refund;
import com.kaicomsol.kpos.models.Success;

public interface RefundView {

    public void onSuccess(Refund refund);
    public void onSuccess(Success message);
    public void onError(String error);
    public void onLogout(int code);
}
