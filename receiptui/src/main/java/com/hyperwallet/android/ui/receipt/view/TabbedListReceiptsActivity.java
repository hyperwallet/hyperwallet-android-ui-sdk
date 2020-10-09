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
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.common.util.PageGroups;
import com.hyperwallet.android.ui.common.view.ActivityUtils;
import com.hyperwallet.android.ui.common.view.error.OnNetworkErrorCallback;
import com.hyperwallet.android.ui.receipt.R;
import com.hyperwallet.android.ui.receipt.viewmodel.TabbedListReceiptsViewModel;
import com.hyperwallet.android.ui.transfermethod.repository.PrepaidCardRepositoryImpl;
import com.hyperwallet.android.ui.user.repository.UserRepositoryImpl;

public class TabbedListReceiptsActivity extends AppCompatActivity implements OnNetworkErrorCallback {
    public static final String TAG = "receipts:prepaid:tabbed-list-receipts";

    public static final String EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT = "EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT";
    private TabbedListReceiptsViewModel mTabbedListReceiptsViewModel;

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

        mTabbedListReceiptsViewModel = ViewModelProviders.of(this, new TabbedListReceiptsViewModel.TabbedListReceiptsViewModelFactory(new UserRepositoryImpl(), new PrepaidCardRepositoryImpl()))
                .get(TabbedListReceiptsViewModel.class);

        registerObservers();

        if (savedInstanceState == null) {
            ActivityUtils.initFragment(this, TabbedListReceiptsFragment.newInstance(),
                    R.id.tabbed_list_receipt_fragment);
        }
    }

    private void registerObservers() {
        mTabbedListReceiptsViewModel.mErrors.observe(this, new Observer<Event<Errors>>() {
            @Override
            public void onChanged(Event<Errors> errorsEvent) {
                ActivityUtils.showError(TabbedListReceiptsActivity.this, TabbedListReceiptsActivity.TAG,
                        PageGroups.RECEIPTS, errorsEvent.getContent().getErrors());
            }
        });
    }

    @Override
    public void retry() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TabbedListReceiptsFragment fragment = (TabbedListReceiptsFragment)
                fragmentManager.findFragmentById(R.id.list_receipt_fragment);

        if (fragment == null) {
            fragment = TabbedListReceiptsFragment.newInstance();
        }
        fragment.retry();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
