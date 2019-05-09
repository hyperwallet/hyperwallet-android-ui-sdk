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
package com.hyperwallet.android.ui.repository;

import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodTypes.BANK_ACCOUNT;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodTypes.BANK_CARD;
import static com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodTypes.PAYPAL_ACCOUNT;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.HyperwalletBankAccount;
import com.hyperwallet.android.model.HyperwalletBankCard;
import com.hyperwallet.android.model.HyperwalletStatusTransition;
import com.hyperwallet.android.model.HyperwalletTransferMethod;
import com.hyperwallet.android.model.PayPalAccount;
import com.hyperwallet.android.model.paging.HyperwalletPageList;

public class TransferMethodRepositoryImpl implements TransferMethodRepository {

    private Handler mHandler = new Handler();

    @VisibleForTesting
    Hyperwallet getHyperwallet() {
        return Hyperwallet.getDefault();
    }

    @Override
    public void createTransferMethod(final HyperwalletTransferMethod transferMethod,
            LoadTransferMethodCallback callback) {
        switch (transferMethod.getField(TYPE)) {
            case BANK_ACCOUNT:
                createBankAccount(transferMethod, callback);
                break;
            case BANK_CARD:
                createBankCard(transferMethod, callback);
                break;
            case PAYPAL_ACCOUNT:
                createPayPalAccount(transferMethod, callback);
                break;
            default: //no default action
        }
    }

    @Override
    public void loadTransferMethod(@NonNull final LoadTransferMethodListCallback callback) {
        getHyperwallet().listTransferMethods(null,
                new HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletPageList<HyperwalletTransferMethod> result) {
                        callback.onTransferMethodListLoaded(result != null ? result.getDataList() : null);
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        callback.onError(exception.getHyperwalletErrors());
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });
    }

    @Override
    public void deactivateTransferMethod(@NonNull final HyperwalletTransferMethod transferMethod,
            @NonNull final DeactivateTransferMethodCallback callback) {
        switch (transferMethod.getField(TYPE)) {
            case BANK_ACCOUNT:
                deactivateBankAccount(transferMethod, callback);
                break;
            case BANK_CARD:
                deactivateBankCardAccount(transferMethod, callback);
            default: //no default action
        }
    }

    private void deactivateBankAccount(@NonNull final HyperwalletTransferMethod transferMethod,
            @NonNull final DeactivateTransferMethodCallback callback) {
        getHyperwallet().deactivateBankAccount(transferMethod.getField(TOKEN), null,
                new HyperwalletListener<HyperwalletStatusTransition>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletStatusTransition result) {
                        callback.onTransferMethodDeactivated(result);
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        callback.onError(exception.getHyperwalletErrors());
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });
    }

    private void deactivateBankCardAccount(@NonNull final HyperwalletTransferMethod transferMethod,
            @NonNull final DeactivateTransferMethodCallback callback) {
        getHyperwallet().deactivateBankCard(transferMethod.getField(TOKEN), null,
                new HyperwalletListener<HyperwalletStatusTransition>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletStatusTransition result) {
                        callback.onTransferMethodDeactivated(result);
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        callback.onError(exception.getHyperwalletErrors());
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });
    }

    private void createBankAccount(final HyperwalletTransferMethod transferMethod,
            final LoadTransferMethodCallback callback) {
        HyperwalletBankAccount bankAccount = (HyperwalletBankAccount) transferMethod;

        getHyperwallet().createBankAccount(bankAccount, new HyperwalletListener<HyperwalletBankAccount>() {
            @Override
            public void onSuccess(@Nullable HyperwalletBankAccount result) {
                callback.onTransferMethodLoaded(result);
            }

            @Override
            public void onFailure(HyperwalletException exception) {
                callback.onError(exception.getHyperwalletErrors());
            }

            @Override
            public Handler getHandler() {
                return mHandler;
            }
        });
    }

    private void createBankCard(@NonNull final HyperwalletTransferMethod transferMethod,
            @NonNull final LoadTransferMethodCallback callback) {
        HyperwalletBankCard bankCard = (HyperwalletBankCard) transferMethod;

        getHyperwallet().createBankCard(bankCard, new HyperwalletListener<HyperwalletBankCard>() {
            @Override
            public void onSuccess(@Nullable HyperwalletBankCard result) {
                callback.onTransferMethodLoaded(result);
            }

            @Override
            public void onFailure(HyperwalletException exception) {
                callback.onError(exception.getHyperwalletErrors());
            }

            @Override
            public Handler getHandler() {
                return mHandler;
            }
        });
    }

    private void createPayPalAccount(@NonNull final HyperwalletTransferMethod transferMethod,
            @NonNull final LoadTransferMethodCallback callback) {
        PayPalAccount payPalAccount = (PayPalAccount) transferMethod;

        getHyperwallet().createPayPalAccount(payPalAccount, new HyperwalletListener<PayPalAccount>() {
            @Override
            public void onSuccess(@Nullable PayPalAccount result) {
                callback.onTransferMethodLoaded(result);
            }

            @Override
            public void onFailure(HyperwalletException exception) {
                callback.onError(exception.getHyperwalletErrors());
            }

            @Override
            public Handler getHandler() {
                return mHandler;
            }
        });
    }
}
