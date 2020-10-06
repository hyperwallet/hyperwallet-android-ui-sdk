package com.hyperwallet.android.ui.transfer;

import android.os.Parcel;
import android.os.Parcelable;

import com.hyperwallet.android.model.transfermethod.TransferMethod;

public class TransferSourceWrapper implements Parcelable {

    private String title;
    private String amount;
    private TransferMethod identification;
    private String type;
    private String token;
    private boolean isPrimary = false;

    public TransferSourceWrapper() {
    }

    protected TransferSourceWrapper(Parcel in) {
        title = in.readString();
        amount = in.readString();
        identification = in.readParcelable(TransferMethod.class.getClassLoader());
        type = in.readString();
        token = in.readString();
        isPrimary = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(amount);
        dest.writeParcelable(identification, flags);
        dest.writeString(type);
        dest.writeString(token);
        dest.writeByte((byte) (isPrimary ? 1 : 0));
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

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }


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

    public TransferMethod getIdentification() {
        return identification;
    }

    public void setIdentification(TransferMethod identification) {
        this.identification = identification;
    }


    @Override
    public int describeContents() {
        return 0;
    }

}
