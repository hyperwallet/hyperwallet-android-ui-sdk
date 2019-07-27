package com.hyperwallet.android.transfer;

import androidx.annotation.NonNull;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.transfer.Transfer;

public interface TransferRepository {

    void createTransfer(@NonNull final Transfer transfer, @NonNull final CreateTransferCallback callback);

    void scheduleTransfer(@NonNull final Transfer transfer,
            @NonNull final ScheduleTransferCallback callback);


    interface CreateTransferCallback {

        void onTransferCreated(Transfer transfer);

        void onError(HyperwalletErrors errors);
    }


    interface ScheduleTransferCallback {

        void onTransferScheduled(StatusTransition statusTransition);

        void onError(HyperwalletErrors errors);
    }

}
