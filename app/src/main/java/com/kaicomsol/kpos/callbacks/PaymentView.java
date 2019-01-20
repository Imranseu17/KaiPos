package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.models.Invoices;
import com.kaicomsol.kpos.models.Payment;

public interface PaymentView {

    public void onSuccess(Payment payment, int code);
    public void onSuccess(Invoices invoices);
    public void onSuccess(int paymentId);
    public void onSuccess(String readCard);
    public void onError(String error, int code);
    public void onLogout(int code);
}
