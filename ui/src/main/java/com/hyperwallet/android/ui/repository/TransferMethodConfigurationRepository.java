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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.graphql.HyperwalletTransferMethodConfigurationField;
import com.hyperwallet.android.model.graphql.HyperwalletTransferMethodConfigurationKey;

public interface TransferMethodConfigurationRepository {

    void getKeys(@NonNull final LoadKeysCallback loadKeysCallback);

    void getFields(@NonNull final String country, @NonNull final String currency,
            @NonNull final String transferMethodType, @NonNull final String transferMethodProfileType,
            @NonNull final LoadFieldsCallback loadFieldsCallback);

    void refreshKeys();

    void refreshFields();

    interface LoadKeysCallback {

        void onKeysLoaded(@Nullable final HyperwalletTransferMethodConfigurationKey transferMethodConfigurationKey);

        void onError(@NonNull final HyperwalletErrors errors);
    }

    interface LoadFieldsCallback {

        void onFieldsLoaded(@Nullable final HyperwalletTransferMethodConfigurationField field,
                @Nullable final String processingTime);

        void onError(@NonNull final HyperwalletErrors errors);
    }
}
