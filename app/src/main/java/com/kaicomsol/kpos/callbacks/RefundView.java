package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.models.Refund;
import com.kaicomsol.kpos.models.Success;
import com.kaicomsol.kpos.models.UpdateResponse;

public interface RefundView {

    public void onSuccess(Refund refund);
    public void onSuccess(UpdateResponse updateResponse);
    public void onError(String error);
    public void onLogout(int code);
}
