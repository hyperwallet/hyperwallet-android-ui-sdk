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
import com.hyperwallet.android.model.meta.Fee;
import com.hyperwallet.android.model.meta.HyperwalletTransferMethodConfigurationKeyResult;
import com.hyperwallet.android.ui.repository.TransferMethodConfigurationRepository;
import com.hyperwallet.android.ui.repository.UserRepository;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class SelectTransferMethodPresenter implements SelectTransferMethodContract.Presenter {
    private static final String TAG = SelectTransferMethodPresenter.class.getName();

    private final TransferMethodConfigurationRepository mTransferMethodConfigurationRepository;
    private final UserRepository userRepository;
    private final SelectTransferMethodContract.View mView;
    private HyperwalletUser mUser;

    SelectTransferMethodPresenter(SelectTransferMethodContract.View view,
            TransferMethodConfigurationRepository transferMethodConfigurationRepository,
            UserRepository userRepository) {
        this.mView = view;
        this.mTransferMethodConfigurationRepository = transferMethodConfigurationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void loadTransferMethodConfigurationKeys(final boolean forceUpdate, @NonNull final String countryCode,
            @NonNull final String currencyCode) {

        mView.showProgressBar();

        if (forceUpdate) {
            mTransferMethodConfigurationRepository.refreshKeys();
        }
        if (mUser == null) {
            loadUser(new UserCallback() {
                @Override
                public void onUserLoaded() {
                    getKeys(countryCode, currencyCode);
                }

                @Override
                public void onUserError(@NonNull HyperwalletErrors errors) {
                    showErrorLoadTranferMethods(errors);
                }
            });
        } else {
            getKeys(countryCode, currencyCode);
        }
    }

    private void showErrorLoadTranferMethods(@NonNull HyperwalletErrors errors) {
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

        if (mUser == null) {
            loadUser(new UserCallback() {
                @Override
                public void onUserLoaded() {
                    getCurrencyKeys(countryCode);
                }

                @Override
                public void onUserError(@NonNull HyperwalletErrors errors) {
                    showErrorLoadCurrency(errors);
                }
            });
        } else {
            getCurrencyKeys(countryCode);
        }
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

        if (mUser == null) {
            loadUser(new UserCallback() {
                @Override
                public void onUserLoaded() {
                    getMethodTypes(countryCode, currencyCode);
                }

                @Override
                public void onUserError(@NonNull HyperwalletErrors errors) {
                    if (!mView.isActive()) {
                        return;
                    }
                    //show user error?
                    mView.showErrorLoadTransferMethodTypes(errors.getErrors());
                }
            });
        } else {
            getMethodTypes(countryCode, currencyCode);
        }
    }

    @Override
    public void openAddTransferMethod(@NonNull final String country, @NonNull final String currency,
            @NonNull final String transferMethodType) {
        mView.showAddTransferMethod(country, currency, transferMethodType);
    }

    @Override
    public void loadCountrySelection(@NonNull final String countryCode) {
        mTransferMethodConfigurationRepository.getKeys(new TransferMethodConfigurationRepository.LoadKeysCallback() {
            @Override
            public void onKeysLoaded(
                    @Nullable final HyperwalletTransferMethodConfigurationKeyResult transferMethodConfigurationKeyResult) {
                if (!mView.isActive()) {
                    return;
                }

                List<String> countryCodes = transferMethodConfigurationKeyResult.getCountries();
                TreeMap<String, String> countryNameCodeMap = new TreeMap<>();
                Locale.Builder builder = new Locale.Builder();
                for (String countryCode : countryCodes) {
                    Locale locale = builder.setRegion(countryCode).build();
                    countryNameCodeMap.put(locale.getDisplayName(), countryCode);
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
            public void onKeysLoaded(
                    @Nullable HyperwalletTransferMethodConfigurationKeyResult transferMethodConfigurationKeyResult) {
                if (!mView.isActive()) {
                    return;
                }

                List<String> currencyCodes = transferMethodConfigurationKeyResult.getCurrencies(countryCode);
                TreeMap<String, String> currencyNameCodeMap = new TreeMap<>();
                for (String currencyCode : currencyCodes) {
                    Currency currency = Currency.getInstance(currencyCode);
                    currencyNameCodeMap.put(currency.getDisplayName(), currencyCode);
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
            @NonNull final List<String> transferMethodTypes,
            @NonNull final HyperwalletTransferMethodConfigurationKeyResult result) {

        List<TransferMethodSelectionItem> selectionItems = new ArrayList<>();
        for (String transferMethodType : transferMethodTypes) {
            List<Fee> fees = result.getFees(country, currency, transferMethodType,
                    mUser.getField(HyperwalletUser.UserFields.PROFILE_TYPE));
            String processingTime = result.getProcessingTime(country, currency, transferMethodType,
                    mUser.getField(HyperwalletUser.UserFields.PROFILE_TYPE));
            TransferMethodSelectionItem data = new TransferMethodSelectionItem(country, currency,
                    mUser.getField(HyperwalletUser.UserFields.PROFILE_TYPE),
                    transferMethodType, processingTime, fees);
            selectionItems.add(data);
        }
        return selectionItems;
    }

    private void loadUser(@Nullable final UserCallback userCallback) {
        userRepository.loadUser(new UserRepository.LoadUserCallback() {
            @Override
            public void onUserLoaded(@Nullable HyperwalletUser user) {
                if (user == null) {
                    //throw exception?
                }
                mUser = user;
                if (userCallback != null) {
                    userCallback.onUserLoaded();
                }
            }

            @Override
            public void onError(@NonNull HyperwalletErrors errors) {
                if (userCallback != null) {
                    userCallback.onUserError(errors);
                }
            }
        });
    }

    private void getKeys(@NonNull final String countryCode, @NonNull final String currencyCode) {
        mTransferMethodConfigurationRepository.getKeys(new TransferMethodConfigurationRepository.LoadKeysCallback() {
            @Override
            public void onKeysLoaded(
                    @Nullable final HyperwalletTransferMethodConfigurationKeyResult transferMethodConfigurationKeyResult) {
                if (!mView.isActive()) {
                    return;
                }
                getAllKeys(transferMethodConfigurationKeyResult, countryCode, currencyCode);
            }

            @Override
            public void onError(@NonNull final HyperwalletErrors errors) {
                showErrorLoadTranferMethods(errors);
            }
        });
    }

    private void getAllKeys(
            @Nullable HyperwalletTransferMethodConfigurationKeyResult transferMethodConfigurationKeyResult,
            @NonNull String countryCode, @NonNull String currencyCode) {
        mView.hideProgressBar();
        List<String> transferMethodTypes =
                transferMethodConfigurationKeyResult.getTransferMethods(countryCode, currencyCode,
                        mUser.getField(HyperwalletUser.UserFields.PROFILE_TYPE));

        mView.showTransferMethodCountry(countryCode);
        mView.showTransferMethodCurrency(currencyCode);
        mView.showTransferMethodTypes(getTransferMethodSelectionItems(countryCode, currencyCode,
                transferMethodTypes, transferMethodConfigurationKeyResult));
    }

    interface UserCallback {
        void onUserLoaded();

        void onUserError(@NonNull HyperwalletErrors errors);
    }

    private void getCurrencyKeys(@NonNull final String countryCode) {
        mTransferMethodConfigurationRepository.getKeys(new TransferMethodConfigurationRepository.LoadKeysCallback() {
            @Override
            public void onKeysLoaded(@Nullable final HyperwalletTransferMethodConfigurationKeyResult result) {
                if (!mView.isActive()) {
                    return;
                }
                List<String> transferMethodCurrencies = result.getCurrencies(countryCode);
                List<String> transferMethodTypes = result.getTransferMethods(
                        countryCode, transferMethodCurrencies.get(0),
                        mUser.getField(HyperwalletUser.UserFields.PROFILE_TYPE));

                mView.showTransferMethodCountry(countryCode);
                mView.showTransferMethodCurrency(transferMethodCurrencies.get(0));
                mView.showTransferMethodTypes(getTransferMethodSelectionItems(countryCode,
                        transferMethodCurrencies.get(0), transferMethodTypes, result));
            }

            @Override
            public void onError(@NonNull final HyperwalletErrors errors) {
                showErrorLoadCurrency(errors);
            }
        });
    }

    private void getMethodTypes(@NonNull final String countryCode,
            @NonNull final String currencyCode) {
        mTransferMethodConfigurationRepository.getKeys(new TransferMethodConfigurationRepository.LoadKeysCallback() {
            @Override
            public void onKeysLoaded(@Nullable final HyperwalletTransferMethodConfigurationKeyResult
                    transferMethodConfigurationKeyResult) {
                if (!mView.isActive()) {
                    return;
                }

                List<String> transferMethodTypes = transferMethodConfigurationKeyResult.getTransferMethods(
                        countryCode, currencyCode, mUser.getField(HyperwalletUser.UserFields.PROFILE_TYPE));

                mView.showTransferMethodCountry(countryCode);
                mView.showTransferMethodCurrency(currencyCode);
                mView.showTransferMethodTypes(getTransferMethodSelectionItems(countryCode, currencyCode,
                        transferMethodTypes, transferMethodConfigurationKeyResult));
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



}
