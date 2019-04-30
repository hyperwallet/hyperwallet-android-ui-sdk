/*
 * The MIT License (MIT)
 * Copyright (c) 2019 Hyperwallet Systems Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.hyperwallet.android.ui.transfermethod;

import com.hyperwallet.android.model.meta.Fee;

import java.util.List;
import java.util.Objects;

public class TransferMethodSelectionItem {

    private final String mCountry;
    private final String mCurrency;
    private final List<Fee> mFees;
    private final String mProcessingTime;
    private final String mProfile;
    private final String mTransferMethodType;

    public TransferMethodSelectionItem(String country, String currency, String profile, String transferMethodType,
            String processingTime, List<Fee> fees) {
        mCountry = country;
        mCurrency = currency;
        mProfile = profile;
        mTransferMethodType = transferMethodType;
        mProcessingTime = processingTime;
        mFees = fees;
    }

    public String getCountry() {
        return mCountry;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public String getProfile() {
        return mProfile;
    }

    public String getTransferMethodType() {
        return mTransferMethodType;
    }

    public String getProcessingTime() {
        return mProcessingTime;
    }

    public List<Fee> getFees() {
        return mFees;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransferMethodSelectionItem that = (TransferMethodSelectionItem) o;
        return Objects.equals(mCountry, that.mCountry) &&
                Objects.equals(mCurrency, that.mCurrency) &&
                Objects.equals(mProfile, that.mProfile) &&
                Objects.equals(mTransferMethodType, that.mTransferMethodType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mCountry, mCurrency, mProfile, mTransferMethodType);
    }
}
