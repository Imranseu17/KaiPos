package com.kaicomsol.kpos.models;

public class CustomerProperties {

    public String label;
    public String value;

    public CustomerProperties(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }


}
