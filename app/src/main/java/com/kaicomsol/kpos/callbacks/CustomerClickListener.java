package com.kaicomsol.kpos.callbacks;


import com.kaicomsol.kpos.models.Customer;

public interface CustomerClickListener {
    public void onCustomerClick(Customer customer);
    public void retryPageLoad();

}
