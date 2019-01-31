package com.kaicomsol.kpos.utils;

public enum CardPropertise {

    CUSTOMER_CARD(1, "77"),
    SERVICE_CARD(2, "88"),
    CARD_RECHARGED(3, "15"),
    CARD_REFUNDED(4, "05"),
    CARD_CHARGED_METER(5, "06"),
    CARD_INITIAL(5, "30");


    private int key;
    private String code;

    CardPropertise(int key, String code) {
        this.key = key;
        this.code = code;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static CardPropertise getByCode(String code) {
        for (CardPropertise rs : CardPropertise.values()) {
            if (rs.getCode().equals(code)) return rs;
        }

        return null;
    }

}