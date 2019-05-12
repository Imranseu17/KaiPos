package com.kaicomsol.kpos.callbacks;


import com.kaicomsol.kpos.models.Payment;
import com.kaicomsol.kpos.models.Receipt;

public interface AuthorizeView {

    public void onSuccess(Payment payment);
    public void onSuccess(Receipt receipt);
    public void onError(String error, int code);
    public void onLogout(int code);
    public void onCaptureSuccess(int paymentID);

}
