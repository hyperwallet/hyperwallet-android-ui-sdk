package com.hyperwallet.android.transaction_history.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.common.view.error.DefaultErrorDialogFragment;
import com.hyperwallet.android.hyperwallet_transactionhistory.R;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.transaction_history.viewmodel.ListTransactionHistoryViewModel;

import java.util.List;


public class ListTransactionHistoryActivity extends AppCompatActivity implements
        ListTransactionHistoryFragment.OnLoadTransactionHistoryNetworkErrorCallback {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_transaction_history);
        ViewModelProviders.of(this).get(ListTransactionHistoryViewModel.class);
        if (savedInstanceState == null) {
            initFragment(ListTransactionHistoryFragment.newInstance());
        }
    }


    private void initFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.list_transfer_method_fragment, fragment);
        fragmentTransaction.commit();
    }


    @Override
    public void showErrorsLoadTransactionHistory(@NonNull List<HyperwalletError> errors) {
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
