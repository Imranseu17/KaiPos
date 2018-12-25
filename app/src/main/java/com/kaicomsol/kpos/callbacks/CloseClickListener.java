package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.model.Customer;

public interface CloseClickListener {
    public void onCloseClick(int id);
    public void onCloseClick(double amount);
}
