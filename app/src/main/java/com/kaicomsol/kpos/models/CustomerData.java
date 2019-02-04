package com.kaicomsol.kpos.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CustomerData {

    @SerializedName("data")
    @Expose
    private List<Customer> customerList = null;

    @SerializedName("paging")
    @Expose
    private Paging paging;

    public List<Customer> getCustomerList() {
        return customerList;
    }

    public Paging getPaging() {
        return paging;
    }

}
