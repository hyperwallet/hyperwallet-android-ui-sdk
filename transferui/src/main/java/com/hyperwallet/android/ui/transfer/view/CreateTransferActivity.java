/*
 * The MIT License (MIT)
 * Copyright (c) 2019 Hyperwallet Systems Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.hyperwallet.android.ui.transfer.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.common.view.error.DefaultErrorDialogFragment;
import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.repository.TransferRepositoryFactory;
import com.hyperwallet.android.ui.transfer.viewmodel.CreateTransferViewModel;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.user.repository.UserRepositoryFactory;

import java.util.List;

/**
 * Create Transfer Activity
 */
public class CreateTransferActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSFER_SOURCE_TOKEN = "TRANSFER_SOURCE_TOKEN";

    private CreateTransferViewModel mCreateTransferViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_transfer);

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

        // initialize view model factory based from intent parameters
        String sourceToken = getIntent().getStringExtra(EXTRA_TRANSFER_SOURCE_TOKEN);

        final CreateTransferViewModel.CreateTransferViewModelFactory factory;
        if (TextUtils.isEmpty(sourceToken)) {
            factory = new CreateTransferViewModel.CreateTransferViewModelFactory(
                    TransferRepositoryFactory.getInstance().getTransferRepository(),
                    TransferMethodRepositoryFactory.getInstance().getTransferMethodRepository(),
                    UserRepositoryFactory.getInstance().getUserRepository());
        } else {
            factory = new CreateTransferViewModel.CreateTransferViewModelFactory(sourceToken,
                    TransferRepositoryFactory.getInstance().getTransferRepository(),
                    TransferMethodRepositoryFactory.getInstance().getTransferMethodRepository(),
                    UserRepositoryFactory.getInstance().getUserRepository());
        }

        mCreateTransferViewModel = ViewModelProviders.of(this, factory).get(CreateTransferViewModel.class);
        mCreateTransferViewModel.getLoadErrorEvent().observe(this, new Observer<Event<HyperwalletErrors>>() {
            @Override
            public void onChanged(Event<HyperwalletErrors> event) {
                if (event != null && !event.isContentConsumed()) {
                    showErrorOnLoadCreateTransfer(event.getContent().getErrors());
                }
            }
        });

        //TODO temporarily display dialog on all quote transfer error
        mCreateTransferViewModel.getQuoteErrors().observe(this, new Observer<Event<HyperwalletErrors>>() {
            @Override
            public void onChanged(Event<HyperwalletErrors> event) {
                if (event != null && !event.isContentConsumed()) {
                    showErrorOnLoadCreateTransfer(event.getContent().getErrors());
                }
            }
        });

        if (savedInstanceState == null) {
            initFragment(CreateTransferFragment.newInstance());
        }
    }

    @Override
    protected void onDestroy() {
        TransferRepositoryFactory.clearInstance();
        TransferMethodRepositoryFactory.clearInstance();
        UserRepositoryFactory.clearInstance();
        super.onDestroy();
    }

    private void initFragment(@NonNull final Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.create_transfer_fragment, fragment);
        fragmentTransaction.commit();
    }


    private void showErrorOnLoadCreateTransfer(@NonNull final List<HyperwalletError> errors) {
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
