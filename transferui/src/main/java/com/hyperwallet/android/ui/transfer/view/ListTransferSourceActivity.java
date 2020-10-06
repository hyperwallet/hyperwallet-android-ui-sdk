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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.common.util.PageGroups;
import com.hyperwallet.android.ui.common.view.ActivityUtils;
import com.hyperwallet.android.ui.common.view.error.OnNetworkErrorCallback;
import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.TransferSourceWrapper;
import com.hyperwallet.android.ui.transfer.viewmodel.ListTransferSourceViewModel;
import com.hyperwallet.android.ui.transfermethod.repository.PrepaidCardRepositoryFactory;

import java.util.ArrayList;
import java.util.List;

public class ListTransferSourceActivity extends AppCompatActivity implements OnNetworkErrorCallback {

    public static final String TAG = "transfer-funds:create-transfer";
    public static final String EXTRA_SELECTED_SOURCE = "EXTRA_SELECTED_SOURCE";
    public static final String EXTRA_SELECTED_SOURCE_TOKEN = "SELECTED_SOURCE_TOKEN";
    public static final String EXTRA_TRANSFER_SOURCE_LIST = "TRANSFER_SOURCE_LIST";

    private ListTransferSourceViewModel mListTransferSourceViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_transfer_source);

        mListTransferSourceViewModel = ViewModelProviders.of(this,
                new ListTransferSourceViewModel.ListTransferSourceViewModelFactory())
                .get(ListTransferSourceViewModel.class);

        String transferToken = getIntent().getStringExtra(EXTRA_SELECTED_SOURCE_TOKEN);
        if (TextUtils.isEmpty(transferToken)) {
            throw new IllegalArgumentException(
                    "EXTRA_SELECTED_SOURCE_TOKEN intent data is needed to start this activity");
        }

        ArrayList<TransferSourceWrapper> sourceList = getIntent().getParcelableArrayListExtra(
                EXTRA_TRANSFER_SOURCE_LIST);
        if (savedInstanceState == null) {
            ActivityUtils.initFragment(this, ListTransferSourceFragment.newInstance(transferToken, sourceList),
                    R.id.list_source_fragment);
        }

        registerObservers();
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
        mListTransferSourceViewModel.getTransferSourceSelection().observe(this,
                new Observer<Event<TransferSourceWrapper>>() {
                    @Override
                    public void onChanged(Event<TransferSourceWrapper> source) {
                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_SELECTED_SOURCE, source.getContent());
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                });

        mListTransferSourceViewModel.getTransferSourceError().observe(this,
                new Observer<Event<Errors>>() {
                    @Override
                    public void onChanged(Event<Errors> errorsEvent) {
                        if (!errorsEvent.isContentConsumed()) {
                            ActivityUtils.showError(ListTransferSourceActivity.this, TAG,
                                    PageGroups.TRANSFER_FUNDS, errorsEvent.getContent().getErrors());
                        }
                    }
                });
    }
}