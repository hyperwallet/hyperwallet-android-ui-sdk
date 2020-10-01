package com.hyperwallet.android.ui.transfer;

import android.os.Parcel;
import android.os.Parcelable;

public class TransferSourceWrapper implements Parcelable {

    private String title;
    private String amount;
    private String identification;
    private String type;
    private String token;

    public TransferSourceWrapper() {
    }

    public TransferSourceWrapper(String title, String amount, String identification, String type, String token) {
        this.title = title;
        this.amount = amount;
        this.identification = identification;
        this.type = type;
        this.token = token;
    }

    protected TransferSourceWrapper(Parcel in) {
        title = in.readString();
        amount = in.readString();
        identification = in.readString();
        type = in.readString();
        token = in.readString();
    }

    public static final Creator<TransferSourceWrapper> CREATOR = new Creator<TransferSourceWrapper>() {
        @Override
        public TransferSourceWrapper createFromParcel(Parcel in) {
            return new TransferSourceWrapper(in);
        }

        @Override
        public TransferSourceWrapper[] newArray(int size) {
            return new TransferSourceWrapper[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAmount() {
        return amount;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(amount);
        parcel.writeString(identification);
        parcel.writeString(type);
        parcel.writeString(token);
    }
}
