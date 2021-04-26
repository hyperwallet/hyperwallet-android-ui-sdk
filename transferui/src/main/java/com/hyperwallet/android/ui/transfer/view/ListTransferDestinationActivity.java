/*
 * Copyright 2019 Hyperwallet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package com.hyperwallet.android.ui.transfer.view;

import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.ADD_TRANSFER_METHOD_REQUEST_CODE;
import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.EXTRA_TRANSFER_METHOD_ADDED;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.ui.common.intent.HyperwalletIntent;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.common.util.PageGroups;
import com.hyperwallet.android.ui.common.view.ActivityUtils;
import com.hyperwallet.android.ui.common.view.OneClickListener;
import com.hyperwallet.android.ui.common.view.error.OnNetworkErrorCallback;
import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.viewmodel.ListTransferDestinationViewModel;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;

public class ListTransferDestinationActivity extends AppCompatActivity implements OnNetworkErrorCallback {

    public static final String TAG = "transfer-funds:create-transfer";
    public static final String EXTRA_SELECTED_DESTINATION_TOKEN = "SELECTED_DESTINATION_TOKEN";
    public static final String EXTRA_SELECTED_DESTINATION = "EXTRA_SELECTED_DESTINATION";
    public static final String EXTRA_SOURCE_IS_PREPAID_CARD_TYPE = "EXTRA_SOURCE_IS_PREPAID_CARD_TYPE";
    public static final String EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT = "EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT";

    private ListTransferDestinationViewModel mListTransferDestinationViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list_transfer_destination);

        String transferToken = getIntent().getStringExtra(EXTRA_SELECTED_DESTINATION_TOKEN);
        boolean sourceIsPrepaidCard = getIntent().getBooleanExtra(EXTRA_SOURCE_IS_PREPAID_CARD_TYPE, false);
        if (TextUtils.isEmpty(transferToken)) {
            throw new IllegalArgumentException(
                    "EXTRA_SELECTED_DESTINATION_TOKEN intent data is needed to start this activity");
        }

        mListTransferDestinationViewModel = ViewModelProviders.of(this,
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(
                        TransferMethodRepositoryFactory.getInstance().getTransferMethodRepository()))
                .get(ListTransferDestinationViewModel.class);

        if (savedInstanceState == null) {
            ActivityUtils.initFragment(this,
                    ListTransferDestinationFragment.newInstance(transferToken, sourceIsPrepaidCard),
                    R.id.list_destination_fragment);
        }

        FloatingActionButton button = findViewById(R.id.create_transfer_method_fab);
        final Intent intent = new Intent();
        intent.setAction(HyperwalletIntent.ACTION_SELECT_TRANSFER_METHOD);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        if (getIntent().getBooleanExtra(EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT, false)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            intent.putExtra(EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT, true);
        }

        if (intent.resolveActivity(getPackageManager()) != null) {
            button.setOnClickListener(new OneClickListener() {
                @Override
                public void onOneClick(View v) {
                    startActivityForResult(intent, ADD_TRANSFER_METHOD_REQUEST_CODE);
                }
            });
        } else {
            button.hide();
        }

        registerObservers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_TRANSFER_METHOD_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_SELECTED_DESTINATION, data.getParcelableExtra(EXTRA_TRANSFER_METHOD_ADDED));
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        getWindow().getDecorView().setSystemUiVisibility(0);
        super.onBackPressed();
    }

    @Override
    public void retry() {
        mListTransferDestinationViewModel.loadTransferDestinationList();
    }

    @Override
    protected void onStop() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        getWindow().getDecorView().setSystemUiVisibility(0);
        super.onStop();
    }

    private void registerObservers() {
        mListTransferDestinationViewModel.getSelectedTransferDestination().observe(this,
                new Observer<Event<TransferMethod>>() {
                    @Override
                    public void onChanged(Event<TransferMethod> destination) {
                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_SELECTED_DESTINATION, destination.getContent());
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                });

        mListTransferDestinationViewModel.getTransferDestinationError().observe(this,
                new Observer<Event<Errors>>() {
                    @Override
                    public void onChanged(Event<Errors> errorsEvent) {
                        if (!errorsEvent.isContentConsumed()) {
                            ActivityUtils.showError(ListTransferDestinationActivity.this, TAG,
                                    PageGroups.TRANSFER_FUNDS, errorsEvent.getContent().getErrors());
                        }
                    }
                });
    }
}