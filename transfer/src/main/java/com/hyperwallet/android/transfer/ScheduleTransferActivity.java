package com.hyperwallet.android.transfer;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.ui.common.view.error.DefaultErrorDialogFragment;
import com.hyperwallet.android.ui.common.view.error.OnNetworkErrorCallback;
import com.hyperwallet.android.ui.common.viewmodel.Event;

import java.util.List;

public class ScheduleTransferActivity extends AppCompatActivity implements OnNetworkErrorCallback {

    private ScheduleTransferViewModel mScheduleTransferViewModel;
    public static final String TRANSFER_EXTRA = "TRANSFER";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_transfer);

        TransferRepository transferRepository = TransferRepositoryFactory.getInstance().getTransferRepository();
        mScheduleTransferViewModel = ViewModelProviders.of(this,
                new ScheduleTransferViewModel.ScheduleTransferViewModelFactory(transferRepository)).get(
                ScheduleTransferViewModel.class);

        Transfer transfer = getIntent().getParcelableExtra(TRANSFER_EXTRA);
        mScheduleTransferViewModel.setTransfer(transfer);

        registerObservers();
        if (savedInstanceState == null) {
            initFragment(ScheduleTransferFragment.newInstance());
        }

    }


    @Override
    public void retry() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ScheduleTransferFragment fragment = (ScheduleTransferFragment)
                fragmentManager.findFragmentById(R.id.schedule_transfer_fragment);

        if (fragment == null) {
            fragment = ScheduleTransferFragment.newInstance();
        }
        fragment.retry();
    }

    private void initFragment(@NonNull final Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.schedule_transfer_fragment, fragment);
        fragmentTransaction.commit();
    }


    private void registerObservers() {
        mScheduleTransferViewModel.getTransferStatusTransitionErrors().observe(this,
                new Observer<Event<HyperwalletErrors>>() {
                    @Override
                    public void onChanged(Event<HyperwalletErrors> hyperwalletErrorsEvent) {
                        if (!hyperwalletErrorsEvent.isContentConsumed()) {
                            showScheduleTransferError(hyperwalletErrorsEvent.getContent().getErrors());
                        }

                    }
                });
    }

    private void showScheduleTransferError(@NonNull final List<HyperwalletError> errors) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DefaultErrorDialogFragment fragment = (DefaultErrorDialogFragment)
                fragmentManager.findFragmentByTag(DefaultErrorDialogFragment.TAG);

        if (fragment == null) {
            fragment = DefaultErrorDialogFragment.newInstance(errors);
        }

        if (!fragment.isAdded()) {
            fragment.show(fragmentManager);
        }
    }
}
