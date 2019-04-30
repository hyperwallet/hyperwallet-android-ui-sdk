/*
 * Copyright 2018 Hyperwallet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.hyperwallet.android.ui.repository;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.meta.HyperwalletTransferMethodConfigurationFieldResult;
import com.hyperwallet.android.model.meta.HyperwalletTransferMethodConfigurationKeyResult;
import com.hyperwallet.android.model.meta.query.HyperwalletTransferMethodConfigurationFieldQuery;
import com.hyperwallet.android.model.meta.query.HyperwalletTransferMethodConfigurationKeysQuery;
import com.hyperwallet.android.ui.util.EspressoIdlingResource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TransferMethodConfigurationRepositoryImpl implements TransferMethodConfigurationRepository {
    private static final String INDIVIDUAL = "INDIVIDUAL";
    private HyperwalletTransferMethodConfigurationKeyResult mTransferMethodConfigurationKeyResult;
    private final Handler mHandler;
    private final Map<FieldMapKey, HyperwalletTransferMethodConfigurationFieldResult> mFieldMap;

    TransferMethodConfigurationRepositoryImpl() {
        mHandler = new Handler();
        mFieldMap = new HashMap<>();
    }

    @VisibleForTesting
    Hyperwallet getHyperwallet() {
        return Hyperwallet.getDefault();
    }

    @VisibleForTesting()
    protected TransferMethodConfigurationRepositoryImpl(@Nullable Handler handler,
            HyperwalletTransferMethodConfigurationKeyResult
                    transferMethodConfigurationKeyResult, Map<FieldMapKey,
            HyperwalletTransferMethodConfigurationFieldResult> fieldMap) {
        mHandler = handler;
        mTransferMethodConfigurationKeyResult = transferMethodConfigurationKeyResult;
        mFieldMap = fieldMap;
    }

    @VisibleForTesting
    void getTransferMethodConfigurationKeyResult(final LoadKeysCallback loadKeysCallback) {
        HyperwalletTransferMethodConfigurationKeysQuery query = new HyperwalletTransferMethodConfigurationKeysQuery();
        EspressoIdlingResource.increment();

        getHyperwallet().retrieveTransferMethodConfigurationKeys(query,
                new HyperwalletListener<HyperwalletTransferMethodConfigurationKeyResult>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletTransferMethodConfigurationKeyResult result) {
                        mTransferMethodConfigurationKeyResult = result;
                        loadKeysCallback.onKeysLoaded(mTransferMethodConfigurationKeyResult);
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        loadKeysCallback.onError(exception.getHyperwalletErrors());
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });
    }


    @VisibleForTesting
    void getTransferMethodConfigurationFieldResult(@NonNull final String country,
            @NonNull final String currency,
            @NonNull final String transferMethodType,
            @NonNull final LoadFieldsCallback loadFieldsCallback) {
        HyperwalletTransferMethodConfigurationFieldQuery query =
                new HyperwalletTransferMethodConfigurationFieldQuery(country, currency, transferMethodType, INDIVIDUAL);
        EspressoIdlingResource.increment();

        getHyperwallet().retrieveTransferMethodConfigurationFields(
                query,
                new HyperwalletListener<HyperwalletTransferMethodConfigurationFieldResult>() {
                    @Override
                    public void onSuccess(HyperwalletTransferMethodConfigurationFieldResult result) {
                        FieldMapKey fieldMapKey = new FieldMapKey(country, currency, transferMethodType);
                        mFieldMap.put(fieldMapKey, result);
                        loadFieldsCallback.onFieldsLoaded(result);
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        loadFieldsCallback.onError(exception.getHyperwalletErrors());
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });

    }

    @Override
    public synchronized void getKeys(@NonNull final LoadKeysCallback loadKeysCallback) {
        if (mTransferMethodConfigurationKeyResult == null) {
            getTransferMethodConfigurationKeyResult(loadKeysCallback);
        } else {
            loadKeysCallback.onKeysLoaded(mTransferMethodConfigurationKeyResult);
        }
    }

    @Override
    public synchronized void getFields(@NonNull final String country, @NonNull final String currency,
            @NonNull final String transferMethodType,
            @NonNull final LoadFieldsCallback loadFieldsCallback) {
        FieldMapKey fieldMapKey = new FieldMapKey(country, currency, transferMethodType);
        HyperwalletTransferMethodConfigurationFieldResult transferMethodConfigurationFieldResult = mFieldMap.get(
                fieldMapKey);
        // if there is no value for country-currency-type combination,
        // it means api call was never made or this combination or it was refreshed
        if (transferMethodConfigurationFieldResult == null) {
            getTransferMethodConfigurationFieldResult(country, currency, transferMethodType, loadFieldsCallback);
        } else {
            loadFieldsCallback.onFieldsLoaded(transferMethodConfigurationFieldResult);
        }
    }

    @Override
    public void refreshKeys() {
        mTransferMethodConfigurationKeyResult = null;
    }

    @Override
    public void refreshFields() {
        mFieldMap.clear();
    }
}

class FieldMapKey {
    private final String mCountry;
    private final String mCurrency;
    private final String mTransferMethodType;

    FieldMapKey(String mCountry, String mCurrency, String mTransferMethodType) {
        this.mCountry = mCountry;
        this.mCurrency = mCurrency;
        this.mTransferMethodType = mTransferMethodType;
    }

    private String getCountry() {
        return mCountry;
    }

    private String getCurrency() {
        return mCurrency;
    }

    private String getTransferMethodType() {
        return mTransferMethodType;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof FieldMapKey)) {
            return false;
        }
        FieldMapKey that = (FieldMapKey) o;

        return Objects.equals(mCountry, that.mCountry)
                && Objects.equals(mCurrency, that.mCurrency)
                && Objects.equals(mTransferMethodType, that.mTransferMethodType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mCountry, mCurrency, mTransferMethodType);
    }


}
