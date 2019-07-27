package com.hyperwallet.android.transfer;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.ui.common.viewmodel.Event;

public class ScheduleTransferViewModel extends ViewModel {

    private Transfer mTransfer;
    private TransferRepository mTransferRepository;
    private MutableLiveData<Event<StatusTransition>> mTransferStatusTransition = new MutableLiveData<>();
    private MutableLiveData<Event<HyperwalletErrors>> mTransferStatusTransitionErrors = new MutableLiveData<>();

    public ScheduleTransferViewModel(@NonNull final TransferRepository transferRepository) {
        mTransferRepository = transferRepository;
    }

    public void setTransfer(@NonNull final Transfer transfer) {
        mTransfer = transfer;
    }

    public Transfer getTransfer() {
        return mTransfer;
    }


    public void scheduleTransfer() {
        mTransferRepository.scheduleTransfer(mTransfer, new TransferRepository.ScheduleTransferCallback() {
            @Override
            public void onTransferScheduled(StatusTransition statusTransition) {
                mTransferStatusTransition.postValue(new Event<>(statusTransition));
            }

            @Override
            public void onError(HyperwalletErrors errors) {
                mTransferStatusTransitionErrors.postValue(new Event<>(errors));
            }
        });
    }

    public MutableLiveData<Event<HyperwalletErrors>> getTransferStatusTransitionErrors() {
        return mTransferStatusTransitionErrors;
    }

    public MutableLiveData<Event<StatusTransition>> getTransferStatusTransition() {
        return mTransferStatusTransition;
    }

    public static class ScheduleTransferViewModelFactory implements ViewModelProvider.Factory {

        private TransferRepository mTransferRepository;

        public ScheduleTransferViewModelFactory(@NonNull final TransferRepository transferRepository) {
            mTransferRepository = transferRepository;
        }


        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ScheduleTransferViewModel(mTransferRepository);
        }
    }
}
