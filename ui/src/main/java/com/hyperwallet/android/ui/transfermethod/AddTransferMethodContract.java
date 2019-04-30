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

import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletTransferMethod;
import com.hyperwallet.android.model.meta.Fee;
import com.hyperwallet.android.model.meta.HyperwalletField;

import java.util.List;

/**
 * View and Presenter Contract for Adding Transfer Method
 */
public interface AddTransferMethodContract {

    interface View {

        void notifyTransferMethodAdded(@NonNull final HyperwalletTransferMethod transferMethod);

        void showErrorAddTransferMethod(@NonNull final List<HyperwalletError> errors);

        void showErrorLoadTransferMethodConfigurationFields(@NonNull final List<HyperwalletError> errors);

        void showTransferMethodFields(@NonNull final List<HyperwalletField> fields);

        void showTransactionInformation(List<Fee> fees, String processingTime);

        void showCreateButtonProgressBar();

        void hideCreateButtonProgressBar();

        void showProgressBar();

        void hideProgressBar();

        void showInputErrors(List<HyperwalletError> errors);

        /**
         * Check the state of a View
         *
         * @return true when View is added to Container
         */
        boolean isActive();

        void retryAddTransferMethod();

        void reloadTransferMethodConfigurationFields();
    }

    interface Presenter {

        void createTransferMethod(@NonNull HyperwalletTransferMethod transferMethod);

        void loadTransferMethodConfigurationFields(boolean forceUpdate,
                @NonNull String country,
                @NonNull String currency,
                @NonNull String transferMethodType);
    }
}
