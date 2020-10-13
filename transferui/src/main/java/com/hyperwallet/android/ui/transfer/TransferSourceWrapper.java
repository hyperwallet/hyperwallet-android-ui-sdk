package com.hyperwallet.android.ui.transfer;

import android.os.Parcel;
import android.os.Parcelable;

import com.hyperwallet.android.model.transfermethod.TransferMethod;

public class TransferSourceWrapper implements Parcelable {

    private TransferMethod identification;
    private String type;
    private String token;

    public TransferSourceWrapper() {
    }

    protected TransferSourceWrapper(Parcel in) {
        identification = in.readParcelable(TransferMethod.class.getClassLoader());
        type = in.readString();
        token = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(identification, flags);
        dest.writeString(type);
        dest.writeString(token);
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
