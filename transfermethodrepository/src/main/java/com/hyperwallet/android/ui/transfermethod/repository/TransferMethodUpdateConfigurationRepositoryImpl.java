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
package com.hyperwallet.android.ui.transfermethod.repository;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.graphql.HyperwalletTransferMethodConfigurationField;
import com.hyperwallet.android.model.graphql.HyperwalletTransferMethodConfigurationKey;
import com.hyperwallet.android.model.graphql.query.TransferMethodConfigurationKeysQuery;
import com.hyperwallet.android.model.graphql.query.TransferMethodUpdateConfigurationFieldQuery;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;

import java.util.HashMap;
import java.util.Map;

public class TransferMethodUpdateConfigurationRepositoryImpl implements TransferMethodUpdateConfigurationRepository {
    private final Handler mHandler;
    private final Map<FieldMapKey, HyperwalletTransferMethodConfigurationField> mFieldMap;
    private HyperwalletTransferMethodConfigurationKey mTransferMethodConfigurationKey;

    //todo use default modifier after RepositoryFactory is removed
    public TransferMethodUpdateConfigurationRepositoryImpl() {
        mHandler = new Handler();
        mFieldMap = new HashMap<>();
    }

    @VisibleForTesting()
    protected TransferMethodUpdateConfigurationRepositoryImpl(@Nullable Handler handler,
            HyperwalletTransferMethodConfigurationKey transferMethodConfigurationKey,
            Map<FieldMapKey, HyperwalletTransferMethodConfigurationField> fieldMap) {
        mHandler = handler;
        mTransferMethodConfigurationKey = transferMethodConfigurationKey;
        mFieldMap = fieldMap;
    }

    @VisibleForTesting
    Hyperwallet getHyperwallet() {
        return Hyperwallet.getDefault();
    }

    @VisibleForTesting
    void getTransferMethodConfigurationKeyResult(final LoadKeysCallback loadKeysCallback) {
        TransferMethodConfigurationKeysQuery query = new TransferMethodConfigurationKeysQuery();
        EspressoIdlingResource.increment();

        getHyperwallet().retrieveTransferMethodConfigurationKeys(query,
                new HyperwalletListener<HyperwalletTransferMethodConfigurationKey>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletTransferMethodConfigurationKey result) {
                        mTransferMethodConfigurationKey = result;
                        loadKeysCallback.onKeysLoaded(mTransferMethodConfigurationKey);
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        loadKeysCallback.onError(exception.getErrors());
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });
    }


    @VisibleForTesting
    void getTransferMethodConfigurationFieldResult(
            @NonNull final String transferMethodType,
            @NonNull final LoadFieldsCallback loadFieldsCallback) {
        TransferMethodUpdateConfigurationFieldQuery query = new TransferMethodUpdateConfigurationFieldQuery(
                transferMethodType);
        EspressoIdlingResource.increment();

        getHyperwallet().retrieveUpdateTransferMethodConfigurationFields(
                query,
                new HyperwalletListener<HyperwalletTransferMethodConfigurationField>() {
                    @Override
                    public void onSuccess(HyperwalletTransferMethodConfigurationField result) {
                        FieldMapKey fieldMapKey = new FieldMapKey(transferMethodType);
                        mFieldMap.put(fieldMapKey, result);
                        loadFieldsCallback.onFieldsLoaded(result);
                        EspressoIdlingResource.decrement();
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        loadFieldsCallback.onError(exception.getErrors());
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
        if (mTransferMethodConfigurationKey == null) {
            getTransferMethodConfigurationKeyResult(loadKeysCallback);
        } else {
            loadKeysCallback.onKeysLoaded(mTransferMethodConfigurationKey);
        }
    }

    @Override
    public synchronized void getFields(@NonNull final String transferMethodType,
            @NonNull final LoadFieldsCallback loadFieldsCallback) {

        FieldMapKey fieldMapKey = new FieldMapKey(transferMethodType);
        HyperwalletTransferMethodConfigurationField transferMethodConfigurationField = mFieldMap.get(fieldMapKey);
        // if there is no value for country-currency-type combination,
        // it means api call was never made or this combination or it was refreshed
        if (transferMethodConfigurationField == null) {
            getTransferMethodConfigurationFieldResult(transferMethodType, loadFieldsCallback);
        } else {
            loadFieldsCallback.onFieldsLoaded(transferMethodConfigurationField);
        }
    }

    @Override
    public void refreshKeys() {
        mTransferMethodConfigurationKey = null;
    }

    @Override
    public void refreshFields() {
        mFieldMap.clear();
    }

}