package com.hyperwallet.android.ui.transfer;

import android.os.Parcel;
import android.os.Parcelable;

import com.hyperwallet.android.model.transfermethod.TransferMethod;

public class TransferSource implements Parcelable {

    private TransferMethod identification;
    private String type;
    private String token;

    public TransferSource() {
    }

    protected TransferSource(final Parcel in) {
        identification = in.readParcelable(TransferMethod.class.getClassLoader());
        type = in.readString();
        token = in.readString();
    }

    @Override
    public void writeToParcel(final Parcel dest, int flags) {
        dest.writeParcelable(identification, flags);
        dest.writeString(type);
        dest.writeString(token);
    }

    public static final Creator<TransferSource> CREATOR = new Creator<TransferSource>() {
        @Override
        public TransferSource createFromParcel(Parcel in) {
            return new TransferSource(in);
        }

        @Override
        public TransferSource[] newArray(int size) {
            return new TransferSource[size];
        }
    };


    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public TransferMethod getIdentification() {
        return identification;
    }

    public void setIdentification(final TransferMethod identification) {
        this.identification = identification;
    }


    @Override
    public int describeContents() {
        return 0;
    }

}
