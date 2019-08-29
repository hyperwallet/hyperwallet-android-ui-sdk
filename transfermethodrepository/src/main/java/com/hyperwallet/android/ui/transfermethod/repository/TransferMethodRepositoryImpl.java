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
package com.hyperwallet.android.ui.transfermethod.repository;

import static com.hyperwallet.android.model.StatusTransition.StatusDefinition.ACTIVATED;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.BANK_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.BANK_CARD;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.PAYPAL_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes.WIRE_ACCOUNT;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.paging.HyperwalletPageList;
import com.hyperwallet.android.model.transfermethod.HyperwalletBankAccount;
import com.hyperwallet.android.model.transfermethod.HyperwalletBankCard;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethodQueryParam;
import com.hyperwallet.android.model.transfermethod.PayPalAccount;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;

public class TransferMethodRepositoryImpl implements TransferMethodRepository {

    private static final short QUERY_SINGLE_RESULT = 1;
    private Handler mHandler = new Handler();

    @VisibleForTesting
    Hyperwallet getHyperwallet() {
        return Hyperwallet.getDefault();
    }

    /**
     * @see TransferMethodRepository#createTransferMethod(HyperwalletTransferMethod, LoadTransferMethodCallback)
     */
    @Override
    public void createTransferMethod(@NonNull final HyperwalletTransferMethod transferMethod,
            LoadTransferMethodCallback callback) {
        switch (transferMethod.getField(TYPE)) {
            case BANK_ACCOUNT:
            case WIRE_ACCOUNT:
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

    /**
     * @see TransferMethodRepository#loadLatestTransferMethod(LoadTransferMethodCallback)
     */
    @Override
    public void loadTransferMethods(@NonNull final LoadTransferMethodListCallback callback) {

        HyperwalletTransferMethodQueryParam queryParam = new HyperwalletTransferMethodQueryParam.Builder()
                .sortByCreatedOnDesc()
                .status(ACTIVATED)
                .build();
        EspressoIdlingResource.increment();
        getHyperwallet().listTransferMethods(queryParam,
                new HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletPageList<HyperwalletTransferMethod> result) {
                        EspressoIdlingResource.decrement();
                        callback.onTransferMethodListLoaded(result != null ? result.getDataList() : null);
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        EspressoIdlingResource.decrement();
                        callback.onError(exception.getHyperwalletErrors());
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });
    }

    /**
     * @see TransferMethodRepository#loadLatestTransferMethod(LoadTransferMethodCallback)
     */
    @Override
    public void loadLatestTransferMethod(@NonNull final LoadTransferMethodCallback callback) {
        HyperwalletTransferMethodQueryParam queryParam = new HyperwalletTransferMethodQueryParam.Builder()
                .sortByCreatedOnDesc()
                .limit(QUERY_SINGLE_RESULT)
                .status(ACTIVATED)
                .build();
        EspressoIdlingResource.increment();
        getHyperwallet().listTransferMethods(queryParam,
                new HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletPageList<HyperwalletTransferMethod> result) {
                        EspressoIdlingResource.decrement();
                        callback.onTransferMethodLoaded(result != null ? result.getDataList().get(0) : null);
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        EspressoIdlingResource.decrement();
                        callback.onError(exception.getHyperwalletErrors());
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });
    }

    /**
     * @see TransferMethodRepository#deactivateTransferMethod(HyperwalletTransferMethod, DeactivateTransferMethodCallback)
     */
    @Override
    public void deactivateTransferMethod(@NonNull final HyperwalletTransferMethod transferMethod,
            @NonNull final DeactivateTransferMethodCallback callback) {
        switch (transferMethod.getField(TYPE)) {
            case BANK_ACCOUNT:
            case WIRE_ACCOUNT:
                deactivateBankAccount(transferMethod, callback);
                break;
            case BANK_CARD:
                deactivateBankCardAccount(transferMethod, callback);
                break;
            case PAYPAL_ACCOUNT:
                deactivatePayPalAccount(transferMethod, callback);
                break;
            default: //no default action
        }
    }

    private void deactivateBankAccount(@NonNull final HyperwalletTransferMethod transferMethod,
            @NonNull final DeactivateTransferMethodCallback callback) {
        getHyperwallet().deactivateBankAccount(transferMethod.getField(TOKEN), null,
                new HyperwalletListener<StatusTransition>() {
                    @Override
                    public void onSuccess(@Nullable StatusTransition result) {
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
                new HyperwalletListener<StatusTransition>() {
                    @Override
                    public void onSuccess(@Nullable StatusTransition result) {
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

    private void deactivatePayPalAccount(@NonNull final HyperwalletTransferMethod transferMethod,
            @NonNull final DeactivateTransferMethodCallback callback) {
        getHyperwallet().deactivatePayPalAccount(transferMethod.getField(TOKEN), null,
                new HyperwalletListener<StatusTransition>() {
                    @Override
                    public void onSuccess(@Nullable StatusTransition result) {
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
