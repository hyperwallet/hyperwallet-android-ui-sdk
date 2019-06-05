
/*
 * Copyright 2018 Hyperwallet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.hyperwallet.android.ui;

import static com.hyperwallet.android.ui.transfermethod.AddTransferMethodActivity.EXTRA_TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.ui.transfermethod.AddTransferMethodActivity.EXTRA_TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.ui.transfermethod.AddTransferMethodActivity.EXTRA_TRANSFER_METHOD_PROFILE_TYPE;
import static com.hyperwallet.android.ui.transfermethod.AddTransferMethodActivity.EXTRA_TRANSFER_METHOD_TYPE;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.HyperwalletAuthenticationTokenProvider;
import com.hyperwallet.android.transaction_history.view.ListTransactionHistoryActivity;
import com.hyperwallet.android.ui.transfermethod.AddTransferMethodActivity;
import com.hyperwallet.android.ui.transfermethod.ListTransferMethodActivity;
import com.hyperwallet.android.ui.transfermethod.SelectTransferMethodActivity;

/**
 * Class responsible for initializing the Hyperwallet UI SDK. It contains methods to interact with the activities and
 * fragments used to interact with the Hyperwallet platform
 */
public final class HyperwalletUi {

    private static HyperwalletUi sInstance;

    private HyperwalletUi() {
    }

    /**
     * @param authenticationTokenProvider An implementation of the {@link HyperwalletAuthenticationTokenProvider}
     * @return Returns a newly created HyperwalletUi that can be used to get Intents to launch different
     * activities.
     */
    public static synchronized HyperwalletUi getInstance(
            @NonNull final HyperwalletAuthenticationTokenProvider authenticationTokenProvider) {
        if (sInstance == null) {
            sInstance = new HyperwalletUi();
            Hyperwallet.getInstance(authenticationTokenProvider);
        }
        return sInstance;
    }


    /**
     * @param context A Context of the application consuming this Intent.
     * @return an Intent with the data necessary to launch the {@link ListTransferMethodActivity}
     */
    public Intent getIntentListTransferMethodActivity(@NonNull final Context context) {
        return new Intent(context, ListTransferMethodActivity.class);
    }


    /**
     * @param context A Context of the application consuming this Intent.
     * @return an Intent with the data necessary to launch the {@link SelectTransferMethodActivity}
     */
    public Intent getIntentSelectTransferMethodActivity(@NonNull final Context context) {
        return new Intent(context, SelectTransferMethodActivity.class);
    }


    /**
     * @param context            A Context of the application consuming this Intent.
     * @param country            The transfer method country code. ISO 3166-1 alpha-2 format.
     * @param currency           The transfer method currency code. ISO 4217 format.
     * @param transferMethodType The type of transfer method. For a complete list of transfer methods, see {@link
     *                           com.hyperwallet.android.model.HyperwalletTransferMethod.TransferMethodTypes}
     * @param profileType        The type of the account holder profile. For a complete list of options, see
     *                           {@link com.hyperwallet.android.model.HyperwalletUser.ProfileTypes}
     * @return an Intent with the data necessary to launch the {@link AddTransferMethodActivity}
     */
    public Intent getIntentAddTransferMethodActivity(@NonNull final Context context, @NonNull final String country,
            @NonNull final String currency, @NonNull final String transferMethodType,
            @NonNull final String profileType) {
        Intent intent = new Intent(context, AddTransferMethodActivity.class);
        intent.putExtra(EXTRA_TRANSFER_METHOD_COUNTRY, country);
        intent.putExtra(EXTRA_TRANSFER_METHOD_CURRENCY, currency);
        intent.putExtra(EXTRA_TRANSFER_METHOD_TYPE, transferMethodType);
        intent.putExtra(EXTRA_TRANSFER_METHOD_PROFILE_TYPE, profileType);
        return intent;
    }


    public Intent getIntentUserReceiptsActivity(@NonNull final Context context) {
        Intent intent = new Intent(context, ListTransactionHistoryActivity.class);
        intent.putExtra(ListTransactionHistoryActivity.EXTRA_RECEIPT_SOURCE_TOKEN, "usr-");
        return intent;
    }

    public Intent getIntentPrepaidCardReceiptsActivity(@NonNull final Context context,
            @NonNull final String prepaidCardToken) {
        Intent intent = new Intent(context, ListTransactionHistoryActivity.class);
        intent.putExtra(ListTransactionHistoryActivity.EXTRA_RECEIPT_SOURCE_TOKEN, prepaidCardToken);
        return intent;
    }

}
