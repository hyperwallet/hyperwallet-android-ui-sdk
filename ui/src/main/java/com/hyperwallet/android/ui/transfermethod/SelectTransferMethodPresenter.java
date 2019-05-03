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

import static com.hyperwallet.android.model.HyperwalletUser.UserFields.PROFILE_TYPE;

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
    private final UserRepository mUserRepository;
    private final SelectTransferMethodContract.View mView;
    private HyperwalletUser mUser;

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

        final TransferMethodConfigurationRepository.LoadKeysCallback loadKeysCallback =
                buildMethodKeysCallback(countryCode, currencyCode, true);

        if (mUser == null) {
            final UserRepository.LoadUserCallback loadUserCallback = buildLoadUserCallback(loadKeysCallback);
            mUserRepository.loadUser(loadUserCallback);
        } else {
            mTransferMethodConfigurationRepository.getKeys(loadKeysCallback);
        }
    }

    @Override
    public void loadCurrency(final boolean forceUpdate, @NonNull final String countryCode) {

        if (forceUpdate) {
            mTransferMethodConfigurationRepository.refreshKeys();
        }

        final TransferMethodConfigurationRepository.LoadKeysCallback loadCurrencyCallback = buildLoadCurrencyCallback(
                countryCode);

        if (mUser == null) {
            final UserRepository.LoadUserCallback loadUserCallback = buildLoadUserCallback(loadCurrencyCallback);
            mUserRepository.loadUser(loadUserCallback);
        } else {
            mTransferMethodConfigurationRepository.getKeys(loadCurrencyCallback);
        }
    }

    @Override
    public void loadTransferMethodTypes(final boolean forceUpdate,
            @NonNull final String countryCode, @NonNull final String currencyCode) {
        if (forceUpdate) {
            mTransferMethodConfigurationRepository.refreshKeys();
        }

        final TransferMethodConfigurationRepository.LoadKeysCallback loadMethodTypesKeysCallback =
                buildMethodKeysCallback(countryCode, currencyCode, false);

        if (mUser == null) {
            final UserRepository.LoadUserCallback loadUserCallback = buildLoadUserCallback(loadMethodTypesKeysCallback);
            mUserRepository.loadUser(loadUserCallback);
        } else {
            mTransferMethodConfigurationRepository.getKeys(loadMethodTypesKeysCallback);
        }
    }

    @Override
    public void openAddTransferMethod(@NonNull final String country, @NonNull final String currency,
            @NonNull final String transferMethodType) {
        mView.showAddTransferMethod(country, currency, transferMethodType);
    }

    @Override
    public void loadCountrySelection(@NonNull final String countryCode) {
        mTransferMethodConfigurationRepository.getKeys(buildLoadCountrySelectionCallback(countryCode));
    }

    public void loadCurrencySelection(@NonNull final String countryCode, @NonNull final String currencyCode) {
        mTransferMethodConfigurationRepository.getKeys(buildCurrencySelectionCallback(countryCode, currencyCode));
    }

    private UserRepository.LoadUserCallback buildLoadUserCallback(
            final TransferMethodConfigurationRepository.LoadKeysCallback loadKeysCallback) {
        return new UserRepository.LoadUserCallback() {
            @Override
            public void onUserLoaded(@Nullable HyperwalletUser user) {
                mUser = user;
                mTransferMethodConfigurationRepository.getKeys(loadKeysCallback);
            }

            @Override
            public void onError(@NonNull HyperwalletErrors errors) {
                loadKeysCallback.onError(errors);
            }
        };
    }

    private TransferMethodConfigurationRepository.LoadKeysCallback buildMethodKeysCallback(
            @NonNull final String countryCode, @NonNull final String currencyCode, final boolean isHandleProgress) {
        return new TransferMethodConfigurationRepository.LoadKeysCallback() {
            @Override
            public void onKeysLoaded(@Nullable final HyperwalletTransferMethodConfigurationKeyResult
                    transferMethodConfigurationKeyResult) {
                if (!mView.isActive()) {
                    return;
                }

                if (isHandleProgress) {
                    mView.hideProgressBar();
                }
                showMethodTypes(transferMethodConfigurationKeyResult, countryCode, currencyCode);
            }

            @Override
            public void onError(@NonNull final HyperwalletErrors errors) {
                if (!mView.isActive()) {
                    return;
                }
                if (isHandleProgress) {
                    mView.hideProgressBar();
                    mView.showErrorLoadTransferMethodConfigurationKeys(errors.getErrors());
                } else {
                    mView.showErrorLoadTransferMethodTypes(errors.getErrors());
                }
            }
        };
    }

    private TransferMethodConfigurationRepository.LoadKeysCallback buildLoadCurrencyCallback(
            @NonNull final String countryCode) {
        return new TransferMethodConfigurationRepository.LoadKeysCallback() {
            @Override
            public void onKeysLoaded(@Nullable final HyperwalletTransferMethodConfigurationKeyResult result) {
                if (!mView.isActive()) {
                    return;
                }
                List<String> transferMethodCurrencies = result.getCurrencies(countryCode);
                showMethodTypes(result, countryCode, transferMethodCurrencies.get(0));
            }

            @Override
            public void onError(@NonNull final HyperwalletErrors errors) {
                if (!mView.isActive()) {
                    return;
                }
                mView.showErrorLoadCurrency(errors.getErrors());
            }
        };
    }

    private void showMethodTypes(
            @Nullable HyperwalletTransferMethodConfigurationKeyResult transferMethodConfigurationKeyResult,
            @NonNull String countryCode, @NonNull String currencyCode) {
        final String userProfileType = mUser.getField(PROFILE_TYPE);
        List<String> transferMethodTypes = transferMethodConfigurationKeyResult.getTransferMethods(
                countryCode, currencyCode, userProfileType);

        mView.showTransferMethodCountry(countryCode);
        mView.showTransferMethodCurrency(currencyCode);
        mView.showTransferMethodTypes(getTransferMethodSelectionItems(countryCode, currencyCode,
                transferMethodTypes, userProfileType, transferMethodConfigurationKeyResult));
    }

    private TransferMethodConfigurationRepository.LoadKeysCallback buildLoadCountrySelectionCallback(
            @NonNull final String countryCode) {
        return new TransferMethodConfigurationRepository.LoadKeysCallback() {
            @Override
            public void onKeysLoaded(
                    @Nullable final HyperwalletTransferMethodConfigurationKeyResult transferMethodConfigurationKeyResult) {
                if (!mView.isActive()) {
                    return;
                }

                List<String> countryCodes = transferMethodConfigurationKeyResult.getCountries();
                TreeMap<String, String> countryNameCodeMap = new TreeMap<>();
                Locale.Builder builder = new Locale.Builder();
                for (String countryCode1 : countryCodes) {
                    Locale locale = builder.setRegion(countryCode1).build();
                    countryNameCodeMap.put(locale.getDisplayName(), countryCode1);
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
        };
    }

    private TransferMethodConfigurationRepository.LoadKeysCallback buildCurrencySelectionCallback(
            @NonNull final String countryCode, @NonNull final String currencyCode) {
        return new TransferMethodConfigurationRepository.LoadKeysCallback() {
            @Override
            public void onKeysLoaded(
                    @Nullable HyperwalletTransferMethodConfigurationKeyResult transferMethodConfigurationKeyResult) {
                if (!mView.isActive()) {
                    return;
                }

                List<String> currencyCodes = transferMethodConfigurationKeyResult.getCurrencies(countryCode);
                TreeMap<String, String> currencyNameCodeMap = new TreeMap<>();
                for (String currencyCode1 : currencyCodes) {
                    Currency currency = Currency.getInstance(currencyCode1);
                    currencyNameCodeMap.put(currency.getDisplayName(), currencyCode1);
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
        };
    }

    private List<TransferMethodSelectionItem> getTransferMethodSelectionItems(
            @NonNull final String country, @NonNull final String currency,
            @NonNull final List<String> transferMethodTypes,
            @NonNull final String userProfileType,
            @NonNull final HyperwalletTransferMethodConfigurationKeyResult result) {

        List<TransferMethodSelectionItem> selectionItems = new ArrayList<>();
        for (String transferMethodType : transferMethodTypes) {
            List<Fee> fees = result.getFees(country, currency, transferMethodType, userProfileType);
            String processingTime = result.getProcessingTime(country, currency, transferMethodType, userProfileType);
            TransferMethodSelectionItem data = new TransferMethodSelectionItem(country, currency, userProfileType,
                    transferMethodType, processingTime, fees);
            selectionItems.add(data);
        }
        return selectionItems;
    }
}
