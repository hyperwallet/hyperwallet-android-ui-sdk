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
package com.hyperwallet.android.ui.transfermethod;

import android.content.Intent;
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
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.view.error.DefaultErrorDialogFragment;
import com.hyperwallet.android.ui.common.view.error.OnNetworkErrorCallback;
import com.hyperwallet.android.ui.view.CountrySelectionDialogFragment;
import com.hyperwallet.android.ui.view.CurrencySelectionDialogFragment;

import java.util.List;

public class SelectTransferMethodActivity extends AppCompatActivity implements
        CountrySelectionDialogFragment.CountrySelectionItemClickListener,
        CurrencySelectionDialogFragment.CurrencySelectionItemClickListener,
        SelectTransferMethodFragment.OnLoadTransferMethodConfigurationKeysNetworkErrorCallback,
        SelectTransferMethodFragment.OnLoadCurrencyConfigurationNetworkErrorCallback,
        SelectTransferMethodFragment.OnLoadTransferMethodTypeNetworkErrorCallback,
        SelectTransferMethodFragment.OnLoadCountrySelectionNetworkErrorCallback,
        SelectTransferMethodFragment.OnLoadCurrencySelectionNetworkErrorCallback,
        OnNetworkErrorCallback {

    private static final String ARGUMENT_RETRY_ACTION = "ARGUMENT_RETRY_ACTION";
    private static final short RETRY_LOAD_COUNTRY_SELECTION = 103;
    private static final short RETRY_LOAD_CURRENCY_CONFIGURATION = 101;
    private static final short RETRY_LOAD_CURRENCY_SELECTION = 104;
    private static final short RETRY_LOAD_TRANSFER_METHOD_CONFIGURATION_KEYS = 100;
    private static final short RETRY_LOAD_TRANSFER_METHOD_TYPES = 102;
    private static final String TAG = SelectTransferMethodActivity.class.getName();

    private short mRetryCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_transfer_method);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.activity_select_transfer_method_title);
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
            initFragment(SelectTransferMethodFragment.newInstance());
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
        transaction.add(R.id.select_transfer_method_fragment, fragment);
        transaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AddTransferMethodActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    @Override
    public void onCountryItemClicked(String countryCode) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SelectTransferMethodFragment selectTransferMethodFragment =
                (SelectTransferMethodFragment) fragmentManager.findFragmentById(R.id.select_transfer_method_fragment);
        selectTransferMethodFragment.showCountryCode(countryCode);
        CountrySelectionDialogFragment countrySelectionDialogFragment =
                (CountrySelectionDialogFragment) fragmentManager.findFragmentById(android.R.id.content);
        countrySelectionDialogFragment.dismiss();
        getSupportFragmentManager().popBackStack(CountrySelectionDialogFragment.TAG,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void onCurrencyItemClicked(String currencyCode) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SelectTransferMethodFragment selectTransferMethodFragment =
                (SelectTransferMethodFragment) fragmentManager.findFragmentById(R.id.select_transfer_method_fragment);
        selectTransferMethodFragment.showCurrencyCode(currencyCode);
        CurrencySelectionDialogFragment currencySelectionDialogFragment =
                (CurrencySelectionDialogFragment) fragmentManager.findFragmentById(android.R.id.content);
        currencySelectionDialogFragment.dismiss();
        getSupportFragmentManager().popBackStack(CurrencySelectionDialogFragment.TAG,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void showErrorsLoadTransferMethodConfigurationKeys(@NonNull final List<HyperwalletError> errors) {
        mRetryCode = RETRY_LOAD_TRANSFER_METHOD_CONFIGURATION_KEYS;
        showError(errors);
    }

    @Override
    public void showErrorsLoadCurrencyConfiguration(@NonNull final List<HyperwalletError> errors) {
        mRetryCode = RETRY_LOAD_CURRENCY_CONFIGURATION;
        showError(errors);
    }

    @Override
    public void showErrorsLoadTransferMethodTypes(@NonNull final List<HyperwalletError> errors) {
        mRetryCode = RETRY_LOAD_TRANSFER_METHOD_TYPES;
        showError(errors);
    }

    @Override
    public void showErrorsLoadCountrySelection(@NonNull final List<HyperwalletError> errors) {
        mRetryCode = RETRY_LOAD_COUNTRY_SELECTION;
        showError(errors);
    }

    @Override
    public void showErrorsLoadCurrencySelection(@NonNull final List<HyperwalletError> errors) {
        mRetryCode = RETRY_LOAD_CURRENCY_SELECTION;
        showError(errors);
    }

    @Override
    public void retry() {
        SelectTransferMethodFragment fragment = getSelectTransferMethodFragment();
        switch (mRetryCode) {
            case RETRY_LOAD_CURRENCY_CONFIGURATION:
                fragment.reloadCurrency();
                break;
            case RETRY_LOAD_TRANSFER_METHOD_CONFIGURATION_KEYS:
                fragment.reloadTransferMethodConfigurationKeys();
                break;
            case RETRY_LOAD_TRANSFER_METHOD_TYPES:
                fragment.reloadTransferMethodTypes();
                break;
            case RETRY_LOAD_CURRENCY_SELECTION:
                fragment.reloadCurrencySelection();
                break;
            case RETRY_LOAD_COUNTRY_SELECTION:
                fragment.reloadCountrySelection();
                break;
            default: // no default action
        }
    }

    private SelectTransferMethodFragment getSelectTransferMethodFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SelectTransferMethodFragment fragment = (SelectTransferMethodFragment)
                fragmentManager.findFragmentById(R.id.select_transfer_method_fragment);
        if (fragment == null) {
            fragment = SelectTransferMethodFragment.newInstance();
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
