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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.common.util.PageGroups;
import com.hyperwallet.android.ui.common.view.ActivityUtils;
import com.hyperwallet.android.ui.common.view.error.OnNetworkErrorCallback;
import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.TransferLocalBroadcast;
import com.hyperwallet.android.ui.transfer.repository.TransferRepositoryFactory;
import com.hyperwallet.android.ui.transfer.viewmodel.ScheduleTransferViewModel;

/**
 * Schedule Transfer Activity
 */
public class ScheduleTransferActivity extends AppCompatActivity implements OnNetworkErrorCallback {

    public static final String TAG = "transfer-funds:review-transfer";

    public static final String EXTRA_TRANSFER = "TRANSFER";
    public static final String EXTRA_TRANSFER_METHOD = "TRANSFER_METHOD";
    public static final String EXTRA_SHOW_FX_CHANGE_WARNING = "SHOW_FX_CHANGE_WARNING";

    private ScheduleTransferViewModel mScheduleTransferViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_transfer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.mobileConfirmationHeader);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Parcelable transferParcel = getIntent().getParcelableExtra(EXTRA_TRANSFER);
        Parcelable transferMethodParcel = getIntent().getParcelableExtra(EXTRA_TRANSFER_METHOD);
        boolean showFxRateChangeWarningParcel = getIntent().getBooleanExtra(EXTRA_SHOW_FX_CHANGE_WARNING, false);
        if (transferParcel instanceof Transfer && transferMethodParcel instanceof TransferMethod) {
            mScheduleTransferViewModel = ViewModelProviders.of(this,
                    new ScheduleTransferViewModel.ScheduleTransferViewModelFactory(
                            TransferRepositoryFactory.getInstance().getTransferRepository()))
                    .get(ScheduleTransferViewModel.class);
            mScheduleTransferViewModel.setTransfer((Transfer) transferParcel);
            mScheduleTransferViewModel.setTransferDestination((TransferMethod) transferMethodParcel);
            mScheduleTransferViewModel.setShowFxChangeWarning(showFxRateChangeWarningParcel);
            registerObservers();
        } else {
            throw new IllegalArgumentException("Required extra arguments are invalid for "
                    + ScheduleTransferActivity.class.getName()
                    + " extra keys: EXTRA_TRANSFER and EXTRA_TRANSFER_METHOD");
        }

        if (savedInstanceState == null) {
            ActivityUtils.initFragment(this, ScheduleTransferFragment.newInstance(), R.id.schedule_transfer_fragment);
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

    private void registerObservers() {
        mScheduleTransferViewModel.getTransferStatusTransitionError().observe(this,
                new Observer<Event<Errors>>() {
                    @Override
                    public void onChanged(final Event<Errors> event) {
                        if (event != null && !event.isContentConsumed()) {
                            ActivityUtils.showError(ScheduleTransferActivity.this, TAG, PageGroups.TRANSFER_FUNDS,
                                    event.getContent().getErrors());
                        }
                    }
                });

        mScheduleTransferViewModel.getTransferStatusTransition().observe(this, new Observer<StatusTransition>() {
            @Override
            public void onChanged(final StatusTransition statusTransition) {
                Intent intent = TransferLocalBroadcast.createBroadcastIntentTransferScheduled(
                        statusTransition);
                LocalBroadcastManager.getInstance(ScheduleTransferActivity.this).sendBroadcast(intent);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

}
