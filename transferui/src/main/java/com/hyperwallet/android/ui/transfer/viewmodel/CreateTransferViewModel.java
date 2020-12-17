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

import static com.hyperwallet.android.model.transfer.Transfer.EMPTY_STRING;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.BANK_ACCOUNT;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.PREPAID_CARD;
import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.ERROR_SDK_MODULE_UNAVAILABLE;

import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hyperwallet.android.Configuration;
import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.model.transfermethod.PrepaidCard;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.model.user.User;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.common.view.ProgramModel;
import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.TransferSource;
import com.hyperwallet.android.ui.transfer.repository.TransferRepository;
import com.hyperwallet.android.ui.transfermethod.repository.PrepaidCardRepository;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepository;
import com.hyperwallet.android.ui.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.UUID;

/**
 * Create Transfer View Model
 */
public class CreateTransferViewModel extends ViewModel {

    private static final String CLIENT_IDENTIFICATION_PREFIX = "HW-ANDROID-";
    private static final String DESTINATION_AMOUNT_INPUT_FIELD = "destinationAmount";
    private static final String DESTINATION_TOKEN_INPUT_FIELD = "destinationToken";
    private static final String PRIVATE_TOKEN_PREFIX = "trm-";
    public static final String CURRENCY_DOT_SEPARATOR = ".";


    private final TransferRepository mTransferRepository;
    private final TransferMethodRepository mTransferMethodRepository;
    private final UserRepository mUserRepository;
    private final PrepaidCardRepository mPrepaidCardRepository;

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
    private final MutableLiveData<ArrayList<TransferSource>> mTransferSources = new MutableLiveData<>();
    private final MutableLiveData<TransferSource> mSelectedTransferSource = new MutableLiveData<>();

    private String mSourceToken;
    private String initialAmount;
    private boolean mIsPortraitMode;
    private ProgramModel mProgramModel;
    private boolean isCardModel;

    private boolean updateTransferAllFunds;
    private String mDecimalSeparator;
    private String mGroupSeparator;
    private boolean mIsQuoteAvailableTransferFunds = false;


    /**
     * Initialize Create Transfer View Model with designated transfer source token
     *
     * @param sourceToken              Source token that represents either from User, Bank or PrepaidCard tokens
     * @param transferRepository       Transfer repository for making transfer calls to Hyperwallet
     * @param transferMethodRepository Transfer Method repository for making transfer method calls to
     *                                 Hyperwallet
     * @param userRepository           User repository for making user calls to Hyperwallet
     * @param prepaidCardRepository    prepaid card repository for making prepaid calls to Hyperwallet
     */
    CreateTransferViewModel(@NonNull final String sourceToken,
            @NonNull final TransferRepository transferRepository,
            @NonNull final TransferMethodRepository transferMethodRepository,
            @NonNull final UserRepository userRepository, @NonNull PrepaidCardRepository prepaidCardRepository) {

        mTransferRepository = transferRepository;
        mTransferMethodRepository = transferMethodRepository;
        mUserRepository = userRepository;
        mSourceToken = sourceToken;
        mPrepaidCardRepository = prepaidCardRepository;

        // initialize
        initialize();
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
            @NonNull final UserRepository userRepository, @NonNull final PrepaidCardRepository prepaidCardRepository) {

        mTransferRepository = transferRepository;
        mTransferMethodRepository = transferMethodRepository;
        mUserRepository = userRepository;
        mPrepaidCardRepository = prepaidCardRepository;

        // Initialize
        initialize();
    }

    private void initialize() {
        mTransferAvailableFunds.setValue(Boolean.FALSE);
        mIsLoading.postValue(Boolean.TRUE);
        mIsCreateQuoteLoading.setValue(Boolean.FALSE);
        mShowFxRateChange.setValue(Boolean.FALSE);
    }

    public void init(@NonNull final String defaultAmount) {
        initialAmount = defaultAmount;
        isCardModel = ProgramModel.isCardModel(getProgramModel());
        if (mSourceToken == null) {
            loadTransferSource();
        } else {
            loadTransferSource(mSourceToken);
        }
    }

    /**
     * Refresh this view model please only call this when this view model is initialized
     * or else it will just do nothing
     */
    public void refresh(String amount) {
        mIsQuoteAvailableTransferFunds = false;
        mTransferAmount.postValue(null);
        mTransferNotes.postValue(null);
        mSourceToken = null;
        // Re-Initialize
        initialize();
        init(amount);
    }

    public boolean isPortraitMode() {
        return mIsPortraitMode;
    }

    public void setQuoteAvailableTransferFunds(boolean availableTransferFundsError){
        mIsQuoteAvailableTransferFunds = availableTransferFundsError;
    }

    public void setPortraitMode(final boolean portraitMode) {
        mIsPortraitMode = portraitMode;
    }

    public LiveData<Boolean> isTransferAllAvailableFunds() {
        return mTransferAvailableFunds;
    }

    public boolean isUpdateTransferAllFunds() {
        return updateTransferAllFunds;
    }

    public void setUpdateTransferAllFunds(boolean updateTransferAllFunds) {
        this.updateTransferAllFunds = updateTransferAllFunds;
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
        mIsQuoteAvailableTransferFunds=false;
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

    public LiveData<ArrayList<TransferSource>> getTransferSources() {
        return mTransferSources;
    }

    public LiveData<TransferSource> getTransferSelectedSource() {
        return mSelectedTransferSource;
    }

    public void setDecimalSeparator(String separator) {
        mDecimalSeparator = separator;
    }

    public void setGroupSeparator(String groupSeparator) {
        mGroupSeparator = groupSeparator;
    }

    public void setSelectedTransferSource(@NonNull final TransferSource source) {
        mIsQuoteAvailableTransferFunds = false;
        mSelectedTransferSource.postValue(source);
        mSourceToken = source.getToken();
        if (mTransferDestination.getValue() == null) {
            loadTransferDestination(mSourceToken);
        } else if (source.getType().equals(PREPAID_CARD) && Objects.equals(mTransferDestination.getValue().getField(
                TYPE), PREPAID_CARD)) {
            loadTransferDestination(mSourceToken);
        } else {
            quoteAvailableTransferFunds(source.getToken(), mTransferDestination.getValue());
        }
    }

    public void notifyModuleUnavailable() {
        Error error = new Error(R.string.module_transfermethodui_unavailable_error,
                ERROR_SDK_MODULE_UNAVAILABLE);
        Errors errors = new Errors(Arrays.asList(error));
        mModuleUnavailableError.postValue(new Event<>(errors));
    }

    public void notifySourceUnavailable() {
        Error error = new Error(R.string.noTransferFromSourceAvailableError,
                ERROR_SDK_MODULE_UNAVAILABLE);
        Errors errors = new Errors(Arrays.asList(error));
        mModuleUnavailableError.postValue(new Event<>(errors));
    }

    public void createTransfer() {
        mIsCreateQuoteLoading.postValue(Boolean.TRUE);
        if (!mIsQuoteAvailableTransferFunds) {
            mIsQuoteAvailableTransferFunds = true;
            quoteAvailableTransferFunds(mSourceToken, mTransferDestination.getValue());
        } else {
            String amount = isTransferRequestSameWithQuote() ? null : mTransferAmount.getValue();
            if (amount != null) {
                amount = amount.replace(mGroupSeparator, EMPTY_STRING).replace(mDecimalSeparator, CURRENCY_DOT_SEPARATOR);
            }
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
        if (!isCardModel) {
            mIsLoading.postValue(Boolean.TRUE);
            mUserRepository.loadUser(new UserRepository.LoadUserCallback() {
                @Override
                public void onUserLoaded(@NonNull User user) {
                    mSourceToken = user.getToken();
                    ArrayList<TransferSource> sources = new ArrayList<>();
                    TransferSource sourceWrapperForAvailableFunds = new TransferSource();
                    sourceWrapperForAvailableFunds.setToken(user.getToken());
                    sourceWrapperForAvailableFunds.setType(BANK_ACCOUNT);
                    sources.add(sourceWrapperForAvailableFunds);
                    loadPrepaidCardList(sources);
                }

                @Override
                public void onError(@NonNull Errors errors) {
                    mIsLoading.postValue(Boolean.FALSE);
                    mLoadTransferRequiredDataErrors.postValue(new Event<>(errors));
                }
            });
        } else {
            loadPrepaidCardList(new ArrayList<TransferSource>());
        }
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
                } else if (mSelectedTransferSource.getValue() != null && Objects.equals(transferMethod.getField(TYPE),
                        PREPAID_CARD) && mSelectedTransferSource.getValue().getType().equals(PREPAID_CARD)) {
                    loadTransferDestinationListWithoutPPC(sourceToken);
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

    @VisibleForTesting
    void loadPrepaidCard(@NonNull final String token) {
        mIsLoading.postValue(Boolean.TRUE);
        mPrepaidCardRepository.loadPrepaidCard(token, new PrepaidCardRepository.LoadPrepaidCardCallback() {
            @Override
            public void onPrepaidCardLoaded(@Nullable PrepaidCard prepaidCard) {
                ArrayList<TransferSource> sources = new ArrayList<>();
                if (prepaidCard != null) {
                    TransferSource sourceWrapper = new TransferSource();
                    sourceWrapper.setToken(prepaidCard.getField(TOKEN));
                    sourceWrapper.setType(PREPAID_CARD);
                    sourceWrapper.setIdentification(prepaidCard);
                    sources.add(sourceWrapper);
                    mSelectedTransferSource.postValue(sourceWrapper);
                }
                loadTransferDestination(token);
                mTransferSources.postValue(sources);
            }

            @Override
            public void onError(@NonNull Errors errors) {
                mIsLoading.postValue(Boolean.FALSE);
                mLoadTransferRequiredDataErrors.postValue(new Event<>(errors));
            }
        });
    }

    @VisibleForTesting
    void loadPrepaidCardList(@NonNull final ArrayList<TransferSource> sources) {
        mIsLoading.postValue(Boolean.TRUE);
        mPrepaidCardRepository.loadPrepaidCards(new PrepaidCardRepository.LoadPrepaidCardsCallback() {
            @Override
            public void onPrepaidCardListLoaded(@NonNull List<PrepaidCard> prepaidCardList) {
                mIsLoading.postValue(Boolean.FALSE);
                if (prepaidCardList.size() > 1) {
                    sortPrepaidCard(prepaidCardList);
                }
                for (PrepaidCard prepaidCard : prepaidCardList) {
                    TransferSource sourceWrapper = new TransferSource();
                    sourceWrapper.setToken(prepaidCard.getField(TOKEN));
                    sourceWrapper.setType(PREPAID_CARD);
                    sourceWrapper.setIdentification(prepaidCard);
                    sources.add(sourceWrapper);
                }
                if (!sources.isEmpty()) {
                    mSelectedTransferSource.postValue(sources.get(0));
                    mSourceToken = sources.get(0).getToken();
                    loadTransferDestination(mSourceToken);
                } else {
                    notifySourceUnavailable();
                }
                mTransferSources.postValue(sources);
            }

            @Override
            public void onError(@NonNull Errors errors) {
                mIsLoading.postValue(Boolean.FALSE);
                mLoadTransferRequiredDataErrors.postValue(new Event<>(errors));
            }
        });
    }

    @VisibleForTesting
    void loadTransferDestinationListWithoutPPC(@NonNull final String sourceToken) {
        mIsLoading.postValue(Boolean.TRUE);
        mTransferMethodRepository.loadTransferMethods(new TransferMethodRepository.LoadTransferMethodListCallback() {
            @Override
            public void onTransferMethodListLoaded(List<TransferMethod> transferMethods) {
                if (transferMethods != null) {
                    ListIterator<TransferMethod> transferMethod = transferMethods.listIterator();
                    while (transferMethod.hasNext()) {
                        if (Objects.equals(transferMethod.next().getField(TYPE), PREPAID_CARD)) {
                            transferMethod.remove();
                        }
                    }
                    if (transferMethods.size() > 0) {
                        mTransferDestination.postValue(transferMethods.get(0));
                        quoteAvailableTransferFunds(sourceToken, transferMethods.get(0));
                    } else {
                        mTransferDestination.setValue(null);
                    }
                }
                mIsLoading.postValue(Boolean.FALSE);
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
                mIsQuoteAvailableTransferFunds = true;
                mQuoteAvailableFunds.postValue(transfer);
            }

            @Override
            public void onError(@NonNull Errors errors) {
                mIsLoading.postValue(Boolean.FALSE);
                mQuoteAvailableFunds.setValue(null);
                if (mIsQuoteAvailableTransferFunds) {
                    if (errors.containsInputError()) {
                        Error error = errors.getErrors().get(0);
                        if (Objects.equals(error.getFieldName(), DESTINATION_TOKEN_INPUT_FIELD)) {
                            mInvalidDestinationError.postValue(new Event<>(error));
                            return;
                        }
                    }
                    mLoadTransferRequiredDataErrors.postValue(new Event<>(errors));
                }
            }
        });
    }

    @VisibleForTesting
    void loadTransferSource(@NonNull final String sourceToken) {
        if (sourceToken.startsWith(PRIVATE_TOKEN_PREFIX)) {
            loadPrepaidCard(sourceToken);
        } else {
            mIsLoading.postValue(Boolean.TRUE);
            mUserRepository.loadUser(new UserRepository.LoadUserCallback() {
                @Override
                public void onUserLoaded(@NonNull User user) {
                    mSourceToken = user.getToken();
                    ArrayList<TransferSource> sources = new ArrayList<>();
                    TransferSource sourceWrapperForAvailableFunds = new TransferSource();
                    sourceWrapperForAvailableFunds.setToken(user.getToken());
                    sourceWrapperForAvailableFunds.setType(BANK_ACCOUNT);
                    sources.add(sourceWrapperForAvailableFunds);
                    mSelectedTransferSource.postValue(sourceWrapperForAvailableFunds);
                    mTransferSources.postValue(sources);
                    loadTransferDestination(mSourceToken);
                }

                @Override
                public void onError(@NonNull Errors errors) {
                    mIsLoading.postValue(Boolean.FALSE);
                    mLoadTransferRequiredDataErrors.postValue(new Event<>(errors));
                }
            });
        }
    }

    private boolean hasTransferAmountChanged(@Nullable final Transfer transfer) {
        return mTransferAvailableFunds.getValue() && transfer != null && !TextUtils.equals(
                transfer.getDestinationAmount(), mTransferAmount.getValue());
    }

    public ProgramModel getProgramModel() {
        getHyperwallet().getConfiguration(new HyperwalletListener<Configuration>() {
            @Override
            public void onSuccess(@Nullable Configuration result) {
                if (result != null && !result.getProgramModel().equals("")) {
                    mProgramModel = ProgramModel.valueOf(result.getProgramModel());
                }
            }

            @Override
            public void onFailure(HyperwalletException exception) {
                // do nothing
            }

            @Override
            public Handler getHandler() {
                return null;
            }
        });
        return mProgramModel;
    }

    @VisibleForTesting
    Hyperwallet getHyperwallet() {
        return Hyperwallet.getDefault();
    }

    @VisibleForTesting
    void sortPrepaidCard(List<PrepaidCard> prepaidCards) {
        Collections.sort(prepaidCards, new Comparator<PrepaidCard>() {
            @Override
            public int compare(PrepaidCard firstPrepaid, PrepaidCard secondPrepaid) {
                if (firstPrepaid.getPrimaryCardToken() == null && secondPrepaid.getPrimaryCardToken() != null) {
                    return -1;
                }
                if (firstPrepaid.getPrimaryCardToken() != null && secondPrepaid.getPrimaryCardToken() == null) {
                    return 1;
                }
                return 0;
            }
        });
    }

    public static class CreateTransferViewModelFactory implements ViewModelProvider.Factory {

        private final TransferRepository transferRepository;
        private final TransferMethodRepository transferMethodRepository;
        private final UserRepository userRepository;
        private final String sourceToken;
        private final PrepaidCardRepository prepaidCardRepository;

        public CreateTransferViewModelFactory(@NonNull final String sourceToken,
                @NonNull final TransferRepository transferRepository,
                @NonNull final TransferMethodRepository transferMethodRepository,
                @NonNull final UserRepository userRepository,
                @NonNull final PrepaidCardRepository prepaidCardRepository) {
            this.sourceToken = sourceToken;
            this.transferMethodRepository = transferMethodRepository;
            this.transferRepository = transferRepository;
            this.userRepository = userRepository;
            this.prepaidCardRepository = prepaidCardRepository;
        }

        public CreateTransferViewModelFactory(@NonNull final TransferRepository transferRepository,
                @NonNull final TransferMethodRepository transferMethodRepository,
                @NonNull final UserRepository userRepository,
                @NonNull final PrepaidCardRepository prepaidCardRepository) {
            this.sourceToken = null;
            this.transferMethodRepository = transferMethodRepository;
            this.transferRepository = transferRepository;
            this.userRepository = userRepository;
            this.prepaidCardRepository = prepaidCardRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CreateTransferViewModel.class)) {
                if (TextUtils.isEmpty(sourceToken)) {
                    return (T) new CreateTransferViewModel(transferRepository, transferMethodRepository,
                            userRepository, prepaidCardRepository);
                }
                return (T) new CreateTransferViewModel(sourceToken, transferRepository, transferMethodRepository,
                        userRepository, prepaidCardRepository);
            }

            throw new IllegalArgumentException("Expecting ViewModel class: " + CreateTransferViewModel.class.getName());
        }
    }
}
