package com.kaicomsol.kpos.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Like implements Parcelable {

    public String account;
    public String customerCode;
    public String meterSerial;
    public String cardNo;
    public String phone;
    public String zone;
    public String address;
    public String apartment;
    public String area;

    public Like() {
    }


    protected Like(Parcel in) {
        account = in.readString();
        customerCode = in.readString();
        meterSerial = in.readString();
        cardNo = in.readString();
        phone = in.readString();
        zone = in.readString();
        address = in.readString();
        apartment = in.readString();
        area = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(account);
        dest.writeString(customerCode);
        dest.writeString(meterSerial);
        dest.writeString(cardNo);
        dest.writeString(phone);
        dest.writeString(zone);
        dest.writeString(address);
        dest.writeString(apartment);
        dest.writeString(area);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Like> CREATOR = new Creator<Like>() {
        @Override
        public Like createFromParcel(Parcel in) {
            return new Like(in);
        }

        @Override
        public Like[] newArray(int size) {
            return new Like[size];
        }
    };
}
