package com.kaicomsol.kpos.utils;

public enum ErrorCode {

    LOGOUTERROR(1, 401),
    ERRORCODE500(2, 500),
    ERRORCODE400(3, 500),
    ERRORCODE406(3, 406),
    ERRORCODE412(4, 412);

    private int key;
    private int code;

    ErrorCode(int key, int code) {
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

    public static ErrorCode getByCode(int code) {
        for (ErrorCode rs : ErrorCode.values()) {
            if (rs.getCode()== code) return rs;
        }

        return null;
    }
}
