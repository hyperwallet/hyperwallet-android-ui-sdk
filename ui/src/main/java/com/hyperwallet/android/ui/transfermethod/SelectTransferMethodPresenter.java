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

import static com.hyperwallet.android.ExceptionMapper.EC_UNEXPECTED_EXCEPTION;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.graphql.HyperwalletTransferMethodConfigurationKey;
import com.hyperwallet.android.model.graphql.keyed.Country;
import com.hyperwallet.android.model.graphql.keyed.Currency;
import com.hyperwallet.android.model.graphql.keyed.HyperwalletTransferMethodType;
import com.hyperwallet.android.model.user.HyperwalletUser;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodConfigurationRepository;
import com.hyperwallet.android.ui.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class SelectTransferMethodPresenter implements SelectTransferMethodContract.Presenter {

    private static final String DEFAULT_COUNTRY_CODE = "US";

    private final TransferMethodConfigurationRepository mTransferMethodConfigurationRepository;
    private final UserRepository mUserRepository;
    private final SelectTransferMethodContract.View mView;

    SelectTransferMethodPresenter(SelectTransferMethodContract.View view,
            @NonNull final TransferMethodConfigurationRepository transferMethodConfigurationRepository,
            @NonNull final UserRepository userRepository) {
        this.mView = view;
        this.mTransferMethodConfigurationRepository = transferMethodConfigurationRepository;
        this.mUserRepository = userRepository;
    }

    @Override
    public void loadTransferMethodConfigurationKeys(final boolean forceUpdate, @Nullable final String countryCode,
            @Nullable final String currencyCode) {

        mView.showProgressBar();

        if (forceUpdate) {
            mTransferMethodConfigurationRepository.refreshKeys();
        }

        mUserRepository.loadUser(new UserRepository.LoadUserCallback() {
            @Override
            public void onUserLoaded(@NonNull final HyperwalletUser user) {
                mTransferMethodConfigurationRepository.getKeys(
                        new TransferMethodConfigurationRepository.LoadKeysCallback() {
                            @Override
                            public void onKeysLoaded(@Nullable final HyperwalletTransferMethodConfigurationKey key) {
                                if (!mView.isActive()) {
                                    return;
                                }

                                Country country = TextUtils.isEmpty(countryCode) ? key.getCountry(user.getCountry())
                                        : key.getCountry(countryCode);

                                if (country == null) { // param and user country is null
                                    country = key.getCountry(DEFAULT_COUNTRY_CODE);
                                }

                                String currencyCodeString = currencyCode;
                                Set<Currency> currencies = key.getCurrencies(country.getCode());
                                if (TextUtils.isEmpty(currencyCodeString) && currencies != null
                                        && !currencies.isEmpty()) {
                                    currencyCodeString = ((Currency) currencies.toArray()[0]).getCode();
                                }

                                // at this point if currencyCodeString is still null/empty, that means program is
                                // mis-configured
                                if (TextUtils.isEmpty(currencyCodeString)) {
                                    HyperwalletError error = new HyperwalletError(
                                            "Can't get Currency based from Country: " + country.getCode(),
                                            EC_UNEXPECTED_EXCEPTION);
                                    showErrorLoadTransferMethods(new HyperwalletErrors(Arrays.asList(error)));
                                    return;
                                }

                                mView.hideProgressBar();
                                Set<HyperwalletTransferMethodType> transferMethodTypes =
                                        key.getTransferMethodType(country.getCode(), currencyCodeString) != null
                                                ? key.getTransferMethodType(country.getCode(), currencyCodeString) :
                                                new HashSet<HyperwalletTransferMethodType>();

                                mView.showTransferMethodCountry(country.getCode());
                                mView.showTransferMethodCurrency(currencyCodeString);
                                mView.showTransferMethodTypes(
                                        getTransferMethodSelectionItems(country.getCode(), currencyCodeString,
                                                user.getProfileType(), transferMethodTypes));
                            }

                            @Override
                            public void onError(@NonNull final HyperwalletErrors errors) {
                                showErrorLoadTransferMethods(errors);
                            }
                        });
            }

            @Override
            public void onError(@NonNull HyperwalletErrors errors) {
                showErrorLoadTransferMethods(errors);
            }
        });
    }

    private void showErrorLoadTransferMethods(@NonNull HyperwalletErrors errors) {
        if (!mView.isActive()) {
            return;
        }
        mView.hideProgressBar();
        mView.showErrorLoadTransferMethodConfigurationKeys(errors.getErrors());
    }

    @Override
    public void loadCurrency(final boolean forceUpdate, @NonNull final String countryCode) {

        if (forceUpdate) {
            mTransferMethodConfigurationRepository.refreshKeys();
        }

        mUserRepository.loadUser(new UserRepository.LoadUserCallback() {
            public void onUserLoaded(@NonNull final HyperwalletUser user) {
                mTransferMethodConfigurationRepository.getKeys(
                        new TransferMethodConfigurationRepository.LoadKeysCallback() {
                            @Override
                            public void onKeysLoaded(@Nullable final HyperwalletTransferMethodConfigurationKey key) {
                                if (!mView.isActive()) {
                                    return;
                                }
                                List<Currency> currencies = key.getCurrencies(countryCode) != null ?
                                        new ArrayList<>(key.getCurrencies(countryCode)) :
                                        new ArrayList<Currency>();
                                String currencyCode = currencies.get(0).getCode();
                                Set<HyperwalletTransferMethodType> transferMethodTypes =
                                        key.getTransferMethodType(countryCode, currencyCode) != null ?
                                                key.getTransferMethodType(countryCode, currencyCode) :
                                                new HashSet<HyperwalletTransferMethodType>();

                                mView.showTransferMethodCountry(countryCode);
                                mView.showTransferMethodCurrency(currencies.get(0).getCode());
                                mView.showTransferMethodTypes(getTransferMethodSelectionItems(countryCode,
                                        currencies.get(0).getCode(),
                                        user.getProfileType(), transferMethodTypes));
                            }

                            @Override
                            public void onError(@NonNull final HyperwalletErrors errors) {
                                showErrorLoadCurrency(errors);
                            }
                        });
            }

            @Override
            public void onError(@NonNull HyperwalletErrors errors) {
                showErrorLoadCurrency(errors);
            }
        });
    }

    private void showErrorLoadCurrency(@NonNull HyperwalletErrors errors) {
        if (!mView.isActive()) {
            return;
        }
        mView.showErrorLoadCurrency(errors.getErrors());
    }

    @Override
    public void loadTransferMethodTypes(final boolean forceUpdate,
            @NonNull final String countryCode, @NonNull final String currencyCode) {
        if (forceUpdate) {
            mTransferMethodConfigurationRepository.refreshKeys();
        }

        mUserRepository.loadUser(new UserRepository.LoadUserCallback() {
            @Override
            public void onUserLoaded(@NonNull final HyperwalletUser user) {
                mTransferMethodConfigurationRepository.getKeys(
                        new TransferMethodConfigurationRepository.LoadKeysCallback() {
                            @Override
                            public void onKeysLoaded(@Nullable final HyperwalletTransferMethodConfigurationKey key) {
                                if (!mView.isActive()) {
                                    return;
                                }

                                Set<HyperwalletTransferMethodType> transferMethodTypes =
                                        key.getTransferMethodType(countryCode, currencyCode) != null ?
                                                key.getTransferMethodType(countryCode, currencyCode) :
                                                new HashSet<HyperwalletTransferMethodType>();
                                mView.showTransferMethodCountry(countryCode);
                                mView.showTransferMethodCurrency(currencyCode);
                                mView.showTransferMethodTypes(
                                        getTransferMethodSelectionItems(countryCode, currencyCode,
                                                user.getProfileType(), transferMethodTypes));
                            }

                            @Override
                            public void onError(@NonNull final HyperwalletErrors errors) {
                                if (!mView.isActive()) {
                                    return;
                                }
                                mView.showErrorLoadTransferMethodTypes(errors.getErrors());
                            }
                        });
            }

            @Override
            public void onError(@NonNull HyperwalletErrors errors) {
                if (!mView.isActive()) {
                    return;
                }
                mView.showErrorLoadTransferMethodTypes(errors.getErrors());
            }
        });
    }

    @Override
    public void openAddTransferMethod(@NonNull final String country, @NonNull final String currency,
            @NonNull final String transferMethodType, @NonNull final String profileType) {
        mView.showAddTransferMethod(country, currency, transferMethodType, profileType);
    }

    @Override
    public void loadCountrySelection(@NonNull final String countryCode) {
        mTransferMethodConfigurationRepository.getKeys(new TransferMethodConfigurationRepository.LoadKeysCallback() {
            @Override
            public void onKeysLoaded(@Nullable final HyperwalletTransferMethodConfigurationKey key) {
                if (!mView.isActive()) {
                    return;
                }

                Set<Country> countries = key.getCountries();
                TreeMap<String, String> countryNameCodeMap = new TreeMap<>();
                String selectedCountryName = "";
                for (Country country : countries) {
                    if (country.getCode().equals(countryCode)) {
                        selectedCountryName = country.getName();
                    }
                    countryNameCodeMap.put(country.getName(), country.getCode());
                }
                mView.showCountrySelectionDialog(countryNameCodeMap, selectedCountryName);
            }

            @Override
            public void onError(@NonNull final HyperwalletErrors errors) {
                if (!mView.isActive()) {
                    return;
                }
                mView.showErrorLoadCountrySelection(errors.getErrors());
            }
        });
    }

    public void loadCurrencySelection(@NonNull final String countryCode, @NonNull final String currencyCode) {
        mTransferMethodConfigurationRepository.getKeys(new TransferMethodConfigurationRepository.LoadKeysCallback() {
            @Override
            public void onKeysLoaded(@Nullable HyperwalletTransferMethodConfigurationKey key) {
                if (!mView.isActive()) {
                    return;
                }

                Set<Currency> currencyCodes = key.getCurrencies(countryCode) != null ?
                        key.getCurrencies(countryCode) : new HashSet<Currency>();

                TreeMap<String, String> currencyNameCodeMap = new TreeMap<>();
                String selectedCurrencyName = "";
                for (Currency currency : currencyCodes) {
                    if (currency.getCode().equals(currencyCode)) {
                        selectedCurrencyName = currency.getName();
                    }
                    currencyNameCodeMap.put(currency.getName(), currency.getCode());
                }
                mView.showCurrencySelectionDialog(currencyNameCodeMap, selectedCurrencyName);
            }

            @Override
            public void onError(@NonNull final HyperwalletErrors errors) {
                if (!mView.isActive()) {
                    return;
                }
                mView.showErrorLoadCurrencySelection(errors.getErrors());
            }
        });
    }

    private List<TransferMethodSelectionItem> getTransferMethodSelectionItems(
            @NonNull final String countryCode, @NonNull final String currencyCode,
            @NonNull final String userProfileType,
            @NonNull final Set<HyperwalletTransferMethodType> transferMethodTypes) {

        List<TransferMethodSelectionItem> selectionItems = new ArrayList<>();
        for (HyperwalletTransferMethodType transferMethodType : transferMethodTypes) {
            TransferMethodSelectionItem data = new TransferMethodSelectionItem(countryCode, currencyCode,
                    userProfileType, transferMethodType.getCode(), transferMethodType.getName(),
                    transferMethodType.getProcessingTime(), transferMethodType.getFees());
            selectionItems.add(data);
        }
        return selectionItems;
    }
}
