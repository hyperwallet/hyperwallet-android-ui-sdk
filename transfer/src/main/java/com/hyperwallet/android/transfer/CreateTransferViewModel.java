package com.hyperwallet.android.transfer;

import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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

    private MutableLiveData<Event<HyperwalletErrors>> mLoadTransferRequiredDataErrors = new MutableLiveData<>();
    private MutableLiveData<Event<HyperwalletErrors>> mCreateTransferErrors = new MutableLiveData<>();

    private MutableLiveData<Event<HyperwalletError>> mInvalidAmountError = new MutableLiveData<>();
    private MutableLiveData<Event<HyperwalletError>> mInvalidDestinationError = new MutableLiveData<>();


    private String mSourceToken;
    private String mAmount;
    private String mNotes;

    //todo pass in the user repository and transfer method repository
    public CreateTransferViewModel(@Nullable final String sourceToken,
            @NonNull final TransferRepository transferRepository) {
        mTransferRepository = transferRepository;
        mSourceToken = sourceToken;
        loadTransferDestination(mSourceToken);
    }

    //todo pass in the user repository and transfer method repository
    public CreateTransferViewModel(@NonNull final TransferRepository transferRepository) {
        mTransferRepository = transferRepository;
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

    //this could be private - we could expose a method called retry and make the decision in the viewmodel
    public boolean hasTransferRequiredData() {
        return TextUtils.isEmpty(mSourceToken) || mTransferDestination.getValue() == null //if we agree that we will create an empty transfer method as destination
                || mQuoteAvailableFunds.getValue() == null //if we agree that we will create an empty transfer with zero amount
                || !(isQuoteAvailableFundsUpToDate());

    }

    //this could be private - we could expose a method called retry and make the decision in the viewmodel
    public void retryLoadTransferRequiredData() {
        if (TextUtils.isEmpty(mSourceToken)) {
            loadTransferSource();
        } else if (mTransferDestination.getValue() == null) {
            loadTransferDestination(mSourceToken);
        } else {
            quoteTransferAvailableFunds(mSourceToken, mTransferDestination.getValue());
        }
    }

    //this could be private - we could expose a method called retry and make the decision in the viewmodel
    public void retryCrateTransfer() {
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


    private boolean isQuoteAvailableFundsUpToDate() {
        return mQuoteAvailableFunds.getValue().getSourceToken().equals(mSourceToken)
                && mQuoteAvailableFunds.getValue().getDestinationToken().equals(
                mTransferDestination.getValue().getField(HyperwalletTransferMethod.TransferMethodFields.TOKEN));
    }

    private void loadTransferSource() {
        Hyperwallet.getDefault().getUser(new HyperwalletListener<HyperwalletUser>() {
            @Override
            public void onSuccess(@Nullable final HyperwalletUser result) {
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

                        if (result == null || result.getDataList() == null || result.getDataList().isEmpty()) { //could not find transfer destination
                            mTransferDestination.postValue(new HyperwalletTransferMethod());
                            mQuoteAvailableFunds.postValue(new Transfer.Builder().build());
                        } else {
                            HyperwalletTransferMethod transferMethod = result.getDataList().get(0);
                            mTransferDestination.postValue(transferMethod);
                            quoteTransferAvailableFunds(sourceToken, transferMethod);
                        }

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
