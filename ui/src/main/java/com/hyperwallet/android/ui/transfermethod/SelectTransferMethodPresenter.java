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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.HyperwalletUser;
import com.hyperwallet.android.model.meta.HyperwalletTransferMethodConfigurationKey;
import com.hyperwallet.android.model.meta.keyed.Country;
import com.hyperwallet.android.model.meta.keyed.HyperwalletTransferMethodType;
import com.hyperwallet.android.ui.repository.TransferMethodConfigurationRepository;
import com.hyperwallet.android.ui.repository.UserRepository;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

public class SelectTransferMethodPresenter implements SelectTransferMethodContract.Presenter {

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
    public void loadTransferMethodConfigurationKeys(final boolean forceUpdate, @NonNull final String countryCode,
            @NonNull final String currencyCode) {

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
                                mView.hideProgressBar();
                                Set<HyperwalletTransferMethodType> transferMethodTypes =
                                        key.getTransferMethodType(countryCode, currencyCode);
                                mView.showTransferMethodCountry(countryCode);
                                mView.showTransferMethodCurrency(currencyCode);
                                mView.showTransferMethodTypes(
                                        getTransferMethodSelectionItems(countryCode, currencyCode,
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
                                List<com.hyperwallet.android.model.meta.keyed.Currency> transferMethodCurrencies =
                                        new ArrayList<>(key.getCurrencies(countryCode));
                                Set<HyperwalletTransferMethodType> transferMethodTypes = key
                                        .getTransferMethodType(countryCode, transferMethodCurrencies.get(0).getCode());

                                mView.showTransferMethodCountry(countryCode);
                                mView.showTransferMethodCurrency(transferMethodCurrencies.get(0).getCode());
                                mView.showTransferMethodTypes(getTransferMethodSelectionItems(countryCode,
                                        transferMethodCurrencies.get(0).getCode(),
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

                                Set<HyperwalletTransferMethodType> transferMethodTypes = key
                                        .getTransferMethodType(countryCode, currencyCode);

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

                Set<Country> countryCodes = key.getCountries();
                TreeMap<String, String> countryNameCodeMap = new TreeMap<>();
                Locale.Builder builder = new Locale.Builder();
                for (Country countryCode : countryCodes) {
                    Locale locale = builder.setRegion(countryCode.getCode()).build();
                    countryNameCodeMap.put(locale.getDisplayName(), countryCode.getCode());
                }
                Locale locale = new Locale.Builder().setRegion(countryCode).build();
                mView.showCountrySelectionDialog(countryNameCodeMap, locale.getDisplayName());
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

                Set<com.hyperwallet.android.model.meta.keyed.Currency> currencyCodes = key.getCurrencies(countryCode);
                TreeMap<String, String> currencyNameCodeMap = new TreeMap<>();
                for (com.hyperwallet.android.model.meta.keyed.Currency currencyCode : currencyCodes) {
                    Currency currency = Currency.getInstance(currencyCode.getCode());
                    currencyNameCodeMap.put(currency.getDisplayName(), currencyCode.getCode());
                }
                mView.showCurrencySelectionDialog(currencyNameCodeMap,
                        Currency.getInstance(currencyCode).getDisplayName());
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
            @NonNull final String country, @NonNull final String currency,
            @NonNull final String userProfileType,
            @NonNull final Set<HyperwalletTransferMethodType> transferMethodTypes) {

        List<TransferMethodSelectionItem> selectionItems = new ArrayList<>();
        for (HyperwalletTransferMethodType transferMethodType : transferMethodTypes) {
            TransferMethodSelectionItem data = new TransferMethodSelectionItem(country, currency, userProfileType,
                    transferMethodType.getCode(), transferMethodType.getName(), transferMethodType.getProcessingTime(),
                    transferMethodType.getFees());
            selectionItems.add(data);
        }
        return selectionItems;
    }
}
