package com.hyperwallet.android.transfer;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.ui.common.view.error.DefaultErrorDialogFragment;
import com.hyperwallet.android.ui.common.view.error.OnNetworkErrorCallback;
import com.hyperwallet.android.ui.common.viewmodel.Event;

import java.util.List;

public class CreateTransferActivity extends AppCompatActivity implements OnNetworkErrorCallback {

    private CreateTransferViewModel mCreateTransferViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_transfer);

        TransferRepository transferRepository = TransferRepositoryFactory.getInstance().getTransferRepository();
        CreateTransferViewModel.CreateTransferViewModelFactory factory =
                new CreateTransferViewModel.CreateTransferViewModelFactory(transferRepository);
        mCreateTransferViewModel = ViewModelProviders.of(this, factory).get(CreateTransferViewModel.class);
        registerObservers();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_create_transfer);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (savedInstanceState == null) {
            initFragment(CreateTransferFragment.newInstance());
        }
    }

    private void initFragment(@NonNull final Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.create_transfer_fragment, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void retry() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        CreateTransferFragment fragment = (CreateTransferFragment)
                fragmentManager.findFragmentById(R.id.create_transfer_fragment);

        if (fragment == null) {
            fragment = CreateTransferFragment.newInstance();
        }
        fragment.retry();
    }


    private void registerObservers() {
        mCreateTransferViewModel.getTransferInitializationErrors().observe(this,
                new Observer<Event<HyperwalletErrors>>() {
                    @Override
                    public void onChanged(Event<HyperwalletErrors> hyperwalletErrorsEvent) {
                        if (!hyperwalletErrorsEvent.isContentConsumed()) {
                            showTransferError(hyperwalletErrorsEvent.getContent().getErrors());
                        }
                    }
                });

        mCreateTransferViewModel.getCreateTransferErrors().observe(this, new Observer<Event<HyperwalletErrors>>() {
            @Override
            public void onChanged(Event<HyperwalletErrors> hyperwalletErrorsEvent) {
                if (!hyperwalletErrorsEvent.isContentConsumed()) {
                    showTransferError(hyperwalletErrorsEvent.getContent().getErrors());
                }
            }
        });

    }

    private void showTransferError(@NonNull final List<HyperwalletError> errors) {
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
