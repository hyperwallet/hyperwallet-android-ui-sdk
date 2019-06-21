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
package com.hyperwallet.android.ui.transfermethod;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.graphql.HyperwalletTransferMethodConfigurationField;
import com.hyperwallet.android.model.graphql.ProcessingTime;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.repository.TransferMethodConfigurationRepository;
import com.hyperwallet.android.ui.repository.TransferMethodRepository;

public class AddTransferMethodPresenter implements AddTransferMethodContract.Presenter {

    private static final String TAG = AddTransferMethodContract.class.getName();
    private final TransferMethodConfigurationRepository mTransferMethodConfigurationRepository;
    private final TransferMethodRepository mTransferMethodRepository;
    private final AddTransferMethodContract.View mView;

    public AddTransferMethodPresenter(AddTransferMethodContract.View view,
            TransferMethodConfigurationRepository transferMethodConfigurationRepository,
            TransferMethodRepository transferMethodRepository) {
        mView = view;
        mTransferMethodConfigurationRepository = transferMethodConfigurationRepository;
        mTransferMethodRepository = transferMethodRepository;
    }

    @Override
    public void createTransferMethod(@NonNull final HyperwalletTransferMethod transferMethod) {
        mView.showCreateButtonProgressBar();
        mTransferMethodRepository.createTransferMethod(transferMethod,
                new TransferMethodRepository.LoadTransferMethodCallback() {
                    @Override
                    public void onTransferMethodLoaded(HyperwalletTransferMethod transferMethod) {

                        if (!mView.isActive()) {
                            return;
                        }
                        mView.hideCreateButtonProgressBar();
                        mView.notifyTransferMethodAdded(transferMethod);
                    }

                    @Override
                    public void onError(HyperwalletErrors errors) {
                        if (!mView.isActive()) {
                            return;
                        }

                        mView.hideCreateButtonProgressBar();
                        if (errors.containsInputError()) {
                            mView.showInputErrors(errors.getErrors());
                        } else {
                            mView.showErrorAddTransferMethod(errors.getErrors());
                        }
                    }
                });
    }

    @Override
    public void loadTransferMethodConfigurationFields(final boolean forceUpdate, @NonNull final String country,
            @NonNull final String currency, @NonNull final String transferMethodType,
            @NonNull final String transferMethodProfileType) {
        mView.showProgressBar();

        if (forceUpdate) {
            mTransferMethodConfigurationRepository.refreshFields();
        }

        mTransferMethodConfigurationRepository.getFields(
                country, currency, transferMethodType, transferMethodProfileType,
                new TransferMethodConfigurationRepository.LoadFieldsCallback() {
                    @Override
                    public void onFieldsLoaded(HyperwalletTransferMethodConfigurationField field,
                            @Nullable final ProcessingTime processingTime) {
                        if (!mView.isActive()) {
                            return;
                        }

                        mView.hideProgressBar();
                        mView.showTransferMethodFields(field.getFields().getFieldGroups());
                        // there can be multiple fees when we have flat fee + percentage fees
                        mView.showTransactionInformation(field.getFees(), processingTime);
                    }

                    @Override
                    public void onError(@NonNull HyperwalletErrors errors) {
                        if (!mView.isActive()) {
                            return;
                        }
                        mView.hideProgressBar();
                        mView.showErrorLoadTransferMethodConfigurationFields(errors.getErrors());
                    }
                });
    }
}
