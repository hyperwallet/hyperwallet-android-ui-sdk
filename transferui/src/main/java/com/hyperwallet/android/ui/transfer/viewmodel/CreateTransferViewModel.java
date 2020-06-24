/*
 * The MIT License (MIT)
 * Copyright (c) 2019 Hyperwallet Systems Inc.
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
package com.hyperwallet.android.ui.transfer.viewmodel;

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.ERROR_SDK_MODULE_UNAVAILABLE;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.model.user.User;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.repository.TransferRepository;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepository;
import com.hyperwallet.android.ui.user.repository.UserRepository;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Create Transfer View Model
 */
public class CreateTransferViewModel extends ViewModel {

    private static final String CLIENT_IDENTIFICATION_PREFIX = "HW-ANDROID-";
    private static final String DESTINATION_AMOUNT_INPUT_FIELD = "destinationAmount";
    private static final String DESTINATION_TOKEN_INPUT_FIELD = "destinationToken";

    private final TransferRepository mTransferRepository;
    private final TransferMethodRepository mTransferMethodRepository;
    private final UserRepository mUserRepository;

    private final MutableLiveData<TransferMethod> mTransferDestination = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mTransferAvailableFunds = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsCreateQuoteLoading = new MutableLiveData<>();
    private final MutableLiveData<Transfer> mQuoteAvailableFunds = new MutableLiveData<>();
    private final MutableLiveData<Event<Transfer>> mCreateTransfer = new MutableLiveData<>();
    private final MutableLiveData<String> mTransferAmount = new MutableLiveData<>();
    private final MutableLiveData<String> mTransferNotes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mShowFxRateChange = new MutableLiveData<>();

    private final MutableLiveData<Event<Errors>> mLoadTransferRequiredDataErrors = new MutableLiveData<>();
    private final MutableLiveData<Event<Errors>> mCreateTransferError = new MutableLiveData<>();
    private final MutableLiveData<Event<Errors>> mModuleUnavailableError = new MutableLiveData<>();
    private final MutableLiveData<Event<Error>> mInvalidAmountError = new MutableLiveData<>();
    private final MutableLiveData<Event<Error>> mInvalidDestinationError = new MutableLiveData<>();

    private String mSourceToken;
    private boolean mIsInitialized;
    private String initialAmount;

    /**
     * Initialize Create Transfer View Model with designated transfer source token
     *
     * @param sourceToken              Source token that represents either from User, Bank or PrepaidCard tokens
     * @param transferRepository       Transfer repository for making transfer calls to Hyperwallet
     * @param transferMethodRepository Transfer Method repository for making transfer method calls to Hyperwallet
     * @param userRepository           User repository for making user calls to Hyperwallet
     */
    CreateTransferViewModel(@NonNull final String sourceToken,
            @NonNull final TransferRepository transferRepository,
            @NonNull final TransferMethodRepository transferMethodRepository,
            @NonNull final UserRepository userRepository) {

        mTransferRepository = transferRepository;
        mTransferMethodRepository = transferMethodRepository;
        mUserRepository = userRepository;
        mSourceToken = sourceToken;

        // initialize
        mTransferAvailableFunds.setValue(Boolean.FALSE);
        mIsLoading.postValue(Boolean.TRUE);
        mIsCreateQuoteLoading.setValue(Boolean.FALSE);
        mShowFxRateChange.setValue(Boolean.FALSE);
    }

    /**
     * Initialize Create Transfer View Model with designated transfer source token
     *
     * @param transferRepository       Transfer repository for making transfer calls to Hyperwallet
     * @param transferMethodRepository Transfer Method repository for making transfer method calls to Hyperwallet
     * @param userRepository           User repository for making user calls to Hyperwallet
     */
    CreateTransferViewModel(@NonNull final TransferRepository transferRepository,
            @NonNull final TransferMethodRepository transferMethodRepository,
            @NonNull final UserRepository userRepository) {

        mTransferRepository = transferRepository;
        mTransferMethodRepository = transferMethodRepository;
        mUserRepository = userRepository;

        // initialize
        mTransferAvailableFunds.setValue(Boolean.FALSE);
        mIsLoading.postValue(Boolean.TRUE);
        mIsCreateQuoteLoading.setValue(Boolean.FALSE);
        mShowFxRateChange.setValue(Boolean.FALSE);
    }

    public void init(@NonNull final String defaultAmount) {
        initialAmount = defaultAmount;

        if (!mIsInitialized) {
            mIsInitialized = true;
            if (mSourceToken == null) {
                loadTransferSource();
            } else {
                loadTransferDestination(mSourceToken);
            }
        }
    }

    /**
     * Refresh this view model please only call this when this view model is initialized
     * or else it will just do nothing
     */
    public void refresh() {
        mTransferAmount.postValue(null);
        mTransferNotes.postValue(null);
        if (!isTransferDestinationUnknown()
                && !isTransferSourceTokenUnknown() && mIsInitialized) {
            quoteAvailableTransferFunds(mSourceToken, mTransferDestination.getValue());
        } else if (isTransferDestinationUnknown()
                && !isTransferSourceTokenUnknown() && mIsInitialized) {
            loadTransferDestination(mSourceToken);
        }
    }

    public LiveData<Boolean> isTransferAllAvailableFunds() {
        return mTransferAvailableFunds;
    }

    public void setTransferAllAvailableFunds(@NonNull final Boolean transferAll) {
        mTransferAvailableFunds.postValue(transferAll);
    }

    public LiveData<String> getTransferAmount() {
        return mTransferAmount;
    }

    public void setTransferAmount(@NonNull final String amount) {
        mTransferAmount.postValue(amount);
    }

    public LiveData<String> getTransferNotes() {
        return mTransferNotes;
    }

    public void setTransferNotes(@Nullable final String notes) {
        mTransferNotes.postValue(notes);
    }

    public LiveData<TransferMethod> getTransferDestination() {
        return mTransferDestination;
    }

    public void setTransferDestination(@NonNull final TransferMethod transferDestination) {
        mTransferDestination.postValue(transferDestination);
        quoteAvailableTransferFunds(mSourceToken, transferDestination);
    }

    public LiveData<Boolean> isLoading() {
        return mIsLoading;
    }

    public LiveData<Boolean> isCreateQuoteLoading() {
        return mIsCreateQuoteLoading;
    }

    public void setCreateQuoteLoading(final Boolean loading) {
        mIsCreateQuoteLoading.postValue(loading);
    }

    public LiveData<Transfer> getQuoteAvailableFunds() {
        return mQuoteAvailableFunds;
    }

    public LiveData<Event<Errors>> getLoadTransferRequiredDataErrors() {
        return mLoadTransferRequiredDataErrors;
    }

    public LiveData<Event<Transfer>> getCreateTransfer() {
        return mCreateTransfer;
    }

    public LiveData<Event<Errors>> getCreateTransferError() {
        return mCreateTransferError;
    }

    public LiveData<Event<Error>> getInvalidAmountError() {
        return mInvalidAmountError;
    }

    public LiveData<Event<Error>> getInvalidDestinationError() {
        return mInvalidDestinationError;
    }

    public LiveData<Event<Errors>> getModuleUnavailableError() {
        return mModuleUnavailableError;
    }

    public LiveData<Boolean> getShowFxRateChange() {
        return mShowFxRateChange;
    }

    public void notifyModuleUnavailable() {
        Error error = new Error(R.string.module_transfermethodui_unavailable_error,
                ERROR_SDK_MODULE_UNAVAILABLE);
        Errors errors = new Errors(Arrays.asList(error));
        mModuleUnavailableError.postValue(new Event<>(errors));
    }

    public void createTransfer() {
        mIsCreateQuoteLoading.postValue(Boolean.TRUE);
        String amount = isTransferRequestSameWithQuote() ? null : mTransferAmount.getValue();

        Transfer transfer = new Transfer.Builder()
                .clientTransferID(CLIENT_IDENTIFICATION_PREFIX + UUID.randomUUID().toString())
                .sourceToken(mSourceToken)
                .destinationToken(mTransferDestination.getValue().getField(TOKEN))
                .destinationCurrency(mTransferDestination.getValue().getField(TRANSFER_METHOD_CURRENCY))
                .notes(mTransferNotes.getValue())
                .destinationAmount(amount)
                .build();

        mTransferRepository.createTransfer(transfer, new TransferRepository.CreateTransferCallback() {
            @Override
            public void onTransferCreated(@Nullable Transfer transfer) {
                mShowFxRateChange.setValue(hasTransferAmountChanged(transfer));
                mCreateTransfer.postValue(new Event<>(transfer));
                mTransferAvailableFunds.setValue(Boolean.FALSE);
            }

            @Override
            public void onError(@NonNull final Errors errors) {
                processCreateTransferError(errors);
                mIsCreateQuoteLoading.postValue(Boolean.FALSE);
                mTransferAvailableFunds.setValue(Boolean.FALSE);
            }
        });
    }

    public void refreshTransferDestination() {
        if (isTransferSourceTokenUnknown()) {
            loadTransferSource();
        } else if (isTransferDestinationUnknown()) {
            loadTransferDestination(mSourceToken);
        }
    }

    public void retry() {
        if (isTransferSourceTokenUnknown()) {
            loadTransferSource();
        } else if (isTransferDestinationUnknown()) {
            loadTransferDestination(mSourceToken);
        } else if (isQuoteInvalid()) {
            quoteAvailableTransferFunds(mSourceToken, mTransferDestination.getValue());
        } else if (isTransferAmountKnown()) {
            createTransfer();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public boolean isTransferDestinationUnknown() {
        return mTransferDestination.getValue() == null;
    }

    private void processCreateTransferError(@NonNull final Errors errors) {
        if (errors.containsInputError()) {
            Error error = errors.getErrors().get(0);
            if (Objects.equals(error.getFieldName(), DESTINATION_AMOUNT_INPUT_FIELD)) {
                mInvalidAmountError.postValue(new Event<>(error));
            } else if (Objects.equals(error.getFieldName(), DESTINATION_TOKEN_INPUT_FIELD)) {
                mInvalidDestinationError.postValue(new Event<>(error));
            } else {
                mCreateTransferError.postValue(new Event<>(errors));
            }
        } else {
            mCreateTransferError.postValue(new Event<>(errors));
        }
    }

    private boolean isTransferRequestSameWithQuote() {
        return mQuoteAvailableFunds.getValue() != null
                && !TextUtils.isEmpty(mQuoteAvailableFunds.getValue().getDestinationAmount())
                && mQuoteAvailableFunds.getValue().getDestinationAmount().equals(mTransferAmount.getValue());
    }

    private boolean isTransferSourceTokenUnknown() {
        return TextUtils.isEmpty(mSourceToken);
    }

    private boolean isTransferAmountKnown() {
        return !TextUtils.isEmpty(mTransferAmount.getValue());
    }

    private boolean isQuoteInvalid() {
        return mQuoteAvailableFunds.getValue() == null
                || !Objects.equals(mQuoteAvailableFunds.getValue().getSourceToken(), mSourceToken)
                || !Objects.equals(mQuoteAvailableFunds.getValue().getDestinationToken(),
                mTransferDestination.getValue().getField(TOKEN));
    }

    @VisibleForTesting
    void loadTransferSource() {
        mIsLoading.postValue(Boolean.TRUE);
        mUserRepository.loadUser(new UserRepository.LoadUserCallback() {
            @Override
            public void onUserLoaded(@NonNull User user) {
                mSourceToken = user.getToken();
                loadTransferDestination(mSourceToken);
            }

            @Override
            public void onError(@NonNull Errors errors) {
                mIsLoading.postValue(Boolean.FALSE);
                mLoadTransferRequiredDataErrors.postValue(new Event<>(errors));
            }
        });
    }

    @VisibleForTesting
    void loadTransferDestination(@NonNull final String sourceToken) {
        mIsLoading.postValue(Boolean.TRUE);
        mTransferMethodRepository.loadLatestTransferMethod(new TransferMethodRepository.LoadTransferMethodCallback() {
            @Override
            public void onTransferMethodLoaded(@Nullable TransferMethod transferMethod) {
                mTransferDestination.postValue(transferMethod);
                if (transferMethod == null) { // dismiss quote
                    mIsLoading.postValue(Boolean.FALSE);
                } else {
                    quoteAvailableTransferFunds(sourceToken, transferMethod);
                }
            }

            @Override
            public void onError(Errors errors) {
                mIsLoading.postValue(Boolean.FALSE);
                mLoadTransferRequiredDataErrors.postValue(new Event<>(errors));
            }
        });
    }

    private void quoteAvailableTransferFunds(@NonNull final String sourceToken,
            @NonNull final TransferMethod transferMethod) {

        mIsLoading.postValue(Boolean.TRUE);
        Transfer transfer = new Transfer.Builder()
                .clientTransferID(CLIENT_IDENTIFICATION_PREFIX + UUID.randomUUID().toString())
                .sourceToken(sourceToken)
                .destinationToken(transferMethod.getField(TOKEN))
                .destinationCurrency(transferMethod.getField(TRANSFER_METHOD_CURRENCY))
                .build();

        mTransferRepository.createTransfer(transfer, new TransferRepository.CreateTransferCallback() {
            @Override
            public void onTransferCreated(@Nullable Transfer transfer) {
                mIsLoading.postValue(Boolean.FALSE);
                mQuoteAvailableFunds.postValue(transfer);
            }

            @Override
            public void onError(@NonNull Errors errors) {
                mIsLoading.postValue(Boolean.FALSE);
                mQuoteAvailableFunds.setValue(null);
                if (errors.containsInputError()) {
                    Error error = errors.getErrors().get(0);
                    if (Objects.equals(error.getFieldName(), DESTINATION_TOKEN_INPUT_FIELD)) {
                        mInvalidDestinationError.postValue(new Event<>(error));
                        return;
                    }
                }
                mLoadTransferRequiredDataErrors.postValue(new Event<>(errors));
            }
        });
    }


    private boolean hasTransferAmountChanged(@Nullable final Transfer transfer) {
        return mTransferAvailableFunds.getValue() && transfer != null && !TextUtils.equals(
                transfer.getDestinationAmount(), mTransferAmount.getValue());
    }


    public static class CreateTransferViewModelFactory implements ViewModelProvider.Factory {

        private final TransferRepository transferRepository;
        private final TransferMethodRepository transferMethodRepository;
        private final UserRepository userRepository;
        private final String sourceToken;

        public CreateTransferViewModelFactory(@NonNull final String sourceToken,
                @NonNull final TransferRepository transferRepository,
                @NonNull final TransferMethodRepository transferMethodRepository,
                @NonNull final UserRepository userRepository) {
            this.sourceToken = sourceToken;
            this.transferMethodRepository = transferMethodRepository;
            this.transferRepository = transferRepository;
            this.userRepository = userRepository;
        }

        public CreateTransferViewModelFactory(@NonNull final TransferRepository transferRepository,
                @NonNull final TransferMethodRepository transferMethodRepository,
                @NonNull final UserRepository userRepository) {
            this.sourceToken = null;
            this.transferMethodRepository = transferMethodRepository;
            this.transferRepository = transferRepository;
            this.userRepository = userRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CreateTransferViewModel.class)) {
                if (TextUtils.isEmpty(sourceToken)) {
                    return (T) new CreateTransferViewModel(transferRepository, transferMethodRepository,
                            userRepository);
                }
                return (T) new CreateTransferViewModel(sourceToken, transferRepository, transferMethodRepository,
                        userRepository);
            }

            throw new IllegalArgumentException("Expecting ViewModel class: " + CreateTransferViewModel.class.getName());
        }
    }
}
