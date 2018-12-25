package com.kaicomsol.kpos.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Invoices {

    @SerializedName("invoices")
    @Expose
    private List<Invoice> invoices = null;

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }
}
