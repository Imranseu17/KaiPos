package com.kaicomsol.kpos.utils;

public enum WorkStatus {
    Latest(1,"New"),
    Dispatched(2,"Dispatched"),
    Pending(3,"Pending"),
    Cancelled(4,"Cancelled"),
    Complete(5,"Complete");
    private int intValue;
    private String value;

    WorkStatus(int intValue, String value) {
        this.intValue = intValue;
        this.value = value;
    }

    public int getIntValue() {
        return intValue;
    }

    public String getValue() {
        return value;
    }
}
