package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.model.Refund;
import com.kaicomsol.kpos.model.Success;

public interface RefundView {

    public void onSuccess(Refund refund);
    public void onSuccess(Success message);
    public void onError(String error);
}
