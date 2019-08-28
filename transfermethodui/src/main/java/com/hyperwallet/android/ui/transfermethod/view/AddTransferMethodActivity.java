/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Hyperwallet Systems Inc.
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
package com.hyperwallet.android.ui.transfermethod.view;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.view.ActivityUtils;
import com.hyperwallet.android.ui.common.view.TransferMethodUtils;
import com.hyperwallet.android.ui.common.view.error.OnNetworkErrorCallback;

import java.util.List;

public class AddTransferMethodActivity extends AppCompatActivity implements
        WidgetSelectionDialogFragment.WidgetSelectionItemListener,
        AddTransferMethodFragment.OnAddTransferMethodNetworkErrorCallback,
        AddTransferMethodFragment.OnLoadTransferMethodConfigurationFieldsNetworkErrorCallback,
        OnNetworkErrorCallback, WidgetDateDialogFragment.OnSelectedDateCallback {

    public static final String EXTRA_TRANSFER_METHOD_COUNTRY = "TRANSFER_METHOD_COUNTRY";
    public static final String EXTRA_TRANSFER_METHOD_CURRENCY = "TRANSFER_METHOD_CURRENCY";
    public static final String EXTRA_TRANSFER_METHOD_TYPE = "TRANSFER_METHOD_TYPE";
    public static final String EXTRA_TRANSFER_METHOD_PROFILE_TYPE = "TRANSFER_METHOD_PROFILE_TYPE";
    public static final int ADD_TRANSFER_METHOD_REQUEST_CODE = 100;
    private static final String ARGUMENT_RETRY_ACTION = "ARGUMENT_RETRY_ACTION";
    private static final short RETRY_SHOW_ERROR_ADD_TRANSFER_METHOD = 100;
    private static final short RETRY_SHOW_ERROR_LOAD_TMC_FIELDS = 101;

    private short mRetryCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transfer_method);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(TransferMethodUtils.getTransferMethodName(this,
                getIntent().getStringExtra(EXTRA_TRANSFER_METHOD_TYPE)));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (savedInstanceState == null) {
            ActivityUtils.initFragment(this, AddTransferMethodFragment.newInstance(
                    getIntent().getStringExtra(EXTRA_TRANSFER_METHOD_COUNTRY),
                    getIntent().getStringExtra(EXTRA_TRANSFER_METHOD_CURRENCY),
                    getIntent().getStringExtra(EXTRA_TRANSFER_METHOD_TYPE),
                    getIntent().getStringExtra(EXTRA_TRANSFER_METHOD_PROFILE_TYPE)
            ), R.id.add_transfer_method_fragment);
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

    @Override
    public void onWidgetSelectionItemClicked(@NonNull final String selectedValue, @NonNull final String fieldName) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddTransferMethodFragment addTransferMethodFragment =
                (AddTransferMethodFragment) fragmentManager.findFragmentById(R.id.add_transfer_method_fragment);
        addTransferMethodFragment.onWidgetSelectionItemClicked(selectedValue, fieldName);

        WidgetSelectionDialogFragment widgetSelectionDialogFragment =
                (WidgetSelectionDialogFragment) fragmentManager.findFragmentById(android.R.id.content);
        widgetSelectionDialogFragment.dismiss();
        getSupportFragmentManager().popBackStack(WidgetSelectionDialogFragment.TAG,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void showErrorsAddTransferMethod(@NonNull final List<HyperwalletError> errors) {
        mRetryCode = RETRY_SHOW_ERROR_ADD_TRANSFER_METHOD;
        ActivityUtils.showError(this, errors);
    }

    @Override
    public void showErrorsLoadTransferMethodConfigurationFields(@NonNull final List<HyperwalletError> errors) {
        mRetryCode = RETRY_SHOW_ERROR_LOAD_TMC_FIELDS;
        ActivityUtils.showError(this, errors);
    }

    @Override
    public void retry() {
        AddTransferMethodFragment fragment = getAddTransferMethodFragment();
        switch (mRetryCode) {
            case RETRY_SHOW_ERROR_ADD_TRANSFER_METHOD:
                fragment.retryAddTransferMethod();
                break;
            case RETRY_SHOW_ERROR_LOAD_TMC_FIELDS:
                fragment.reloadTransferMethodConfigurationFields();
                break;
            default: // no default action
        }
    }

    private AddTransferMethodFragment getAddTransferMethodFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddTransferMethodFragment fragment = (AddTransferMethodFragment)
                fragmentManager.findFragmentById(R.id.add_transfer_method_fragment);

        if (fragment == null) {
            fragment = AddTransferMethodFragment.newInstance(
                    getIntent().getStringExtra(EXTRA_TRANSFER_METHOD_COUNTRY),
                    getIntent().getStringExtra(EXTRA_TRANSFER_METHOD_CURRENCY),
                    getIntent().getStringExtra(EXTRA_TRANSFER_METHOD_TYPE),
                    getIntent().getStringExtra(EXTRA_TRANSFER_METHOD_PROFILE_TYPE)
            );
        }
        return fragment;
    }

    @Override
    public void setSelectedDateField(@NonNull String fieldName, final String selectedValue) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddTransferMethodFragment addTransferMethodFragment =
                (AddTransferMethodFragment) fragmentManager.findFragmentById(R.id.add_transfer_method_fragment);
        if (addTransferMethodFragment != null) {
            addTransferMethodFragment.onDateSelected(selectedValue, fieldName);
        }
    }
}
