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
package com.hyperwallet.android.ui.transfermethod.view;

import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.ADD_TRANSFER_METHOD_REQUEST_CODE;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.util.PageGroups;
import com.hyperwallet.android.ui.common.view.ActivityUtils;
import com.hyperwallet.android.ui.common.view.TransferMethodUtils;
import com.hyperwallet.android.ui.common.view.error.OnNetworkErrorCallback;

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

    public static final String TAG = "transfer-method:add:select-transfer-method";
    public static final String EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT = "EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT";

    private static final String ARGUMENT_RETRY_ACTION = "ARGUMENT_RETRY_ACTION";
    private static final short RETRY_LOAD_COUNTRY_SELECTION = 103;
    private static final short RETRY_LOAD_CURRENCY_CONFIGURATION = 101;
    private static final short RETRY_LOAD_CURRENCY_SELECTION = 104;
    private static final short RETRY_LOAD_TRANSFER_METHOD_CONFIGURATION_KEYS = 100;
    private static final short RETRY_LOAD_TRANSFER_METHOD_TYPES = 102;

    private short mRetryCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_select_transfer_method);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        int titleStyleCollapse = TransferMethodUtils.getAdjustCollapseTitleStyle(getTitle().toString());
        collapsingToolbar.setCollapsedTitleTextAppearance(titleStyleCollapse);
        int titleStyleExpanded = TransferMethodUtils.getAdjustExpandTitleStyle(getTitle().toString());
        collapsingToolbar.setExpandedTitleTextAppearance(titleStyleExpanded);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        TextView titleText = findViewById(R.id.toolbar_title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (getIntent().getBooleanExtra(EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT, false)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (savedInstanceState == null) {
            ActivityUtils.initFragment(this,
                    SelectTransferMethodFragment.newInstance(
                            getIntent().getBooleanExtra(EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT, false)),
                    R.id.select_transfer_method_fragment);
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
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_TRANSFER_METHOD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
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
    public void showErrorsLoadTransferMethodConfigurationKeys(@NonNull final List<Error> errors) {
        mRetryCode = RETRY_LOAD_TRANSFER_METHOD_CONFIGURATION_KEYS;
        ActivityUtils.showError(this, TAG, PageGroups.TRANSFER_METHOD, errors);
    }

    @Override
    public void showErrorsLoadCurrencyConfiguration(@NonNull final List<Error> errors) {
        mRetryCode = RETRY_LOAD_CURRENCY_CONFIGURATION;
        ActivityUtils.showError(this, TAG, PageGroups.TRANSFER_METHOD, errors);
    }

    @Override
    public void showErrorsLoadTransferMethodTypes(@NonNull final List<Error> errors) {
        mRetryCode = RETRY_LOAD_TRANSFER_METHOD_TYPES;
        ActivityUtils.showError(this, TAG, PageGroups.TRANSFER_METHOD, errors);
    }

    @Override
    public void showErrorsLoadCountrySelection(@NonNull final List<Error> errors) {
        mRetryCode = RETRY_LOAD_COUNTRY_SELECTION;
        ActivityUtils.showError(this, TAG, PageGroups.TRANSFER_METHOD, errors);
    }

    @Override
    public void showErrorsLoadCurrencySelection(@NonNull final List<Error> errors) {
        mRetryCode = RETRY_LOAD_CURRENCY_SELECTION;
        ActivityUtils.showError(this, TAG, PageGroups.TRANSFER_METHOD, errors);
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
            fragment = SelectTransferMethodFragment
                    .newInstance(getIntent().getBooleanExtra(EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT, false));
        }
        return fragment;
    }
}
