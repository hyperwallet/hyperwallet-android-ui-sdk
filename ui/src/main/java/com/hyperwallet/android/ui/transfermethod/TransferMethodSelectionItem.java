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

import androidx.annotation.NonNull;

import com.hyperwallet.android.model.graphql.HyperwalletFee;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TransferMethodSelectionItem {

    private final String mCountry;
    private final String mCurrency;
    private final List<HyperwalletFee> mFees;
    private final String mProcessingTime;
    private final String mProfileType;
    private final String mTransferMethodType;
    private final String mTransferMethodName;

    public TransferMethodSelectionItem(@NonNull final String country, @NonNull final String currency,
            @NonNull final String profileType, @NonNull final String transferMethodType,
            @NonNull final String transferMethodName, @NonNull final String processingTime,
            @NonNull final Set<HyperwalletFee> fees) {
        mCountry = country;
        mCurrency = currency;
        mProfileType = profileType;
        mTransferMethodType = transferMethodType;
        mTransferMethodName = transferMethodName;
        mProcessingTime = processingTime;
        mFees = new ArrayList<>(fees);
    }

    public String getCountry() {
        return mCountry;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public String getProfileType() {
        return mProfileType;
    }

    public String getTransferMethodName() {
        return mTransferMethodName;
    }

    public String getTransferMethodType() {
        return mTransferMethodType;
    }

    public String getProcessingTime() {
        return mProcessingTime;
    }

    public List<HyperwalletFee> getFees() {
        return mFees;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransferMethodSelectionItem that = (TransferMethodSelectionItem) o;
        return Objects.equals(mCountry, that.mCountry) &&
                Objects.equals(mCurrency, that.mCurrency) &&
                Objects.equals(mProfileType, that.mProfileType) &&
                Objects.equals(mTransferMethodType, that.mTransferMethodType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mCountry, mCurrency, mProfileType, mTransferMethodType);
    }
}
