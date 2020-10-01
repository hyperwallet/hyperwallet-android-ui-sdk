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

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.hyperwallet.android.ui.common.view.ActivityUtils;
import com.hyperwallet.android.ui.common.view.error.OnNetworkErrorCallback;
import com.hyperwallet.android.ui.receipt.R;

public class TabbedListReceiptActivity extends AppCompatActivity implements OnNetworkErrorCallback {
    public static final String EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT = "EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT";
    public static final String EXTRA_PREPAID_CARD_TOKEN = "PREPAID_CARD_TOKEN";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed_list_receipt);

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

        if (getIntent().getBooleanExtra(EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT, false)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        String token = getIntent().getStringExtra(EXTRA_PREPAID_CARD_TOKEN);
        if (TextUtils.isEmpty(token)) {
            throw new IllegalArgumentException("Activity " + TabbedListReceiptActivity.class.getName()
                    + " requires parameter: " + EXTRA_PREPAID_CARD_TOKEN + " value");
        }

        if (savedInstanceState == null) {
            ActivityUtils.initFragment(this, TabbedListReceiptFragment.newInstance(),
                    R.id.tabbed_list_receipt_fragment);
        }
    }

    @Override
    public void retry() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TabbedListReceiptFragment fragment = (TabbedListReceiptFragment)
                fragmentManager.findFragmentById(R.id.list_receipt_fragment);

        if (fragment == null) {
            fragment = TabbedListReceiptFragment.newInstance();
        }
        fragment.retry();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
