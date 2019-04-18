package com.kaicomsol.kpos.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TransactionModel implements Parcelable {

    @SerializedName("id")
    @Expose
    private Long id;
    @SerializedName("invoiceType")
    @Expose
    private String invoiceType;
    @SerializedName("quantity")
    @Expose
    private double quantity;
    @SerializedName("price")
    @Expose
    private double price;
    @SerializedName("amount")
    @Expose
    private double amount;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("percentage")
    @Expose
    private boolean percentage;
    @SerializedName("paymentDate")
    @Expose
    private long paymentDate;

    public TransactionModel(Parcel in) {
        id = in.readLong();
        invoiceType = in.readString();
        quantity = in.readDouble();
        price = in.readDouble();
        amount = in.readDouble();
        description = in.readString();
        percentage = in.readByte() != 0;
        paymentDate = in.readLong();
    }

    public static final Creator<TransactionModel> CREATOR = new Creator<TransactionModel>() {
        @Override
        public TransactionModel createFromParcel(Parcel in) {
            return new TransactionModel(in);
        }

        @Override
        public TransactionModel[] newArray(int size) {
            return new TransactionModel[size];
        }
    };

    public TransactionModel() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isPercentage() {
        return percentage;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getPercentage() {
        return percentage;
    }

    public void setPercentage(boolean percentage) {
        this.percentage = percentage;
    }

    public long getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(long paymentDate) {
        this.paymentDate = paymentDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(invoiceType);
        dest.writeDouble(quantity);
        dest.writeDouble(price);
        dest.writeDouble(amount);
        dest.writeString(description);
        dest.writeByte((byte) (percentage ? 1 : 0));
        dest.writeLong(paymentDate);
    }
}
