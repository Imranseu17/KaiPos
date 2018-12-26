package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.model.Invoices;
import com.kaicomsol.kpos.model.Payment;

public interface PaymentView {

    public void onSuccess(Payment payment);
    public void onSuccess(Invoices invoices);
    public void onSuccess(int paymentId);
    public void onError(String error, int code);
    public void onLogout(int code);
}
