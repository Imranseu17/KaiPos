package com.kaicomsol.kpos.utils;

public enum RechargeStatus {

    PAYMENT_SUCCESS(1,701),
    CANCEL_SUCCESS(2,702),
    CAPTURE_SUCCESS(3,703),
    PAYMENT_ERROR(4,801),
    CANCEL_ERROR(5,802),
    CAPTURE_ERROR(6,803),
    READ_CARD_ERROR(7,804),
    ERROR_CODE_100(8,100),
    ERROR_CODE_406(9,406),
    INVOICE_ERROR(10,805),
    RECEIPT_ERROR(11,806),
    UNKNOWN_ERROR(12,400),
    COMMISSIONED_ERROR(13,412),
    CAPTUREAlREADY_ERROR(14,455);

    private int key;
    private int code;

    RechargeStatus(int key, int code) {
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

    public static RechargeStatus getByCode(int code){
        for(RechargeStatus rs : RechargeStatus.values()){
            if(rs.code==code)return rs;
        }

        return null;


    }
}

