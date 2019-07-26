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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.viewmodel.ListTransferDestinationViewModel;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;

public class ListTransferDestinationActivity extends AppCompatActivity implements SelectDestinationItemClickListener {

    public static final String ARGUMENT_SELECTED_DESTINATION = "argument_selected_destination";
    private static final String TAG = ListTransferDestinationActivity.class.getSimpleName();

    private ListTransferDestinationViewModel mListTransferDestinationViewModel;

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
            initFragment(ListTransferDestinationFragment.newInstance(
                    (HyperwalletTransferMethod) getIntent().getParcelableExtra(ARGUMENT_SELECTED_DESTINATION)));
        }

//        mListTransferDestinationViewModel = ViewModelProviders.of(this).get(
//                ListTransferDestinationViewModel.class);

        mListTransferDestinationViewModel = ViewModelProviders.of(this, new ListTransferDestinationViewModel
                .ListTransferDestinationViewModelFactory(
                TransferMethodRepositoryFactory.getInstance().getTransferMethodRepository())).get(
                ListTransferDestinationViewModel.class);

        registerObservers();
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

    private void registerObservers() {
        mListTransferDestinationViewModel.getTransferDestinationSection().observe(this,
                new Observer<Event<HyperwalletTransferMethod>>() {
                    @Override
                    public void onChanged(Event<HyperwalletTransferMethod> destination) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(ARGUMENT_SELECTED_DESTINATION, destination.getContent());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                });
    }

    @Override
    public void selectTransferDestination(int position) {

    }
}
