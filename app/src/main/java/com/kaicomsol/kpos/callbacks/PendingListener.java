package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.models.Customer;

public interface PendingListener {
    public void onClick(int paymentId);
}
