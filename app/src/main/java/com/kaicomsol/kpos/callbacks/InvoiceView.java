package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.models.Invoices;

public interface InvoiceView {

    public void onSuccess(Invoices invoices);
    public void onError(String error);
}
