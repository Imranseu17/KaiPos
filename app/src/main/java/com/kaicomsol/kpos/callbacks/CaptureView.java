package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.models.Receipt;

public interface CaptureView {
    public void onCaptureSuccess(int paymentId);
    public void onError(String error, int code);
    public void onLogout(int code);
}
