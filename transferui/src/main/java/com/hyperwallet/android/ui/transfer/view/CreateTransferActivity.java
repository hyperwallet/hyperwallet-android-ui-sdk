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
import com.hyperwallet.android.ui.common.viewmodel.Navigator;
import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.repository.TransferRepositoryFactory;
import com.hyperwallet.android.ui.transfer.viewmodel.CreateTransferViewModel;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;
import com.hyperwallet.android.ui.user.repository.UserRepositoryFactory;

/**
 * Create Transfer Activity
 */
public class CreateTransferActivity extends AppCompatActivity implements OnNetworkErrorCallback {

    public static final String TAG = "transfer-funds:create-transfer";
    public static final String EXTRA_TRANSFER_SOURCE_TOKEN = "TRANSFER_SOURCE_TOKEN";

    private CreateTransferViewModel mCreateTransferViewModel;
    private CreateTransferActivity.Helper mHelper;

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
        mHelper = new CreateTransferActivity.Helper(mCreateTransferViewModel) {
            @Override
            public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
                if (requestCode == SCHEDULE_TRANSFER_REQUEST_CODE
                    && resultCode == Activity.RESULT_OK && data != null) {
                    CreateTransferActivity.this.setResult(Activity.RESULT_OK, data);
                    CreateTransferActivity.this.finish();
                } else if (requestCode == SCHEDULE_TRANSFER_REQUEST_CODE) {
                    // back button
                    FragmentManager fragmentManager = CreateTransferActivity.this.getSupportFragmentManager();
                    CreateTransferFragment fragment = (CreateTransferFragment)
                            fragmentManager.findFragmentById(R.id.create_transfer_fragment);

                    if (fragment == null) {
                        fragment = CreateTransferFragment.newInstance();
                    }
                    fragment.reApplyFieldRules();
                }
            }

            @Override
            public AppCompatActivity getHostActivity() {
                return CreateTransferActivity.this;
            }
        };

        mHelper.registerObservers();

        if (savedInstanceState == null) {
            ActivityUtils.initFragment(this, CreateTransferFragment.newInstance(), R.id.create_transfer_fragment);
        }
    }

    @Override
    public void retry() {
        mHelper.retry();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        TransferRepositoryFactory.clearInstance();
        TransferMethodRepositoryFactory.clearInstance();
        UserRepositoryFactory.clearInstance();
        super.onDestroy();
    }



    public abstract static class Helper implements OnNetworkErrorCallback, Navigator<Event<Transfer>> {

        private CreateTransferViewModel mCreateTransferViewModel;

        public Helper(@NonNull final CreateTransferViewModel createTransferViewModel) {
            mCreateTransferViewModel = createTransferViewModel;
        }

        public abstract void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);
        public abstract AppCompatActivity getHostActivity();

        @Override
        public void retry() {
            FragmentManager fragmentManager = getHostActivity().getSupportFragmentManager();
            CreateTransferFragment fragment = (CreateTransferFragment)
                    fragmentManager.findFragmentById(R.id.create_transfer_fragment);

            if (fragment == null) {
                fragment = CreateTransferFragment.newInstance();
            }
            fragment.retry();
        }

        @Override
        public void navigate(Event<Transfer> event) {
            if (!event.isContentConsumed()) {
                Intent intent = new Intent(getHostActivity(), getHostActivity().getClass());
                intent.putExtra(ScheduleTransferActivity.EXTRA_TRANSFER, event.getContent());
                intent.putExtra(ScheduleTransferActivity.EXTRA_TRANSFER_METHOD,
                        mCreateTransferViewModel.getTransferDestination().getValue());
                intent.putExtra(ScheduleTransferActivity.EXTRA_SHOW_FX_CHANGE_WARNING,
                        mCreateTransferViewModel.getShowFxRateChange().getValue());
                getHostActivity().startActivityForResult(intent, SCHEDULE_TRANSFER_REQUEST_CODE);
            }
        }


        public void registerObservers() {
            mCreateTransferViewModel.getLoadTransferRequiredDataErrors().observe(getHostActivity(),
                    new Observer<Event<Errors>>() {
                        @Override
                        public void onChanged(Event<Errors> event) {
                            if (event != null && !event.isContentConsumed()) {
                                ActivityUtils.showError(getHostActivity(), TAG, PageGroups.TRANSFER_FUNDS,
                                        event.getContent().getErrors());
                            }
                        }
                    });

            mCreateTransferViewModel.getCreateTransferError().observe(getHostActivity(), new Observer<Event<Errors>>() {
                @Override
                public void onChanged(Event<Errors> event) {
                    if (event != null && !event.isContentConsumed()) {
                        ActivityUtils.showError(getHostActivity(), TAG, PageGroups.TRANSFER_FUNDS,
                                event.getContent().getErrors());
                    }
                }
            });

            mCreateTransferViewModel.getCreateTransfer().observe(getHostActivity(), new Observer<Event<Transfer>>() {
                @Override
                public void onChanged(final Event<Transfer> transfer) {
                    navigate(transfer);
                }
            });

            mCreateTransferViewModel.getModuleUnavailableError().observe(getHostActivity(), new Observer<Event<Errors>>() {
                @Override
                public void onChanged(Event<Errors> event) {
                    if (!event.isContentConsumed()) {
                        ActivityUtils.showError(getHostActivity(), TAG, PageGroups.TRANSFER_FUNDS,
                                event.getContent().getErrors());
                    }
                }
            });
        }

    }


}
