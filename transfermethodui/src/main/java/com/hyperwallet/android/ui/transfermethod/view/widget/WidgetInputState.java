/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Hyperwallet Systems Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.hyperwallet.android.ui.transfermethod.view.widget;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class WidgetInputState implements Parcelable, Serializable {

    public static final Creator<WidgetInputState> CREATOR = new Creator<WidgetInputState>() {

        @Override
        public WidgetInputState createFromParcel(Parcel in) {
            return new WidgetInputState(in);
        }

        @Override
        public WidgetInputState[] newArray(int size) {
            return new WidgetInputState[size];
        }
    };
    private static final long serialVersionUID = -8490513063328896560L;

    private final String mFieldName;
    private String mErrorMessage;
    private boolean mHasFocused;
    private boolean mHasApiError;
    private String mSelectedName;
    private String mValue;

    protected WidgetInputState(@NonNull final String fieldName) {
        mFieldName = fieldName;
    }

    private WidgetInputState(Parcel in) {
        mErrorMessage = in.readString();
        mHasFocused = in.readByte() != 0;
        mFieldName = in.readString();
        mHasApiError = in.readByte() != 0;
        mSelectedName = in.readString();
        mValue = in.readString();
    }

    public boolean hasFocused() {
        return mHasFocused;
    }

    public void setHasFocused(boolean hasFocused) {
        this.mHasFocused = hasFocused;
    }

    public boolean hasApiError() {
        return mHasApiError;
    }

    public void setHasApiError(boolean hasApiError) {
        this.mHasApiError = hasApiError;
    }

    @Nullable
    public String getErrorMessage() {
        return mErrorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.mErrorMessage = errorMessage;
    }

    @Nullable
    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    @NonNull
    public String getSelectedName() {
        return mSelectedName == null ? "" : mSelectedName;
    }

    public void setSelectedName(@NonNull final String selectedName) {
        mSelectedName = selectedName;
    }

    @NonNull
    public String getFieldName() {
        return mFieldName;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mErrorMessage);
        dest.writeByte((byte) (mHasFocused ? 1 : 0));
        dest.writeString(mFieldName);
        dest.writeByte((byte) (mHasApiError ? 1 : 0));
        dest.writeString(mSelectedName);
        dest.writeString(mValue);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WidgetInputState that = (WidgetInputState) o;
        return mHasFocused == that.mHasFocused &&
                mHasApiError == that.mHasApiError &&
                Objects.equals(mErrorMessage, that.mErrorMessage) &&
                Objects.equals(mFieldName, that.mFieldName) &&
                Objects.equals(mSelectedName, that.mSelectedName) &&
                Objects.equals(mValue, that.mValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mErrorMessage, mHasFocused, mFieldName, mHasApiError, mSelectedName, mValue);
    }
}
