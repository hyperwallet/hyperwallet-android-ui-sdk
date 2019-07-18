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

package com.hyperwallet.android.ui.transfermethod.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.HyperwalletError;

import java.util.List;
import java.util.TreeMap;

public interface SelectTransferMethodContract {

    interface View {

        void showAddTransferMethod(@NonNull final String country, @NonNull final String currency,
                @NonNull final String transferMethodType, @NonNull final String profileType);

        void showErrorLoadTransferMethodConfigurationKeys(@NonNull final List<HyperwalletError> errors);

        void showErrorLoadCurrency(@NonNull final List<HyperwalletError> errors);

        void showErrorLoadTransferMethodTypes(@NonNull final List<HyperwalletError> errors);

        void showErrorLoadCountrySelection(@NonNull final List<HyperwalletError> errors);

        void showErrorLoadCurrencySelection(@NonNull final List<HyperwalletError> errors);

        void showTransferMethodCurrency(@NonNull String currencyCode);

        void showTransferMethodCountry(@NonNull String countryCode);

        void showTransferMethodTypes(@NonNull List<TransferMethodSelectionItem> transferMethodTypes);

        void showProgressBar();

        void hideProgressBar();

        void showCountrySelectionDialog(@NonNull final TreeMap<String, String> countryNameCodeMap,
                @NonNull final String selectedCountryName);

        void showCurrencySelectionDialog(@NonNull final TreeMap<String, String> currencyNameCodeMap,
                @NonNull final String selectedCurrencyName);

        /**
         * Check the state of a View
         *
         * @return true when View is added to Container
         */
        boolean isActive();

        void reloadCurrency();

        void reloadTransferMethodConfigurationKeys();

        void reloadTransferMethodTypes();

        void reloadCountrySelection();

        void reloadCurrencySelection();
    }

    interface Presenter {

        void loadCurrency(final boolean forceUpdate, @NonNull final String countryCode);

        void loadTransferMethodConfigurationKeys(final boolean forceUpdate, @Nullable final String countryCode,
                @Nullable final String currencyCode);

        void loadTransferMethodTypes(final boolean forceUpdate, @NonNull final String countryCode,
                @NonNull final String currencyCode);

        void openAddTransferMethod(@NonNull final String country, @NonNull final String currency,
                @NonNull final String transferMethodType, @NonNull final String profileType);

        void loadCountrySelection(@NonNull final String countryCode);

        void loadCurrencySelection(@NonNull final String countryCode, @NonNull final String currencyCode);
    }
}
