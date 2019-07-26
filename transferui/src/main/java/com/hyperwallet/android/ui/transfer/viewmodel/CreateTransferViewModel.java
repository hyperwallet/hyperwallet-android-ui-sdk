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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.model.user.HyperwalletUser;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.transfer.repository.TransferRepository;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepository;
import com.hyperwallet.android.ui.user.repository.UserRepository;

import java.util.UUID;

/**
 * Create Transfer View Model
 */
public class CreateTransferViewModel extends ViewModel {

    private static final String CLIENT_IDENTIFICATION_PREFIX = "HW-MBLA-";

    private final TransferRepository mTransferRepository;
    private final TransferMethodRepository mTransferMethodRepository;
    private final UserRepository mUserRepository;

    private final MutableLiveData<HyperwalletTransferMethod> mTransferDestination = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mTransferAvailableFunds = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsCreateQuoteLoading = new MutableLiveData<>();
    private final MutableLiveData<Transfer> mQuoteAvailableFunds = new MutableLiveData<>();
    private final MutableLiveData<Transfer> mQuoteTransfer = new MutableLiveData<>();
    private final MutableLiveData<String> mTransferAmount = new MutableLiveData<>();
    private final MutableLiveData<String> mTransferNotes = new MutableLiveData<>();

    // initialisation/loading error
    private Observer<Event<HyperwalletErrors>> mLoadErrorEventObserver;
    private final MutableLiveData<Event<HyperwalletErrors>> mLoadErrorEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<HyperwalletErrors>> mLoadErrors = new MutableLiveData<>();

    // create quote error
    private Observer<Event<HyperwalletErrors>> mQuoteEventObserver;
    private final MutableLiveData<Event<HyperwalletErrors>> mQuoteErrorEvent = new MutableLiveData<>();
    private final MutableLiveData<Event<HyperwalletErrors>> mQuoteErrors = new MutableLiveData<>();


    /**
     * Initialize Create Transfer View Model with designated transfer source token
     *
     * @param sourceToken              Source token that represents either from Wallet, Bank or other EA tokens
     * @param transferRepository       Transfer repository for making transfer calls to HW Transfer Platform
     * @param transferMethodRepository Transfer Method repository for making transfer method calls to HW Transfer
     *                                 Platform
     * @param userRepository           User repository for making user calls to HW Transfer Platform
     */
    CreateTransferViewModel(@NonNull final String sourceToken,
            @NonNull final TransferRepository transferRepository,
            @NonNull final TransferMethodRepository transferMethodRepository,
            @NonNull final UserRepository userRepository) {

        mTransferRepository = transferRepository;
        mTransferMethodRepository = transferMethodRepository;
        mUserRepository = userRepository;

        // initialized
        mTransferAvailableFunds.setValue(Boolean.FALSE);
        mIsLoading.postValue(Boolean.TRUE);
        mIsCreateQuoteLoading.setValue(Boolean.FALSE);
        createLoadErrorObserver();
        loadTransferDestination(sourceToken);
    }

    /**
     * Initialize Create Transfer View Model with designated transfer source token
     *
     * @param transferRepository       Transfer repository for making transfer calls to HW Transfer Platform
     * @param transferMethodRepository Transfer Method repository for making transfer method calls to HW Transfer
     *                                 Platform
     * @param userRepository           User repository for making user calls to HW Transfer Platform
     */
    CreateTransferViewModel(@NonNull final TransferRepository transferRepository,
            @NonNull final TransferMethodRepository transferMethodRepository,
            @NonNull final UserRepository userRepository) {

        mTransferRepository = transferRepository;
        mTransferMethodRepository = transferMethodRepository;
        mUserRepository = userRepository;

        // initialized
        mTransferAvailableFunds.setValue(Boolean.FALSE);
        mIsLoading.postValue(Boolean.TRUE);
        mIsCreateQuoteLoading.setValue(Boolean.FALSE);
        createLoadErrorObserver();
        loadTransferSource();
        createQuoteErrorObserver();
    }

    public LiveData<Boolean> isTransferAllAvailableFunds() {
        return mTransferAvailableFunds;
    }

    public void setTransferAllAvailableFunds(Boolean transferAll) {
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

    public LiveData<Boolean> isLoading() {
        return mIsLoading;
    }

    public LiveData<Boolean> isCreateQuoteLoading() {
        return mIsCreateQuoteLoading;
    }

    public LiveData<Transfer> getQuoteAvailableFunds() {
        return mQuoteAvailableFunds;
    }

    public LiveData<Event<HyperwalletErrors>> getLoadErrorEvent() {
        return mLoadErrorEvent;
    }

    public LiveData<Transfer> getQuoteTransfer() {
        return mQuoteTransfer;
    }

    public LiveData<Event<HyperwalletErrors>> getQuoteErrors() {
        return mQuoteErrorEvent;
    }

    public void createQuoteTransfer() {
        mIsCreateQuoteLoading.postValue(Boolean.TRUE);
        Transfer transfer = mTransferAvailableFunds.getValue() ?
                new Transfer.Builder()
                        .clientTransferID(CLIENT_IDENTIFICATION_PREFIX + UUID.randomUUID().toString())
                        .sourceToken(mQuoteAvailableFunds.getValue().getSourceToken())
                        .destinationToken(mTransferDestination.getValue().getField(TOKEN))
                        .destinationCurrency(mTransferDestination.getValue().getField(TRANSFER_METHOD_CURRENCY))
                        .notes(mTransferNotes.getValue())
                        .build() :
                new Transfer.Builder()
                        .clientTransferID(CLIENT_IDENTIFICATION_PREFIX + UUID.randomUUID().toString())
                        .sourceToken(mQuoteAvailableFunds.getValue().getSourceToken())
                        .destinationToken(mTransferDestination.getValue().getField(TOKEN))
                        .destinationCurrency(mTransferDestination.getValue().getField(TRANSFER_METHOD_CURRENCY))
                        .notes(mTransferNotes.getValue())
                        .destinationAmount(mTransferAmount.getValue())
                        .build();

        mTransferRepository.createTransfer(transfer, new TransferRepository.CreateTransferCallback() {
            @Override
            public void onTransferCreated(@Nullable Transfer transfer) {
                mQuoteTransfer.postValue(transfer);
                mIsCreateQuoteLoading.postValue(Boolean.FALSE);
            }

            @Override
            public void onError(@NonNull HyperwalletErrors errors) {
                mQuoteErrors.postValue(new Event<>(errors));
                mIsCreateQuoteLoading.postValue(Boolean.FALSE);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mLoadErrors.removeObserver(mLoadErrorEventObserver);
        mQuoteErrors.removeObserver(mQuoteEventObserver);
    }

    private void createLoadErrorObserver() {
        mLoadErrorEventObserver = new Observer<Event<HyperwalletErrors>>() {
            @Override
            public void onChanged(Event<HyperwalletErrors> event) {
                mLoadErrorEvent.postValue(event);
            }
        };
        mLoadErrors.observeForever(mLoadErrorEventObserver);
    }

    private void createQuoteErrorObserver() {
        mQuoteEventObserver = new Observer<Event<HyperwalletErrors>>() {
            @Override
            public void onChanged(Event<HyperwalletErrors> event) {
                mQuoteErrorEvent.postValue(event);
            }
        };
        mQuoteErrors.observeForever(mQuoteEventObserver);
    }

    private void loadTransferSource() {
        mUserRepository.loadUser(new UserRepository.LoadUserCallback() {
            @Override
            public void onUserLoaded(@NonNull HyperwalletUser user) {
                loadTransferDestination(user.getToken());
            }

            @Override
            public void onError(@NonNull HyperwalletErrors errors) {
                mIsLoading.postValue(Boolean.FALSE);
                mLoadErrors.postValue(new Event<>(errors));
            }
        });
    }

    private void loadTransferDestination(@NonNull final String sourceToken) {
        mTransferMethodRepository.loadLatestTransferMethod(new TransferMethodRepository.LoadTransferMethodCallback() {
            @Override
            public void onTransferMethodLoaded(@Nullable HyperwalletTransferMethod transferMethod) {
                mTransferDestination.postValue(transferMethod);
                quoteAvailableTransferFunds(sourceToken, transferMethod);
            }

            @Override
            public void onError(HyperwalletErrors errors) {
                mIsLoading.postValue(Boolean.FALSE);
                mLoadErrors.postValue(new Event<>(errors));
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
                mLoadErrors.postValue(new Event<>(errors));
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
            if (TextUtils.isEmpty(sourceToken)) {
                return (T) new CreateTransferViewModel(transferRepository, transferMethodRepository, userRepository);
            }
            return (T) new CreateTransferViewModel(sourceToken, transferRepository, transferMethodRepository,
                    userRepository);
        }
    }
}
