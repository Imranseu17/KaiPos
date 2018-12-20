package com.kaicomsol.kpos.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CustomerInfo {

    @SerializedName("customerAccountInfo")
    @Expose
    private CustomerAccountInfo customerAccountInfo;

    public CustomerAccountInfo getCustomerAccountInfo() {
        return customerAccountInfo;
    }

    public void setCustomerAccountInfo(CustomerAccountInfo customerAccountInfo) {
        this.customerAccountInfo = customerAccountInfo;
    }
}
