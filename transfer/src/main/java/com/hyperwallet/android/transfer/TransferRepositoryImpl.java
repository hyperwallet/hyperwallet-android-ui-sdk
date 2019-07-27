package com.hyperwallet.android.transfer;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.transfer.Transfer;

public class TransferRepositoryImpl implements TransferRepository {


    public void createTransfer(@NonNull final Transfer transfer, @NonNull final CreateTransferCallback callback) {
        Hyperwallet.getDefault().createTransfer(transfer, new HyperwalletListener<Transfer>() {
            @Override
            public void onSuccess(@Nullable Transfer result) {
                callback.onTransferCreated(result);
            }

            @Override
            public void onFailure(HyperwalletException exception) {
                callback.onError(exception.getHyperwalletErrors());
            }

            @Override
            public Handler getHandler() {
                return null;
            }
        });
    }


    public void scheduleTransfer(@NonNull final Transfer transfer,
            @NonNull final ScheduleTransferCallback callback) {


        Hyperwallet.getDefault().scheduleTransfer(transfer.getToken(), null,
                new HyperwalletListener<StatusTransition>() {
                    @Override
                    public void onSuccess(@Nullable StatusTransition result) {
                        callback.onTransferScheduled(result);
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        callback.onError(exception.getHyperwalletErrors());
                    }

                    @Override
                    public Handler getHandler() {
                        return null;
                    }
                });
    }



}
