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

import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.SCHEDULE_TRANSFER_REQUEST_CODE;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.common.util.PageGroups;
import com.hyperwallet.android.ui.common.view.ActivityUtils;
import com.hyperwallet.android.ui.common.view.error.OnNetworkErrorCallback;
import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.repository.TransferRepositoryFactory;
import com.hyperwallet.android.ui.transfer.viewmodel.CreateTransferViewModel;
import com.hyperwallet.android.ui.transfermethod.repository.PrepaidCardRepositoryFactory;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.user.repository.UserRepositoryFactory;

/**
 * Create Transfer Activity
 */
public class CreateTransferActivity extends AppCompatActivity implements OnNetworkErrorCallback {

    public static final String TAG = "transfer-funds:create-transfer";
    public static final String EXTRA_TRANSFER_SOURCE_TOKEN = "TRANSFER_SOURCE_TOKEN";
    public static final String EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT = "EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT";

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
                    UserRepositoryFactory.getInstance().getUserRepository(),
                    PrepaidCardRepositoryFactory.getInstance().getPrepaidCardRepository());
        } else {
            factory = new CreateTransferViewModel.CreateTransferViewModelFactory(sourceToken,
                    TransferRepositoryFactory.getInstance().getTransferRepository(),
                    TransferMethodRepositoryFactory.getInstance().getTransferMethodRepository(),
                    UserRepositoryFactory.getInstance().getUserRepository(),
                    PrepaidCardRepositoryFactory.getInstance().getPrepaidCardRepository());
        }

        mCreateTransferViewModel = ViewModelProviders.of(this, factory).get(CreateTransferViewModel.class);
        if (getIntent().getBooleanExtra(EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT, false)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mCreateTransferViewModel.setPortraitMode(true);
        }

        registerObservers();

        if (savedInstanceState == null) {
            ActivityUtils.initFragment(this, CreateTransferFragment.newInstance(), R.id.create_transfer_fragment);
        }
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

    @Override
    protected void onStop() {
        mCreateTransferViewModel.setCreateQuoteLoading(Boolean.FALSE);
        super.onStop();
    }

    private void navigate(@NonNull final Event<Transfer> event) {
        if (!event.isContentConsumed()) {
            Intent intent = new Intent(this, ScheduleTransferActivity.class);
            intent.putExtra(ScheduleTransferActivity.EXTRA_TRANSFER, event.getContent());
            intent.putExtra(ScheduleTransferActivity.EXTRA_TRANSFER_METHOD,
                    mCreateTransferViewModel.getTransferDestination().getValue());
            intent.putExtra(ScheduleTransferActivity.EXTRA_TRANSFER_METHOD_SOURCE,
                    mCreateTransferViewModel.getTransferSelectedSource().getValue());
            intent.putExtra(ScheduleTransferActivity.EXTRA_SHOW_FX_CHANGE_WARNING,
                    mCreateTransferViewModel.getShowFxRateChange().getValue());

            intent.putExtra(ScheduleTransferActivity.EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT,
                    mCreateTransferViewModel.isPortraitMode()
                            || getIntent().getBooleanExtra(EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT, false));

            startActivityForResult(intent, SCHEDULE_TRANSFER_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCHEDULE_TRANSFER_REQUEST_CODE
                && resultCode == Activity.RESULT_OK && data != null) {
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        TransferRepositoryFactory.clearInstance();
        TransferMethodRepositoryFactory.clearInstance();
        UserRepositoryFactory.clearInstance();
        super.onDestroy();
    }

    private void registerObservers() {
        mCreateTransferViewModel.getLoadTransferRequiredDataErrors().observe(this,
                new Observer<Event<Errors>>() {
                    @Override
                    public void onChanged(Event<Errors> event) {
                        if (event != null && !event.isContentConsumed()) {
                            ActivityUtils.showError(CreateTransferActivity.this, TAG, PageGroups.TRANSFER_FUNDS,
                                    event.getContent().getErrors());
                        }
                    }
                });

        mCreateTransferViewModel.getCreateTransferError().observe(this, new Observer<Event<Errors>>() {
            @Override
            public void onChanged(Event<Errors> event) {
                if (event != null && !event.isContentConsumed()) {
                    ActivityUtils.showError(CreateTransferActivity.this, TAG, PageGroups.TRANSFER_FUNDS,
                            event.getContent().getErrors());
                }
            }
        });

        mCreateTransferViewModel.getCreateTransfer().observe(this, new Observer<Event<Transfer>>() {
            @Override
            public void onChanged(final Event<Transfer> transfer) {
                navigate(transfer);
            }
        });

        mCreateTransferViewModel.getModuleUnavailableError().observe(this, new Observer<Event<Errors>>() {
            @Override
            public void onChanged(Event<Errors> event) {
                if (!event.isContentConsumed()) {
                    ActivityUtils.showError(CreateTransferActivity.this, TAG, PageGroups.TRANSFER_FUNDS,
                            event.getContent().getErrors());
                }
            }
        });
    }
}
