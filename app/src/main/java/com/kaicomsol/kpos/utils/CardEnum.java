package com.kaicomsol.kpos.utils;

public enum  CardEnum {

    ADD_CARD_SUCCESS(1,201),
    ADD_CARD_FAILED(1,401),
    ACTIVE_CARD_SUCCESS(1,202),
    ACTIVE_CARD_FAILED(1,402),
    LOST_CARD_SUCCESS(1,203),
    LOST_CARD_FAILED(1,403),
    DELETE_CARD_SUCCESS(1,204),
    DELETE_CARD_FAILED(1,404),
    EMERGENCY_VALUE_FAILED(1,405),
    GET_METER_INFO_FAILED(1,406),
    DAMAGE_CARD_SUCCESS(1,205),
    DAMAGE_CARD_FAILED(1,407);


    private int key;
    private int code;

    CardEnum(int key, int code) {
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

    public static CardEnum getByCode(int code){
        for(CardEnum rs :CardEnum.values()){
            if(rs.code==code)return rs;
        }

        return null;


    }
}
