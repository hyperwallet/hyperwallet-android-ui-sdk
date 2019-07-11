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
    private MutableLiveData<Boolean> mTransferAllFunds = new MutableLiveData<>();
    private MutableLiveData<Transfer> mTransfer = new MutableLiveData<>();
    private MutableLiveData<Event<Transfer>> mDetailNavigation = new MutableLiveData<>();


    //todo pass in the user repository and transfer method repository
    public CreateTransferViewModel(@NonNull final TransferRepository transferRepository) {
        mTransferRepository = transferRepository;
        initialize();
    }


    public void createTransfer(String amount) {
        Transfer transfer = new Transfer.Builder()
                .clientTransferID("mbl-"+UUID.randomUUID().toString())
                .sourceToken(mUser.getToken())
                .destinationToken(mTransferDestination.getValue().getField(HyperwalletTransferMethod.TransferMethodFields.TOKEN))
                .destinationCurrency(mTransferDestination.getValue().getField(HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY))
                .destinationAmount(amount)
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
            quoteTransferAllFunds(mUser, transferDestination);
        } else {
            quoteTransferAllFunds(transferDestination);
        }
    }


    public void setTransferAllFunds(final boolean transferAllFunds) {
        mTransferAllFunds.postValue(transferAllFunds);
    }


    public LiveData<HyperwalletTransferMethod> getTransferDestination() {
        return mTransferDestination;
    }


    public MutableLiveData<Boolean> getTransferAllFunds() {
        return mTransferAllFunds;
    }


    public LiveData<Transfer> getTransfer() {
        return mTransfer;
    }


    public MutableLiveData<Event<Transfer>> getDetailNavigation() {
        return mDetailNavigation;
    }


    private void initialize() {
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


    private void quoteTransferAllFunds(@NonNull final HyperwalletTransferMethod transferDestination) {
        //todo replace with user repository
        Hyperwallet.getDefault().getUser(new HyperwalletListener<HyperwalletUser>() {
            @Override
            public void onSuccess(@Nullable HyperwalletUser result) {
                mUser = result;
                quoteTransferAllFunds(result, transferDestination);
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
                        quoteTransferAllFunds(user, transferMethod);
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


    private void quoteTransferAllFunds(@NonNull final HyperwalletUser user,
            @NonNull final HyperwalletTransferMethod transferMethod) {

        Transfer transfer = new Transfer.Builder()
                .clientTransferID("mbl-"+UUID.randomUUID().toString())
                .sourceToken(user.getToken())
                .destinationToken(transferMethod.getField(HyperwalletTransferMethod.TransferMethodFields.TOKEN))
                .destinationCurrency(transferMethod.getField(HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY))
                .build();

        mTransferRepository.createTransfer(transfer, new TransferRepository.CreateTransferCallback() {
            @Override
            public void onTransferCreated(Transfer transfer) {
                mTransfer.postValue(transfer);
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
