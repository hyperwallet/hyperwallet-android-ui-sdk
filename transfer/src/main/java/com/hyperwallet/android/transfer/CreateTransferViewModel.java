package com.hyperwallet.android.transfer;

import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.HyperwalletStatusTransition;
import com.hyperwallet.android.model.paging.HyperwalletPageList;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethodQueryParam;
import com.hyperwallet.android.model.user.HyperwalletUser;
import com.hyperwallet.android.ui.common.viewmodel.Event;

import java.util.UUID;

public class CreateTransferViewModel extends ViewModel {

    private TransferRepository mTransferRepository;

    private MutableLiveData<HyperwalletTransferMethod> mTransferDestination = new MutableLiveData<>();
    private MutableLiveData<Boolean> mTransferAllAvailableFunds = new MutableLiveData<>();
    private MutableLiveData<Transfer> mQuoteAvailableFunds = new MutableLiveData<>();
    private MutableLiveData<Event<Transfer>> mDetailNavigation = new MutableLiveData<>();

    /*
        The main reason to have 4 live data objects, is because I'd like to keep the fragment with no logic. Since the
        destinationToken field isn't a regular input field, like destinationAmount (TextView),
        we cannot create a default logic to highlight it (waiting or requirements - I might be wrong on that). So we
        end up with one live data for general errors during ui initialization (mLoadTransferRequiredDataErrors - we display the pop up)
        then mCreateTransferErrors any error (network or unexpected errors - pop up) and one live data for each field
        we will highlight so the fragment can create a different highlighting logic for each case
      */
    private MutableLiveData<Event<HyperwalletErrors>> mCreateTransferErrors = new MutableLiveData<>();
    private MutableLiveData<Event<HyperwalletErrors>> mLoadTransferRequiredDataErrors = new MutableLiveData<>();
    private MutableLiveData<Event<HyperwalletError>> mInvalidAmountError = new MutableLiveData<>();
    private MutableLiveData<Event<HyperwalletError>> mInvalidDestinationError = new MutableLiveData<>();


    private String mSourceToken;

    private String mAmount; //needed for retry case
    private String mNotes; //needed for retry case
    private State mState;  //needed for retry cases


    //todo pass in the user repository and transfer method repository
    public CreateTransferViewModel(@Nullable final String sourceToken,
            @NonNull final TransferRepository transferRepository) {
        mTransferRepository = transferRepository;
        mSourceToken = sourceToken;
        mState = new State(State.TRANSFER_DESTINATION_KNOWN);
        loadTransferDestination(mSourceToken);
    }

    //todo pass in the user repository and transfer method repository
    public CreateTransferViewModel(@NonNull final TransferRepository transferRepository) {
        mTransferRepository = transferRepository;
        mState = new State();
        loadTransferSource();
    }


    public void createTransfer(@Nullable final String amount, @Nullable final String notes) {
        mAmount = amount;
        mNotes = notes;
        Transfer transfer = new Transfer.Builder()
                .clientTransferID("mbl-" + UUID.randomUUID().toString())
                .sourceToken(mSourceToken)
                .destinationToken(
                        mTransferDestination.getValue().getField(HyperwalletTransferMethod.TransferMethodFields.TOKEN))
                .destinationCurrency(mTransferDestination.getValue().getField(
                        HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY))
                .destinationAmount(mAmount)
                .notes(mNotes)
                .build();
        mTransferRepository.createTransfer(transfer, new TransferRepository.CreateTransferCallback() {
            @Override
            public void onTransferCreated(Transfer transfer) {
                mDetailNavigation.postValue(new Event<>(transfer));
            }

            @Override
            public void onError(HyperwalletErrors errors) {
                handleCreateTransferError(errors);
            }
        });
    }


    //todo - it will be used when user selects another transfer destination
    public void setTransferDestination(@NonNull final HyperwalletTransferMethod transferDestination) {
        mTransferDestination.postValue(transferDestination);
        if (TextUtils.isEmpty(mSourceToken)) {
            quoteTransferAvailableFunds(transferDestination);
        } else {
            quoteTransferAvailableFunds(mSourceToken, transferDestination);
        }
    }

    //we expose the method and let the object (view model) to decide what to do based on its state
    public void retry() {
        if (mState.isInitial()) {
            loadTransferSource();
        } else if (mState.isTransferSourceKnown()) {
            loadTransferDestination(mSourceToken);
        } else if (mState.isTransferDestinationKnown()) {
            quoteTransferAvailableFunds(mSourceToken, mTransferDestination.getValue());
        } else if (mState.isTransferMaxAmountKnown()){
            retryCrateTransfer();
        }
    }


    //this could be private - we could expose a method called retry and make the decision in the viewmodel
    private void retryCrateTransfer() {
        createTransfer(mAmount, mNotes);
    }


    public void setTransferAllAvailableFunds(final boolean transferAllAvailableFunds) {
        mTransferAllAvailableFunds.postValue(transferAllAvailableFunds);
    }


    public LiveData<HyperwalletTransferMethod> getTransferDestination() {
        return mTransferDestination;
    }


    public LiveData<Boolean> getTransferAllAvailableFunds() {
        return mTransferAllAvailableFunds;
    }


    public LiveData<Transfer> getQuoteAvailableFunds() {
        return mQuoteAvailableFunds;
    }


    public MutableLiveData<Event<HyperwalletErrors>> getCreateTransferErrors() {
        return mCreateTransferErrors;
    }

    public MutableLiveData<Event<HyperwalletErrors>> getLoadTransferRequiredDataErrors() {
        return mLoadTransferRequiredDataErrors;
    }


    public LiveData<Event<Transfer>> getDetailNavigation() {
        return mDetailNavigation;
    }


    private boolean isQuoteAvailableFundsUpToDate() { //transfer case when the user hits next and a transfer could not be created. so I know that I need to retry the transfer
        return mQuoteAvailableFunds.getValue().getSourceToken().equals(mSourceToken)
                && mQuoteAvailableFunds.getValue().getDestinationToken().equals(
                mTransferDestination.getValue().getField(HyperwalletTransferMethod.TransferMethodFields.TOKEN));
    }

    private void loadTransferSource() {
        Hyperwallet.getDefault().getUser(new HyperwalletListener<HyperwalletUser>() {
            @Override
            public void onSuccess(@Nullable final HyperwalletUser result) {
                mState.next();
                mSourceToken = result.getToken();
                loadTransferDestination(mSourceToken);

            }

            @Override
            public void onFailure(HyperwalletException exception) {
                mLoadTransferRequiredDataErrors.postValue(new Event<>(exception.getHyperwalletErrors()));
            }

            @Override
            public Handler getHandler() {
                return null;
            }
        });
    }


    private void loadTransferDestination(@NonNull final String sourceToken) {

        //todo replace with transfer method repository and possibly expose a new method there...
        HyperwalletTransferMethodQueryParam queryParam = new HyperwalletTransferMethodQueryParam.Builder()
                .limit(1)
                .status(HyperwalletStatusTransition.StatusDefinition.ACTIVATED)
                .build();

        Hyperwallet.getDefault().listTransferMethods(queryParam,
                new HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletPageList<HyperwalletTransferMethod> result) {
                        mState.next();
                        HyperwalletTransferMethod transferMethod = result.getDataList().get(0);
                        mTransferDestination.postValue(transferMethod);
                        quoteTransferAvailableFunds(sourceToken, transferMethod);
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        mLoadTransferRequiredDataErrors.postValue(new Event<>(exception.getHyperwalletErrors()));
                    }

                    @Override
                    public Handler getHandler() {
                        return null;
                    }
                });

    }

    private void quoteTransferAvailableFunds(@NonNull final HyperwalletTransferMethod transferDestination) {
        //todo replace with user repository
        Hyperwallet.getDefault().getUser(new HyperwalletListener<HyperwalletUser>() {
            @Override
            public void onSuccess(@Nullable HyperwalletUser result) {
                mSourceToken = result.getToken();
                quoteTransferAvailableFunds(mSourceToken, transferDestination);
                mState.next();
            }

            @Override
            public void onFailure(HyperwalletException exception) {
                mLoadTransferRequiredDataErrors.postValue(new Event<>(exception.getHyperwalletErrors()));
            }

            @Override
            public Handler getHandler() {
                return null;
            }
        });

    }

    private void quoteTransferAvailableFunds(@NonNull String sourceToken,
            @NonNull final HyperwalletTransferMethod transferMethod) {

        Transfer transfer = new Transfer.Builder()
                .clientTransferID("mbl-" + UUID.randomUUID().toString())
                .sourceToken(sourceToken)
                .destinationToken(transferMethod.getField(HyperwalletTransferMethod.TransferMethodFields.TOKEN))
                .destinationCurrency(transferMethod.getField(
                        HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY))
                .build();


        mTransferRepository.createTransfer(transfer, new TransferRepository.CreateTransferCallback() {
            @Override
            public void onTransferCreated(Transfer transfer) {
                mState.next();
                mQuoteAvailableFunds.postValue(transfer);
            }

            @Override
            public void onError(HyperwalletErrors errors) {
                mLoadTransferRequiredDataErrors.postValue(new Event<>(errors));
            }
        });
    }


    private void handleCreateTransferError(@NonNull final HyperwalletErrors errors) {
        if (errors.containsInputError()) {
            HyperwalletError error = errors.getErrors().get(0);
            if (error.getFieldName().equals("destinationAmount")) {
                mInvalidAmountError.postValue(new Event<>(error));
            } else if (error.getFieldName().equals("destinationToken")) {
                mInvalidDestinationError.postValue(new Event<>(error));
            } else {
                mCreateTransferErrors.postValue(new Event<>(errors));
            }
        } else {
            mCreateTransferErrors.postValue(new Event<>(errors));
        }
    }


    private static class State {

        private static final int INITIAL = 0;
        private static final int TRANSFER_SOURCE_KNOWN = 1;
        private static final int TRANSFER_DESTINATION_KNOWN = 2;
        private static final int TRANSFER_MAX_AMOUNT_KNOWN = 3;

        private int mState;

        private State(final int initialState) {
            this();
            if (initialState < 0 || initialState > 1) {
                throw new IllegalArgumentException("initial state invalid");
            }
            mState = initialState;
        }

        private State() {
            mState = INITIAL;
        }

        private void next() {
            switch (mState) {
                case INITIAL:
                    mState = TRANSFER_SOURCE_KNOWN;
                    break;
                case TRANSFER_SOURCE_KNOWN:
                    mState = TRANSFER_DESTINATION_KNOWN;
                    break;
                case TRANSFER_DESTINATION_KNOWN:
                    mState = TRANSFER_MAX_AMOUNT_KNOWN;
                    break;
            }
        }

        private boolean isInitial() {
            return mState == INITIAL;
        }

        private boolean isTransferSourceKnown() {
            return mState == TRANSFER_SOURCE_KNOWN;
        }

        private boolean isTransferDestinationKnown() {
            return mState == TRANSFER_DESTINATION_KNOWN;
        }

        private boolean isTransferMaxAmountKnown() {
            return mState == TRANSFER_MAX_AMOUNT_KNOWN;
        }
    }


    public static class CreateTransferViewModelFactory implements ViewModelProvider.Factory {

        private TransferRepository mTransferRepository;
        private String mSourceToken;

        //todo pass in the user repository and transfer method repository
        public CreateTransferViewModelFactory(@Nullable String sourceToken,
                @NonNull final TransferRepository transferRepository) {
            mTransferRepository = transferRepository;
            mSourceToken = sourceToken;
        }

        public CreateTransferViewModelFactory(@NonNull final TransferRepository transferRepository) {
            mTransferRepository = transferRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (TextUtils.isEmpty(mSourceToken)) {
                return (T) new CreateTransferViewModel(mTransferRepository);
            } else {
                return (T) new CreateTransferViewModel(mSourceToken, mTransferRepository);
            }
        }
    }


}
