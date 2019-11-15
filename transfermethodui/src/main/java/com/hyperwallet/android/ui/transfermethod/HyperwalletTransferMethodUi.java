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
package com.hyperwallet.android.ui.transfermethod;

import static com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodActivity.EXTRA_TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodActivity.EXTRA_TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodActivity.EXTRA_TRANSFER_METHOD_PROFILE_TYPE;
import static com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodActivity.EXTRA_TRANSFER_METHOD_TYPE;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.HyperwalletAuthenticationTokenProvider;
import com.hyperwallet.android.ui.common.insight.HyperwalletInsight;
import com.hyperwallet.android.ui.common.intent.HyperwalletIntent;
import com.hyperwallet.android.ui.transfermethod.view.AddTransferMethodActivity;
import com.hyperwallet.android.ui.transfermethod.view.ListTransferMethodActivity;

/**
 * Class responsible for initializing the Hyperwallet UI SDK. It contains methods to interact with the activities and
 * fragments used to interact with the Hyperwallet platform
 */
public final class HyperwalletTransferMethodUi {

    private static HyperwalletTransferMethodUi sInstance;

    private HyperwalletTransferMethodUi() {
    }

    /**
     * @deprecated use {@link #getInstance(Context, HyperwalletAuthenticationTokenProvider)}
     *
     * @param authenticationTokenProvider An implementation of the {@link HyperwalletAuthenticationTokenProvider}
     * @return Returns a newly created HyperwalletTransferMethodUi that can be used to get Intents to launch different
     * activities.
     */
    @Deprecated
    public static synchronized HyperwalletTransferMethodUi getInstance(
            @NonNull final HyperwalletAuthenticationTokenProvider authenticationTokenProvider) {
        if (sInstance == null) {
            sInstance = new HyperwalletTransferMethodUi();
            Hyperwallet.getInstance(authenticationTokenProvider);
        }
        return sInstance;
    }

    /**
     * Returns an instance that will also initialize the Insights library.
     *
     * @param context                     A Context of the application consuming this Intent.
     * @param authenticationTokenProvider An implementation of the {@link HyperwalletAuthenticationTokenProvider}
     * @return singleton instance of HyperwalletTransferMethodUi
     */
    public static synchronized HyperwalletTransferMethodUi getInstance(@NonNull final Context context,
            @NonNull final HyperwalletAuthenticationTokenProvider authenticationTokenProvider) {
        if (sInstance == null) {
            sInstance = new HyperwalletTransferMethodUi();
        }

        // initialize insight
        HyperwalletInsight.getInstance().initialize(context, authenticationTokenProvider);
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
     * @return an Intent with Action specified to start implicit activity
     * {@link HyperwalletIntent#ACTION_SELECT_TRANSFER_METHOD}
     */
    public Intent getIntentSelectTransferMethodActivity(@NonNull final Context context) {
        Intent intent = new Intent();
        intent.setAction(HyperwalletIntent.ACTION_SELECT_TRANSFER_METHOD);
        return intent;
    }

    /**
     * @param context            A Context of the application consuming this Intent.
     * @param country            The transfer method country code. ISO 3166-1 alpha-2 format.
     * @param currency           The transfer method currency code. ISO 4217 format.
     * @param transferMethodType The type of transfer method. For a complete list of transfer methods, see {@link
     *                       com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodTypes}
     * @param profileType        The type of the account holder profile. For a complete list of options, see
     *                           {@link com.hyperwallet.android.model.user.HyperwalletUser.ProfileTypes}
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
}
