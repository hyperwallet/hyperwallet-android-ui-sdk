package com.hyperwallet.android.transfer;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

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

import java.util.UUID;

public class CreateTransferViewModel extends ViewModel {

    private TransferRepository mTransferRepository;
    private Transfer mTransfer;

    private MutableLiveData<HyperwalletUser> mUser = new MutableLiveData<>();
    private MutableLiveData<HyperwalletTransferMethod> mTransferMethod = new MutableLiveData<>();



    private Observer<HyperwalletUser> mUserObserver = new Observer<HyperwalletUser>() {
        @Override
        public void onChanged(HyperwalletUser user) {

        }
    };


    public CreateTransferViewModel(@NonNull final TransferRepository transferRepository) {
        mTransferRepository = transferRepository;
        mUser.observeForever(mUserObserver);
        loadUser();
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        mUser.removeObserver(mUserObserver);
    }

    private void loadUser() {
        if (mUser.getValue() == null) {
            //todo replace with user repository
            Hyperwallet.getDefault().getUser(new HyperwalletListener<HyperwalletUser>() {
                @Override
                public void onSuccess(@Nullable HyperwalletUser result) {
                    mUser.postValue(result);
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


    public void setTransferDestination(@NonNull final HyperwalletTransferMethod transferDestination) {
        if (mUser.getValue() != null) {
            quoteTransferAllFunds(mUser.getValue(), transferDestination);
        } else {
            loadUser();
        }

    }


    private void loadTransferDestination() {

        //todo replace with transfer method repository and possibly expose a new method there...
        HyperwalletTransferMethodQueryParam queryParam = new HyperwalletTransferMethodQueryParam.Builder()
                .limit(1)
                .status(HyperwalletStatusTransition.StatusDefinition.ACTIVATED)
                .build();

        Hyperwallet.getDefault().listTransferMethods(queryParam,
                new HyperwalletListener<HyperwalletPageList<HyperwalletTransferMethod>>() {
                    @Override
                    public void onSuccess(@Nullable HyperwalletPageList<HyperwalletTransferMethod> result) {
                        mTransferMethod.setValue(result.getDataList().get(0));
                        quoteTransferAllFunds(mUser.getValue(), mTransferMethod.getValue());
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
                mTransfer = transfer;
            }

            @Override
            public void onError(HyperwalletErrors errors) {

            }
        });
    }

}
