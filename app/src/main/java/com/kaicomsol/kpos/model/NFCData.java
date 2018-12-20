package com.kaicomsol.kpos.model;

import android.nfc.Tag;

import com.kaicomsol.kpos.nfcfelica.HttpResponsAsync;

public class NFCData {


    private HttpResponsAsync.ReadCardArgument argumnet = null;
    private static final NFCData ourInstance = new NFCData();
    private String position = null;
    private String meterSerial = null;
    private Tag tag = null;
    private String customerCode = null;



    public static NFCData getInstance() {
        return ourInstance;
    }

    private NFCData() {
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setArgument(HttpResponsAsync.ReadCardArgument argumnet){
        this.argumnet = argumnet;
    }

    public String getMeterSerial() {
        return meterSerial;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public void setMeterSerial(String meterSerial) {
        this.meterSerial = meterSerial;
    }

    public HttpResponsAsync.ReadCardArgument getArgument(){
        return this.argumnet;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }
}
