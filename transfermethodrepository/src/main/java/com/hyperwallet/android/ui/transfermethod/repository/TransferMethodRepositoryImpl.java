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

import static com.hyperwallet.android.ExceptionMapper.EC_UNEXPECTED_EXCEPTION;
import static com.hyperwallet.android.model.StatusTransition.StatusDefinition.ACTIVATED;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.BANK_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.BANK_CARD;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.PAYPAL_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.VENMO_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.WIRE_ACCOUNT;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.paging.PageList;
import com.hyperwallet.android.model.transfermethod.BankAccount;
import com.hyperwallet.android.model.transfermethod.BankCard;
import com.hyperwallet.android.model.transfermethod.PayPalAccount;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.model.transfermethod.TransferMethodQueryParam;
import com.hyperwallet.android.model.transfermethod.VenmoAccount;
import com.hyperwallet.android.ui.common.repository.EspressoIdlingResource;

import java.util.Collections;

public class TransferMethodRepositoryImpl implements TransferMethodRepository {

    private static final short QUERY_SINGLE_RESULT = 1;
    private static final int DEFAULT_LIMIT = 100;
    private Handler mHandler = new Handler();

    @VisibleForTesting
    Hyperwallet getHyperwallet() {
        return Hyperwallet.getDefault();
    }

    /**
     * @see TransferMethodRepository#createTransferMethod(TransferMethod, LoadTransferMethodCallback)
     */
    @Override
    public void createTransferMethod(@NonNull final TransferMethod transferMethod,
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
            case VENMO_ACCOUNT:
                createVenmoAccount(transferMethod, callback);
                break;
            default: // error on unknown transfer type
                callback.onError(getErrorsOnUnsupportedTransferType());
        }
    }

    /**
     * @see TransferMethodRepository#loadLatestTransferMethod(LoadTransferMethodCallback)
     */
    @Override
    public void loadTransferMethods(@NonNull final LoadTransferMethodListCallback callback) {

        TransferMethodQueryParam queryParam = new TransferMethodQueryParam.Builder()
                .limit(DEFAULT_LIMIT)
                .sortByCreatedOnDesc()
                .status(ACTIVATED)
                .build();
        EspressoIdlingResource.increment();
        getHyperwallet().listTransferMethods(queryParam,
                new HyperwalletListener<PageList<TransferMethod>>() {
                    @Override
                    public void onSuccess(@Nullable PageList<TransferMethod> result) {
                        EspressoIdlingResource.decrement();
                        callback.onTransferMethodListLoaded(result != null ? result.getDataList() : null);
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        EspressoIdlingResource.decrement();
                        callback.onError(exception.getErrors());
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
        TransferMethodQueryParam queryParam = new TransferMethodQueryParam.Builder()
                .sortByCreatedOnDesc()
                .limit(QUERY_SINGLE_RESULT)
                .status(ACTIVATED)
                .build();
        EspressoIdlingResource.increment();
        getHyperwallet().listTransferMethods(queryParam,
                new HyperwalletListener<PageList<TransferMethod>>() {
                    @Override
                    public void onSuccess(@Nullable PageList<TransferMethod> result) {
                        EspressoIdlingResource.decrement();
                        callback.onTransferMethodLoaded(result != null ? result.getDataList().get(0) : null);
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        EspressoIdlingResource.decrement();
                        callback.onError(exception.getErrors());
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });
    }

    /**
     * @see TransferMethodRepository#deactivateTransferMethod(TransferMethod, DeactivateTransferMethodCallback)
     */
    @Override
    public void deactivateTransferMethod(@NonNull final TransferMethod transferMethod,
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
            case VENMO_ACCOUNT:
                deactivateVenmoAccount(transferMethod, callback);
                break;
            default: // error on unknown transfer type
                callback.onError(getErrorsOnUnsupportedTransferType());
        }
    }

    private void deactivateBankAccount(@NonNull final TransferMethod transferMethod,
            @NonNull final DeactivateTransferMethodCallback callback) {
        getHyperwallet().deactivateBankAccount(transferMethod.getField(TOKEN), null,
                new HyperwalletListener<StatusTransition>() {
                    @Override
                    public void onSuccess(@Nullable StatusTransition result) {
                        callback.onTransferMethodDeactivated(result);
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        callback.onError(exception.getErrors());
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });
    }

    private void deactivateBankCardAccount(@NonNull final TransferMethod transferMethod,
            @NonNull final DeactivateTransferMethodCallback callback) {
        getHyperwallet().deactivateBankCard(transferMethod.getField(TOKEN), null,
                new HyperwalletListener<StatusTransition>() {
                    @Override
                    public void onSuccess(@Nullable StatusTransition result) {
                        callback.onTransferMethodDeactivated(result);
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        callback.onError(exception.getErrors());
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });
    }

    private void deactivatePayPalAccount(@NonNull final TransferMethod transferMethod,
            @NonNull final DeactivateTransferMethodCallback callback) {
        getHyperwallet().deactivatePayPalAccount(transferMethod.getField(TOKEN), null,
                new HyperwalletListener<StatusTransition>() {
                    @Override
                    public void onSuccess(@Nullable StatusTransition result) {
                        callback.onTransferMethodDeactivated(result);
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        callback.onError(exception.getErrors());
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });
    }

    private void deactivateVenmoAccount(@NonNull final TransferMethod transferMethod,
                                        @NonNull final DeactivateTransferMethodCallback callback) {
        getHyperwallet().deactivateVenmoAccount(transferMethod.getField(TOKEN), null,
                new HyperwalletListener<StatusTransition>() {
                    @Override
                    public void onSuccess(@Nullable StatusTransition result) {
                        callback.onTransferMethodDeactivated(result);
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        callback.onError(exception.getErrors());
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });
    }

    private void createBankAccount(final TransferMethod transferMethod,
                                   final LoadTransferMethodCallback callback) {
        BankAccount bankAccount = (BankAccount) transferMethod;

        getHyperwallet().createBankAccount(bankAccount, new HyperwalletListener<BankAccount>() {
            @Override
            public void onSuccess(@Nullable BankAccount result) {
                callback.onTransferMethodLoaded(result);
            }

            @Override
            public void onFailure(HyperwalletException exception) {
                callback.onError(exception.getErrors());
            }

            @Override
            public Handler getHandler() {
                return mHandler;
            }
        });
    }

    private void createBankCard(@NonNull final TransferMethod transferMethod,
            @NonNull final LoadTransferMethodCallback callback) {
        BankCard bankCard = (BankCard) transferMethod;

        getHyperwallet().createBankCard(bankCard, new HyperwalletListener<BankCard>() {
            @Override
            public void onSuccess(@Nullable BankCard result) {
                callback.onTransferMethodLoaded(result);
            }

            @Override
            public void onFailure(HyperwalletException exception) {
                callback.onError(exception.getErrors());
            }

            @Override
            public Handler getHandler() {
                return mHandler;
            }
        });
    }

    private void createPayPalAccount(@NonNull final TransferMethod transferMethod,
                                     @NonNull final LoadTransferMethodCallback callback) {
        PayPalAccount payPalAccount = (PayPalAccount) transferMethod;

        getHyperwallet().createPayPalAccount(payPalAccount, new HyperwalletListener<PayPalAccount>() {
            @Override
            public void onSuccess(@Nullable PayPalAccount result) {
                callback.onTransferMethodLoaded(result);
            }

            @Override
            public void onFailure(HyperwalletException exception) {
                callback.onError(exception.getErrors());
            }

            @Override
            public Handler getHandler() {
                return mHandler;
            }
        });
    }

    private void createVenmoAccount(@NonNull final TransferMethod transferMethod,
                                    @NonNull final LoadTransferMethodCallback callback) {
        VenmoAccount venmoAccount = (VenmoAccount) transferMethod;

        getHyperwallet().createVenmoAccount(venmoAccount, new HyperwalletListener<VenmoAccount>() {
            @Override
            public void onSuccess(@Nullable VenmoAccount result) {
                callback.onTransferMethodLoaded(result);
            }

            @Override
            public void onFailure(HyperwalletException exception) {
                callback.onError(exception.getErrors());
            }

            @Override
            public Handler getHandler() {
                return mHandler;
            }
        });
    }

    // Note: This way of surfacing error is not ideal but rather a workaround please have a look on other options,
    // before resulting into this pattern
    private Errors getErrorsOnUnsupportedTransferType() {
        Error error = new Error(R.string.error_unsupported_transfer_type,
                EC_UNEXPECTED_EXCEPTION);
        return new Errors(Collections.singletonList(error));
    }
}
