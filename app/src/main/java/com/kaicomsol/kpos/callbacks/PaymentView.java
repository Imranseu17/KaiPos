package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.models.Invoices;
import com.kaicomsol.kpos.models.Payment;
import com.kaicomsol.kpos.models.Receipt;

public interface PaymentView {

    public void onSuccess(Payment payment);
    public void onSuccess(Invoices invoices);
    public void onSuccess(String readCard);
    public void onError(String error, int code);
    public void onLogout(int code);
}
