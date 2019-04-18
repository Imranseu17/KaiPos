package com.kaicomsol.kpos.utils;

public enum CardCheck {

    VALID_CARD(1, 200),
    INVALID_CARD(2, 401),
    EXCEPTION_CARD(3, 500);



    private int key;
    private int code;

    CardCheck(int key, int code) {
        this.key = key;
        this.code = code;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static CardCheck getByCode(int code) {
        for (CardCheck rs : CardCheck.values()) {
            if (rs.getCode()== code) return rs;
        }

        return null;
    }
}
