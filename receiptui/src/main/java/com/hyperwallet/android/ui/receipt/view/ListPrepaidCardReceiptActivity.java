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
package com.hyperwallet.android.ui.receipt.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.common.view.ActivityUtils;
import com.hyperwallet.android.ui.common.view.error.OnNetworkErrorCallback;
import com.hyperwallet.android.ui.common.viewmodel.Navigator;
import com.hyperwallet.android.ui.receipt.R;
import com.hyperwallet.android.ui.receipt.repository.PrepaidCardReceiptRepositoryImpl;
import com.hyperwallet.android.ui.receipt.viewmodel.ListPrepaidCardReceiptViewModel;
import com.hyperwallet.android.ui.receipt.viewmodel.ReceiptViewModel;

public class ListPrepaidCardReceiptActivity extends AppCompatActivity implements OnNetworkErrorCallback,
        Navigator<Event<Receipt>> {

    public static final String EXTRA_PREPAID_CARD_TOKEN = "PREPAID_CARD_TOKEN";

    private ReceiptViewModel mReceiptViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_prepaid_card_receipt);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_receipt_list);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String token = getIntent().getStringExtra(EXTRA_PREPAID_CARD_TOKEN);
        if (TextUtils.isEmpty(token)) {
            throw new IllegalArgumentException("Activity " + ListPrepaidCardReceiptActivity.class.getName()
                    + " requires parameter: " + EXTRA_PREPAID_CARD_TOKEN + " value");
        }

        mReceiptViewModel = ViewModelProviders.of(this, new ListPrepaidCardReceiptViewModel
                .ListPrepaidCardReceiptViewModelFactory(new PrepaidCardReceiptRepositoryImpl(token)))
                .get(ReceiptViewModel.class);

        mReceiptViewModel.getReceiptErrors().observe(this, new Observer<Event<HyperwalletErrors>>() {
            @Override
            public void onChanged(Event<HyperwalletErrors> event) {
                if (event != null && !event.isContentConsumed()) {
                    ActivityUtils.showError(ListPrepaidCardReceiptActivity.this,
                            event.getContent().getErrors());
                }
            }
        });

        mReceiptViewModel.getDetailNavigation().observe(this, new Observer<Event<Receipt>>() {
            @Override
            public void onChanged(@NonNull final Event<Receipt> event) {
                navigate(event);
            }
        });

        if (savedInstanceState == null) {
            ActivityUtils.initFragment(this, ListReceiptFragment.newInstance(),
                    R.id.list_receipt_fragment);
        }
    }

    @Override
    public void retry() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ListReceiptFragment fragment = (ListReceiptFragment)
                fragmentManager.findFragmentById(R.id.list_receipt_fragment);

        if (fragment == null) {
            fragment = ListReceiptFragment.newInstance();
        }
        fragment.retry();
    }

    @Override
    public void navigate(@NonNull final Event<Receipt> event) {
        if (!event.isContentConsumed()) {
            Intent intent = new Intent(this, ReceiptDetailActivity.class);
            intent.putExtra(ReceiptDetailActivity.EXTRA_RECEIPT, event.getContent());
            startActivity(intent);
        }
    }
}
