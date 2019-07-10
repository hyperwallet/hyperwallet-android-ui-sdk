package com.hyperwallet.android.ui.transfermethod;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.viewmodel.Event;
import com.hyperwallet.android.ui.repository.RepositoryFactory;
import com.hyperwallet.android.ui.viewmodel.ListTransferDestinationViewModel;

public class ListTransferDestinationActivity extends AppCompatActivity {

    private ListTransferDestinationViewModel mTransferMethodSelectorViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_method_selector);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_list_transfer_method);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTransferMethodSelectorViewModel = ViewModelProviders.of(this,
                new ListTransferDestinationViewModel.ListTransferDestinationViewModelFactory(
                        RepositoryFactory.getInstance().getTransferMethodRepository())).get(
                ListTransferDestinationViewModel.class);

        mTransferMethodSelectorViewModel.getTransferMethodSelection().observe(this,
                new Observer<Event<HyperwalletTransferMethod>>() {
                    @Override
                    public void onChanged(Event<HyperwalletTransferMethod> event) {
                        if (!event.isContentConsumed()) {
                            selectTransferMethod(event.getContent());
                        }
                    }
                });
        if (savedInstanceState == null) {
            initFragment(ListTransferDestinationFragment.newInstance());
        }
    }


    private void selectTransferMethod(@NonNull final HyperwalletTransferMethod transferMethod) {
        Intent intent = new Intent();
        String token = transferMethod.getField(HyperwalletTransferMethod.TransferMethodFields.TOKEN);
        intent.putExtra("TOKEN", token);
        setResult(RESULT_OK, intent);
        finish();
    }


    private void initFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.list_transfer_method_fragment, fragment);
        fragmentTransaction.commit();
    }

}
