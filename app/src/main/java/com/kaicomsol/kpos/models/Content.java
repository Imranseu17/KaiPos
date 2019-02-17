package com.kaicomsol.kpos.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Content implements Parcelable {

    @SerializedName("paymentId")
    @Expose
    private String paymentId;
    @SerializedName("saleDateTimeInLong")
    @Expose
    private long saleDateTimeInLong;
    @SerializedName("customerAccNo")
    @Expose
    private String customerAccNo;
    @SerializedName("meterSerialNo")
    @Expose
    private String meterSerialNo;
    @SerializedName("posId")
    @Expose
    private String posId;
    @SerializedName("posUser")
    @Expose
    private String posUser;
    @SerializedName("amount")
    @Expose
    private double amount;
    @SerializedName("paymentCharge")
    @Expose
    private double paymentCharge;
    @SerializedName("totalAmount")
    @Expose
    private double totalAmount;
    @SerializedName("paymentMethod")
    @Expose
    private String paymentMethod;


    public Content() {
    }

    protected Content(Parcel in) {
        paymentId = in.readString();
        saleDateTimeInLong = in.readLong();
        customerAccNo = in.readString();
        meterSerialNo = in.readString();
        posId = in.readString();
        posUser = in.readString();
        amount = in.readDouble();
        paymentCharge = in.readDouble();
        totalAmount = in.readDouble();
        paymentMethod = in.readString();
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public long getSaleDateTimeInLong() {
        return saleDateTimeInLong;
    }

    public void setSaleDateTimeInLong(long saleDateTimeInLong) {
        this.saleDateTimeInLong = saleDateTimeInLong;
    }

    public String getCustomerAccNo() {
        return customerAccNo;
    }

    public void setCustomerAccNo(String customerAccNo) {
        this.customerAccNo = customerAccNo;
    }

    public String getMeterSerialNo() {
        return meterSerialNo;
    }

    public void setMeterSerialNo(String meterSerialNo) {
        this.meterSerialNo = meterSerialNo;
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

    public String getPosUser() {
        return posUser;
    }

    public void setPosUser(String posUser) {
        this.posUser = posUser;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getPaymentCharge() {
        return paymentCharge;
    }

    public void setPaymentCharge(double paymentCharge) {
        this.paymentCharge = paymentCharge;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public static Creator<Content> getCREATOR() {
        return CREATOR;
    }

    public static final Creator<Content> CREATOR = new Creator<Content>() {
        @Override
        public Content createFromParcel(Parcel in) {
            return new Content(in);
        }

        @Override
        public Content[] newArray(int size) {
            return new Content[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(paymentId);
        dest.writeLong(saleDateTimeInLong);
        dest.writeString(customerAccNo);
        dest.writeString(meterSerialNo);
        dest.writeString(posId);
        dest.writeString(posUser);
        dest.writeDouble(amount);
        dest.writeDouble(paymentCharge);
        dest.writeDouble(totalAmount);
        dest.writeString(paymentMethod);
    }
}
