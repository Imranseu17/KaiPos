package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.model.Payment;

public interface PaymentView {

    public void onSuccess(Payment payment);
    public void onError(String error);
}
