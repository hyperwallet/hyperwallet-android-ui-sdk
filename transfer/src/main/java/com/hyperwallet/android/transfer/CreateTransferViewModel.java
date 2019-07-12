package com.hyperwallet.android.transfer;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
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
    private HyperwalletUser mUser;

    private MutableLiveData<HyperwalletTransferMethod> mTransferDestination = new MutableLiveData<>();
    private MutableLiveData<Boolean> mTransferAvailableFunds = new MutableLiveData<>();
    private MutableLiveData<Transfer> mQuoteAvailableFunds = new MutableLiveData<>();
    private MutableLiveData<Event<Transfer>> mDetailNavigation = new MutableLiveData<>();


    //todo pass in the user repository and transfer method repository
    public CreateTransferViewModel(@NonNull final TransferRepository transferRepository) {
        mTransferRepository = transferRepository;
        loadTransferSource();
    }


    public void createTransfer(String amount, String notes) {
        Transfer transfer = new Transfer.Builder()
                .clientTransferID("mbl-"+UUID.randomUUID().toString())
                .sourceToken(mUser.getToken())
                .destinationToken(mTransferDestination.getValue().getField(HyperwalletTransferMethod.TransferMethodFields.TOKEN))
                .destinationCurrency(mTransferDestination.getValue().getField(HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY))
                .destinationAmount(amount)
                .notes(notes)
                .build();
        mTransferRepository.createTransfer(transfer, new TransferRepository.CreateTransferCallback() {
            @Override
            public void onTransferCreated(Transfer transfer) {
                mDetailNavigation.postValue(new Event<>(transfer));
            }

            @Override
            public void onError(HyperwalletErrors errors) {
                System.out.println("errors!!!");
            }
        });
    }


    //todo - it will be used when user selects another transfer destination
    public void setTransferDestination(@NonNull final HyperwalletTransferMethod transferDestination) {
        if (mUser != null) {
            quoteTransferAvailableFunds(transferDestination, mUser);
        } else {
            quoteTransferAvailableFunds(transferDestination);
        }
    }


    public void setTransferAvailableFunds(final boolean transferAvailableFunds) {
        mTransferAvailableFunds.postValue(transferAvailableFunds);
    }


    public LiveData<HyperwalletTransferMethod> getTransferDestination() {
        return mTransferDestination;
    }


    public LiveData<Boolean> getTransferAvailableFunds() {
        return mTransferAvailableFunds;
    }


    public LiveData<Transfer> getQuoteAvailableFunds() {
        return mQuoteAvailableFunds;
    }


    public LiveData<Event<Transfer>> getDetailNavigation() {
        return mDetailNavigation;
    }


    private void loadTransferSource() {
        if (mUser == null) {
            //todo replace with user repository
            Hyperwallet.getDefault().getUser(new HyperwalletListener<HyperwalletUser>() {
                @Override
                public void onSuccess(@Nullable final HyperwalletUser result) {
                    mUser = result;
                    loadTransferDestination(mUser);
                }

                @Override
                public void onFailure(HyperwalletException exception) {

                }

                @Override
                public Handler getHandler() {
                    return null;
                }
            });
        }
    }


    private void loadTransferDestination(@NonNull final HyperwalletUser user) {

        //todo replace with transfer method repository and possibly expose a new method there...
        HyperwalletTransferMethodQueryParam queryParam = new HyperwalletTransferMethodQueryParam.Builder()
                .limit(1)
                .status(HyperwalletStatusTransition.StatusDefinition.ACTIVATED)
                .build();

        Hyperwallet.getDefault().listTransferMethods(queryParam,
                new HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletPageList<HyperwalletTransferMethod> result) {
                        HyperwalletTransferMethod transferMethod = result.getDataList().get(0);
                        mTransferDestination.postValue(transferMethod);
                        quoteTransferAvailableFunds(transferMethod, user);
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {

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
                mUser = result;
                quoteTransferAvailableFunds(transferDestination, result);
            }

            @Override
            public void onFailure(HyperwalletException exception) {

            }

            @Override
            public Handler getHandler() {
                return null;
            }
        });

    }

    private void quoteTransferAvailableFunds(@NonNull final HyperwalletTransferMethod transferMethod,
            @NonNull final HyperwalletUser user) {

        Transfer transfer = new Transfer.Builder()
                .clientTransferID("mbl-"+UUID.randomUUID().toString())
                .sourceToken(user.getToken())
                .destinationToken(transferMethod.getField(HyperwalletTransferMethod.TransferMethodFields.TOKEN))
                .destinationCurrency(transferMethod.getField(HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY))
                .build();

        mTransferRepository.createTransfer(transfer, new TransferRepository.CreateTransferCallback() {
            @Override
            public void onTransferCreated(Transfer transfer) {
                mQuoteAvailableFunds.postValue(transfer);
            }

            @Override
            public void onError(HyperwalletErrors errors) {
                System.out.println("errors!!!");
            }
        });
    }


    public static class CreateTransferViewModelFactory implements ViewModelProvider.Factory {

        private TransferRepository mTransferRepository;

        //todo pass in the user repository and transfer method repository
        public CreateTransferViewModelFactory(@NonNull final TransferRepository transferRepository) {
            mTransferRepository = transferRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CreateTransferViewModel(mTransferRepository);
        }
    }


}
