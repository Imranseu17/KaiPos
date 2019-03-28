package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.models.Invoices;
import com.kaicomsol.kpos.models.Payment;
import com.kaicomsol.kpos.models.Receipt;

public interface StateView {

    public void onSuccess(Receipt receipt);
    public void onCaptureSuccess(int paymentId);
    public void onCancelSuccess(int paymentId);
    public void onError(String error, int code);
    public void onLogout(int code);
}
