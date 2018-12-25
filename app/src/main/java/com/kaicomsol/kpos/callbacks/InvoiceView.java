package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.model.Invoices;
import com.kaicomsol.kpos.model.Meter;

public interface InvoiceView {

    public void onSuccess(Invoices invoices);
    public void onError(String error);
}
