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

import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.model.user.HyperwalletUser;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.transfer.repository.TransferRepository;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepository;
import com.hyperwallet.android.ui.user.repository.UserRepository;

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

    private final MutableLiveData<HyperwalletTransferMethod> mTransferDestination = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mTransferAvailableFunds = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsCreateQuoteLoading = new MutableLiveData<>();
    private final MutableLiveData<Transfer> mQuoteAvailableFunds = new MutableLiveData<>();
    private final MutableLiveData<Event<Transfer>> mCreateTransfer = new MutableLiveData<>();
    private final MutableLiveData<String> mTransferAmount = new MutableLiveData<>();
    private final MutableLiveData<String> mTransferNotes = new MutableLiveData<>();

    private final MutableLiveData<Event<HyperwalletErrors>> mLoadTransferRequiredDataErrors = new MutableLiveData<>();
    private final MutableLiveData<Event<HyperwalletErrors>> mCreateTransferError = new MutableLiveData<>();
    private final MutableLiveData<Event<HyperwalletError>> mInvalidAmountError = new MutableLiveData<>();
    private final MutableLiveData<Event<HyperwalletError>> mInvalidDestinationError = new MutableLiveData<>();

    private String mSourceToken;

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
        loadTransferDestination(sourceToken);
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
        loadTransferSource();
    }

    public LiveData<Boolean> isTransferAllAvailableFunds() {
        return mTransferAvailableFunds;
    }

    public void setTransferAllAvailableFunds(@NonNull final Boolean transferAll) {
        mTransferAvailableFunds.postValue(transferAll);
    }

    public void setTransferAmount(@NonNull final String amount) {
        mTransferAmount.postValue(amount);
    }

    public LiveData<String> getTransferAmount() {
        return mTransferAmount;
    }

    public void setTransferNotes(@Nullable final String notes) {
        mTransferNotes.postValue(notes);
    }

    public LiveData<String> getTransferNotes() {
        return mTransferNotes;
    }

    public LiveData<HyperwalletTransferMethod> getTransferDestination() {
        return mTransferDestination;
    }

    public void setTransferDestination(@NonNull final HyperwalletTransferMethod transferDestination) {
        mTransferDestination.postValue(transferDestination);
        mIsLoading.postValue(Boolean.TRUE);
        quoteAvailableTransferFunds(mSourceToken, transferDestination);
    }

    public LiveData<Boolean> isLoading() {
        return mIsLoading;
    }

    public LiveData<Boolean> isCreateQuoteLoading() {
        return mIsCreateQuoteLoading;
    }

    public LiveData<Transfer> getQuoteAvailableFunds() {
        return mQuoteAvailableFunds;
    }

    public LiveData<Event<HyperwalletErrors>> getLoadTransferRequiredDataErrors() {
        return mLoadTransferRequiredDataErrors;
    }

    public LiveData<Event<Transfer>> getCreateTransfer() {
        return mCreateTransfer;
    }

    public LiveData<Event<HyperwalletErrors>> getCreateTransferError() {
        return mCreateTransferError;
    }

    public LiveData<Event<HyperwalletError>> getInvalidAmountError() {
        return mInvalidAmountError;
    }

    public LiveData<Event<HyperwalletError>> getInvalidDestinationError() {
        return mInvalidDestinationError;
    }

    public void createTransfer() {
        mIsCreateQuoteLoading.postValue(Boolean.TRUE);
        Transfer transfer = mTransferAvailableFunds.getValue() ?
                new Transfer.Builder()
                        .clientTransferID(CLIENT_IDENTIFICATION_PREFIX + UUID.randomUUID().toString())
                        .sourceToken(mSourceToken)
                        .destinationToken(mTransferDestination.getValue().getField(TOKEN))
                        .destinationCurrency(mTransferDestination.getValue().getField(TRANSFER_METHOD_CURRENCY))
                        .notes(mTransferNotes.getValue())
                        .build() :
                new Transfer.Builder()
                        .clientTransferID(CLIENT_IDENTIFICATION_PREFIX + UUID.randomUUID().toString())
                        .sourceToken(mSourceToken)
                        .destinationToken(mTransferDestination.getValue().getField(TOKEN))
                        .destinationCurrency(mTransferDestination.getValue().getField(TRANSFER_METHOD_CURRENCY))
                        .notes(mTransferNotes.getValue())
                        .destinationAmount(mTransferAmount.getValue())
                        .build();

        mTransferRepository.createTransfer(transfer, new TransferRepository.CreateTransferCallback() {
            @Override
            public void onTransferCreated(@Nullable Transfer transfer) {
                mCreateTransfer.postValue(new Event<>(transfer));
                mIsCreateQuoteLoading.postValue(Boolean.FALSE);
            }

            @Override
            public void onError(@NonNull final HyperwalletErrors errors) {
                processCreateTransferError(errors);
                mIsCreateQuoteLoading.postValue(Boolean.FALSE);
            }
        });
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

    private void processCreateTransferError(@NonNull final HyperwalletErrors errors) {
        if (errors.containsInputError()) {
            HyperwalletError error = errors.getErrors().get(0);
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

    private void loadTransferSource() {
        mUserRepository.loadUser(new UserRepository.LoadUserCallback() {
            @Override
            public void onUserLoaded(@NonNull HyperwalletUser user) {
                mSourceToken = user.getToken();
                loadTransferDestination(mSourceToken);
            }

            @Override
            public void onError(@NonNull HyperwalletErrors errors) {
                mIsLoading.postValue(Boolean.FALSE);
                mLoadTransferRequiredDataErrors.postValue(new Event<>(errors));
            }
        });
    }

    private void loadTransferDestination(@NonNull final String sourceToken) {
        mTransferMethodRepository.loadLatestTransferMethod(new TransferMethodRepository.LoadTransferMethodCallback() {
            @Override
            public void onTransferMethodLoaded(@Nullable HyperwalletTransferMethod transferMethod) {
                mTransferDestination.postValue(transferMethod);
                if (transferMethod == null) { // dismiss quote
                    mIsLoading.postValue(Boolean.FALSE);
                } else {
                    quoteAvailableTransferFunds(sourceToken, transferMethod);
                }
            }

            @Override
            public void onError(HyperwalletErrors errors) {
                mIsLoading.postValue(Boolean.FALSE);
                mLoadTransferRequiredDataErrors.postValue(new Event<>(errors));
            }
        });
    }

    private void quoteAvailableTransferFunds(@NonNull final String sourceToken,
            @NonNull final HyperwalletTransferMethod transferMethod) {

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
            public void onError(@NonNull HyperwalletErrors errors) {
                mIsLoading.postValue(Boolean.FALSE);
                mQuoteAvailableFunds.setValue(null);
                mLoadTransferRequiredDataErrors.postValue(new Event<>(errors));
            }
        });
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
