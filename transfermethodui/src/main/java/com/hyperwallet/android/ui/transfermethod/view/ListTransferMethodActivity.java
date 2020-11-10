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

import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.SELECT_TRANSFER_METHOD_REQUEST_CODE;
import static com.hyperwallet.android.ui.transfermethod.view.ListTransferMethodFragment.ARGUMENT_IS_TRANSFER_METHODS_RELOAD_NEEDED;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.util.PageGroups;
import com.hyperwallet.android.ui.common.view.ActivityUtils;
import com.hyperwallet.android.ui.common.view.OneClickListener;
import com.hyperwallet.android.ui.common.view.TransferMethodUtils;
import com.hyperwallet.android.ui.common.view.error.OnNetworkErrorCallback;

import java.util.List;
import java.util.Objects;

public class ListTransferMethodActivity extends AppCompatActivity implements
        ListTransferMethodFragment.OnAddNewTransferMethodSelected,
        ListTransferMethodFragment.OnDeactivateTransferMethodNetworkErrorCallback,
        ListTransferMethodFragment.OnLoadTransferMethodNetworkErrorCallback,
        ListTransferMethodFragment.OnTransferMethodContextMenuDeletionSelected,
        OnTransferMethodDeactivateCallback, OnNetworkErrorCallback {

    public static final String TAG = "transfer-method:list:list-transfer-methods";
    public static final String EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT = "EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT";

    private static final String ARGUMENT_RETRY_ACTION = "ARGUMENT_RETRY_ACTION";
    private static final String ARGUMENT_TRANSFER_METHOD = "ARGUMENT_TRANSFER_METHOD";
    private static final short RETRY_CONFIRM_DEACTIVATE_TRANSFER_METHOD = 102;
    private static final short RETRY_CONFIRM_DEACTIVATE_TRANSFER_METHOD_DIALOG = 103;
    private static final short RETRY_DEACTIVATE_TRANSFER_METHOD = 101;
    private static final short RETRY_LOAD_TRANSFER_METHOD = 100;

    private short mRetryCode;
    private TransferMethod mTransferMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_transfer_method);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView titleText = findViewById(R.id.toolbar_title);
        titleText.setText(R.string.mobileTransferMethodsHeader);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (getIntent().getBooleanExtra(EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT, false)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new OneClickListener() {
            @Override
            public void onOneClick(View v) {
                showSelectTransferMethodView();
            }
        });

        if (savedInstanceState == null) {
            ActivityUtils.initFragment(this, ListTransferMethodFragment.newInstance(),
                    R.id.list_transfer_method_fragment);
        } else {
            mRetryCode = savedInstanceState.getShort(ARGUMENT_RETRY_ACTION);
            mTransferMethod = savedInstanceState.getParcelable(ARGUMENT_TRANSFER_METHOD);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putShort(ARGUMENT_RETRY_ACTION, mRetryCode);
        outState.putParcelable(ARGUMENT_TRANSFER_METHOD, mTransferMethod);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            mRetryCode = savedInstanceState.getShort(ARGUMENT_RETRY_ACTION);
            mTransferMethod = savedInstanceState.getParcelable(ARGUMENT_TRANSFER_METHOD);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_TRANSFER_METHOD_REQUEST_CODE && resultCode == RESULT_OK) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentById(R.id.list_transfer_method_fragment);
            if (fragment != null && fragment.getArguments() != null) {
                fragment.getArguments().putBoolean(ARGUMENT_IS_TRANSFER_METHODS_RELOAD_NEEDED, true);
            }
        }
    }

    @Override
    public void showSelectTransferMethodView() {
        Intent myIntent = new Intent(ListTransferMethodActivity.this, SelectTransferMethodActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        myIntent.putExtra(SelectTransferMethodActivity.EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT,
                getIntent().getBooleanExtra(EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT, false));
        startActivityForResult(myIntent, SELECT_TRANSFER_METHOD_REQUEST_CODE);
    }

    @Override
    public void showErrorsDeactivateTransferMethod(@NonNull final List<Error> errors) {
        mRetryCode = RETRY_DEACTIVATE_TRANSFER_METHOD;
        ActivityUtils.showError(this, TAG, PageGroups.TRANSFER_METHOD, errors);
    }

    @Override
    public void showErrorsLoadTransferMethods(@NonNull final List<Error> errors) {
        mRetryCode = RETRY_LOAD_TRANSFER_METHOD;
        ActivityUtils.showError(this, TAG, PageGroups.TRANSFER_METHOD, errors);
    }

    @Override
    public void showConfirmationDialog(@NonNull TransferMethod transferMethod) {
        mRetryCode = RETRY_CONFIRM_DEACTIVATE_TRANSFER_METHOD_DIALOG;
        mTransferMethod = transferMethod;
        FragmentManager fragmentManager = getSupportFragmentManager();
        TransferMethodConfirmDeactivationDialogFragment fragment = (TransferMethodConfirmDeactivationDialogFragment)
                fragmentManager.findFragmentByTag(TransferMethodConfirmDeactivationDialogFragment.TAG);
        if (fragment == null) {
            fragment = TransferMethodConfirmDeactivationDialogFragment.newInstance(
                    TransferMethodUtils.getTransferMethodName(getApplicationContext(),
                            Objects.requireNonNull(transferMethod.getField(TransferMethod.TransferMethodFields.TYPE)))
            );
        }

        if (!fragment.isAdded()) {
            fragment.show(fragmentManager);
        }
    }

    @Override
    public void confirm() {
        mRetryCode = RETRY_CONFIRM_DEACTIVATE_TRANSFER_METHOD;
        FragmentManager fragmentManager = getSupportFragmentManager();
        ListTransferMethodFragment fragment = (ListTransferMethodFragment)
                fragmentManager.findFragmentById(R.id.list_transfer_method_fragment);

        if (fragment == null) {
            fragment = ListTransferMethodFragment.newInstance();
        }
        fragment.confirmTransferMethodDeactivation(mTransferMethod);
    }

    @Override
    public void retry() {
        switch (mRetryCode) {
            case RETRY_CONFIRM_DEACTIVATE_TRANSFER_METHOD:
                confirm();
                break;
            case RETRY_CONFIRM_DEACTIVATE_TRANSFER_METHOD_DIALOG:
                showConfirmationDialog(mTransferMethod);
                break;
            case RETRY_LOAD_TRANSFER_METHOD:
                retryLoadTransferMethods();
                break;
            case RETRY_DEACTIVATE_TRANSFER_METHOD:
                retryDeactivateTransferMethod();
                break;
            default: // no default action
        }
    }

    private void retryDeactivateTransferMethod() {
        ListTransferMethodFragment fragment = getListTransferMethodFragment();
        fragment.confirmTransferMethodDeactivation(mTransferMethod);
    }

    private void retryLoadTransferMethods() {
        ListTransferMethodFragment fragment = getListTransferMethodFragment();
        fragment.loadTransferMethods();
    }

    private ListTransferMethodFragment getListTransferMethodFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ListTransferMethodFragment fragment = (ListTransferMethodFragment)
                fragmentManager.findFragmentById(R.id.list_transfer_method_fragment);
        if (fragment == null) {
            fragment = ListTransferMethodFragment.newInstance();
        }
        return fragment;
    }
}
