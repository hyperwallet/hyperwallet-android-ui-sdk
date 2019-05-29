package com.hyperwallet.android.transaction_history.view;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.hyperwallet_transactionhistory.R;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.transaction_history.viewmodel.ReceiptDetailViewModel;

public class ReceiptDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECEIPT = "EXTRA_RECEIPT";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_detail);
        ReceiptDetailViewModel receiptDetailViewModel = ViewModelProviders.of(this).get(ReceiptDetailViewModel.class);
        receiptDetailViewModel.setHyperwalletTransferMethod((HyperwalletTransferMethod) getIntent().getParcelableExtra(EXTRA_RECEIPT));
        if (savedInstanceState == null) {
            initFragment(ReceiptDetailFragment.getInstance());
        }
    }


    private void initFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.receipt_detail_fragment, fragment);
        fragmentTransaction.commit();
    }
}
