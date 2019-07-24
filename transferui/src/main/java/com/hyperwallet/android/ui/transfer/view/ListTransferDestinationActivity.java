/*
 * Copyright 2018 Hyperwallet
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

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.ui.common.view.error.DefaultErrorDialogFragment;
import com.hyperwallet.android.ui.common.view.error.OnNetworkErrorCallback;
import com.hyperwallet.android.ui.transfer.R;

import java.util.List;

public class ListTransferDestinationActivity extends AppCompatActivity implements
        ListTransferDestinationFragment.DestinationItemClickListener,
        OnNetworkErrorCallback {

    private static final String ARGUMENT_RETRY_ACTION = "ARGUMENT_RETRY_ACTION";
    private static final String TAG = ListTransferDestinationActivity.class.getSimpleName();

    private short mRetryCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_transfer_destination);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.destination);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (savedInstanceState == null) {
            initFragment(ListTransferDestinationFragment.newInstance());
        } else {
            mRetryCode = savedInstanceState.getShort(ARGUMENT_RETRY_ACTION);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putShort(ARGUMENT_RETRY_ACTION, mRetryCode);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            mRetryCode = savedInstanceState.getShort(ARGUMENT_RETRY_ACTION);
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

    private void initFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.frame, fragment);
        transaction.commit();
    }

    @Override
    public void selectTransferDestination(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        ListTransferDestinationFragment countrySelectionDialogFragment =
                (ListTransferDestinationFragment) fragmentManager.findFragmentById(android.R.id.content);


    }

    @Override
    public void retry() {
        ListTransferDestinationFragment fragment = getSelectTransferMethodFragment();
        //fragment.reloadTransferDestination();
    }

    private ListTransferDestinationFragment getSelectTransferMethodFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ListTransferDestinationFragment fragment = (ListTransferDestinationFragment)
                fragmentManager.findFragmentById(R.id.frame);
        if (fragment == null) {
            fragment = ListTransferDestinationFragment.newInstance();
        }
        return fragment;
    }

    private void showError(@NonNull List<HyperwalletError> errors) {
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
