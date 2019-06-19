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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.ui.common.view.error.DefaultErrorDialogFragment;
import com.hyperwallet.android.ui.common.view.error.OnNetworkErrorCallback;
import com.hyperwallet.android.ui.common.viewmodel.Event;
import com.hyperwallet.android.ui.common.viewmodel.ListDetailNavigator;
import com.hyperwallet.android.ui.receipt.R;
import com.hyperwallet.android.ui.receipt.repository.UserReceiptRepositoryFactory;
import com.hyperwallet.android.ui.receipt.viewmodel.ListUserReceiptViewModel;

import java.util.List;

public class ListUserReceiptActivity extends AppCompatActivity implements OnNetworkErrorCallback,
        ListDetailNavigator<Event<Receipt>> {

    private ListUserReceiptViewModel mListUserReceiptViewModel;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user_receipt);

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

        UserReceiptRepositoryFactory factory = UserReceiptRepositoryFactory.getInstance();
        mListUserReceiptViewModel = ViewModelProviders.of(this, new ListUserReceiptViewModel
                .ListReceiptViewModelFactory(factory.getUserReceiptRepository()))
                .get(ListUserReceiptViewModel.class);

        mListUserReceiptViewModel.getReceiptErrors().observe(this, new Observer<Event<HyperwalletErrors>>() {
            @Override
            public void onChanged(Event<HyperwalletErrors> event) {
                if (event != null && !event.isContentConsumed()) {
                    showErrorOnLoadReceipt(event.getContent().getErrors());
                }
            }
        });

        mListUserReceiptViewModel.getDetailNavigation().observe(this, new Observer<Event<Receipt>>() {
            @Override
            public void onChanged(Event<Receipt> event) {
                navigate(event);
            }
        });

        if (savedInstanceState == null) {
            initFragment(ListReceiptFragment.newInstance());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserReceiptRepositoryFactory.clearInstance();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initFragment(@NonNull final Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.list_receipt_fragment, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void retry() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ListReceiptFragment fragment = (ListReceiptFragment)
                fragmentManager.findFragmentById(R.id.list_receipt_fragment);

        if (fragment == null) {
            fragment = ListReceiptFragment.newInstance();
        }
        fragment.retry();
    }

    private void showErrorOnLoadReceipt(@NonNull final List<HyperwalletError> errors) {
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

    @Override
    public void navigate(@NonNull final Event<Receipt> event) {
        if (!event.isContentConsumed()) {
            Intent intent = new Intent(this, ReceiptDetailActivity.class);
            intent.putExtra(ReceiptDetailActivity.EXTRA_RECEIPT, event.getContent());
            startActivity(intent);
        }
    }
}
