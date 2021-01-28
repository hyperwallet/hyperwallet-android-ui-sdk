package com.hyperwallet.android.ui.transfermethod.view;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

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

public class UpdateTransferMethodActivity extends AppCompatActivity implements
        WidgetSelectionDialogFragment.WidgetSelectionItemListener,
        UpdateTransferMethodFragment.OnUpdateTransferMethodNetworkErrorCallback,
        UpdateTransferMethodFragment.OnLoadTransferMethodConfigurationFieldsNetworkErrorCallback,
        OnNetworkErrorCallback, WidgetDateDialogFragment.OnSelectedDateCallback {

    public static final String TAG = "transfer-method:update:collect-transfer-method-information";

    public static final String EXTRA_TRANSFER_METHOD_TOKEN = "EXTRA_TRANSFER_METHOD_TOKEN";
    private static final String ARGUMENT_RETRY_ACTION = "ARGUMENT_RETRY_ACTION";
    public static final String EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT = "EXTRA_LOCK_SCREEN_ORIENTATION_TO_PORTRAIT";
    private static final short RETRY_SHOW_ERROR_UPDATE_TRANSFER_METHOD = 100;
    private static final short RETRY_SHOW_ERROR_LOAD_TMC_FIELDS = 101;

    private short mRetryCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_update_transfer_method);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        int titleStyleCollapse = TransferMethodUtils.getAdjustCollapseTitleStyle(getTitle().toString());
        collapsingToolbar.setCollapsedTitleTextAppearance(titleStyleCollapse);
        int titleStyleExpanded = TransferMethodUtils.getAdjustExpandTitleStyle(getTitle().toString());
        collapsingToolbar.setExpandedTitleTextAppearance(titleStyleExpanded);
        getSupportActionBar().setTitle("");

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
            ActivityUtils.initFragment(this, UpdateTransferMethodFragment.newInstance(
                    getIntent().getStringExtra(EXTRA_TRANSFER_METHOD_TOKEN)
            ), R.id.update_transfer_method_fragment);
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
    public void retry() {
        UpdateTransferMethodFragment fragment = getUpdateTransferFragment();
        switch (mRetryCode) {
            case RETRY_SHOW_ERROR_UPDATE_TRANSFER_METHOD:
                fragment.retryUpdateTransferMethod();
                break;
            case RETRY_SHOW_ERROR_LOAD_TMC_FIELDS:
                fragment.reloadTransferMethodConfigurationFields();
                break;
            default: // no default action
        }
    }

    private UpdateTransferMethodFragment getUpdateTransferFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        UpdateTransferMethodFragment fragment = (UpdateTransferMethodFragment)
                fragmentManager.findFragmentById(R.id.update_transfer_method_fragment);

        if (fragment == null) {
            fragment = UpdateTransferMethodFragment.newInstance(
                    getIntent().getStringExtra(EXTRA_TRANSFER_METHOD_TOKEN));
        }
        return fragment;
    }

    @Override
    public void showErrorsLoadTransferMethodConfigurationFields(@NonNull List<Error> errors) {
        mRetryCode = RETRY_SHOW_ERROR_LOAD_TMC_FIELDS;
        ActivityUtils.showError(this, TAG, PageGroups.TRANSFER_METHOD, errors);
    }

    @Override
    public void showErrorsUpdateTransferMethod(@NonNull List<Error> errors) {
        mRetryCode = RETRY_SHOW_ERROR_UPDATE_TRANSFER_METHOD;
        ActivityUtils.showError(this, TAG, PageGroups.TRANSFER_METHOD, errors);
    }

    @Override
    public void setSelectedDateField(@NonNull String fieldName, String selectedValue) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        UpdateTransferMethodFragment updateTransferMethodFragment =
                (UpdateTransferMethodFragment) fragmentManager.findFragmentById(R.id.update_transfer_method_fragment);
        if (updateTransferMethodFragment != null) {
            updateTransferMethodFragment.onDateSelected(selectedValue, fieldName);
        }
    }

    @Override
    public void onWidgetSelectionItemClicked(@NonNull String selectedValue, @NonNull String fieldName) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        UpdateTransferMethodFragment updateTransferMethodFragment =
                (UpdateTransferMethodFragment) fragmentManager.findFragmentById(R.id.update_transfer_method_fragment);
        updateTransferMethodFragment.onWidgetSelectionItemClicked(selectedValue, fieldName);

        WidgetSelectionDialogFragment widgetSelectionDialogFragment =
                (WidgetSelectionDialogFragment) fragmentManager.findFragmentById(android.R.id.content);
        widgetSelectionDialogFragment.dismiss();
        getSupportFragmentManager().popBackStack(WidgetSelectionDialogFragment.TAG,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
