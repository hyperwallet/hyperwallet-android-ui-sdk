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
package com.hyperwallet.android.ui.transfer.view;

import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getStringFontIcon;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getStringResourceByName;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getTransferMethodDetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.snackbar.Snackbar;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.viewmodel.CreateTransferViewModel;

import java.util.Locale;


/**
 * Create Transfer Fragment
 */
public class CreateTransferFragment extends Fragment {

    private View mProgressBar;
    private CreateTransferViewModel mCreateTransferViewModel;
    private EditText mTransferAmount;
    private TextView mTransferCurrency;
    private TextView mTransferAllFundsSummary;
    private EditText mTransferNotes;
    private Button mTransferNextButton;
    private View mTransferNextButtonProgress;
    private View mTransferDestination;
    private View mAddTransferDestination;

    /**
     * Please don't use this constructor this is reserved for Android Core Framework
     *
     * @see #newInstance()
     */
    public CreateTransferFragment() {
    }

    static CreateTransferFragment newInstance() {
        return new CreateTransferFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCreateTransferViewModel = ViewModelProviders.of(requireActivity()).get(CreateTransferViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_transfer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressBar = view.findViewById(R.id.progress_bar);
        mTransferAmount = view.findViewById(R.id.transfer_amount);
        mTransferCurrency = view.findViewById(R.id.transfer_amount_currency);
        mTransferAllFundsSummary = view.findViewById(R.id.transfer_summary_label);
        mTransferNotes = view.findViewById(R.id.transfer_notes);
        mTransferNextButtonProgress = view.findViewById(R.id.transfer_action_button_progress_bar);

        mTransferNextButton = view.findViewById(R.id.transfer_action_button);
        mTransferNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO temporary next action
                Snackbar.make(mTransferNextButton, "Create Transfer Next Button Clicked", Snackbar.LENGTH_SHORT).show();
            }
        });

        mTransferDestination = view.findViewById(R.id.transfer_destination);
        mTransferDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO temporary action for transfer destination clicked; it should open up transfer method list UI
                Snackbar.make(mTransferDestination, "LIST Transfer clicked", Snackbar.LENGTH_SHORT).show();
            }
        });

        mAddTransferDestination = view.findViewById(R.id.add_transfer_destination);
        mAddTransferDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO temporary action for transfer destination clicked; it should open up add transfer method UI
                Snackbar.make(mTransferDestination, "ADD Transfer clicked", Snackbar.LENGTH_SHORT).show();
            }
        });

        Switch transferAllSwitch = view.findViewById(R.id.switchButton);
        transferAllSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCreateTransferViewModel.setTransferAllAvailableFunds(isChecked);
            }
        });
        registerObserver();
    }

    private void registerObserver() {
        mCreateTransferViewModel.isLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(final Boolean loading) {
                if (loading) {
                    mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });

        mCreateTransferViewModel.getQuoteAvailableFunds().observe(getViewLifecycleOwner(), new Observer<Transfer>() {
            @Override
            public void onChanged(final Transfer transfer) {
                if (transfer != null) {
                    String summary = requireContext().getString(R.string.transfer_summary_label,
                            transfer.getDestinationAmount(), transfer.getDestinationCurrency());
                    mTransferAllFundsSummary.setText(summary);
                }
            }
        });

        mCreateTransferViewModel.getTransferDestination().observe(getViewLifecycleOwner(),
                new Observer<HyperwalletTransferMethod>() {
                    @Override
                    public void onChanged(final HyperwalletTransferMethod transferMethod) {
                        if (transferMethod != null) {
                            showTransferDestination(transferMethod);
                        } else {
                            mAddTransferDestination.setVisibility(View.VISIBLE);
                            mTransferDestination.setVisibility(View.GONE);
                        }
                    }
                });

        mCreateTransferViewModel.isTransferAllAvailableFunds().observe(getViewLifecycleOwner(),
                new Observer<Boolean>() {
                    @Override
                    public void onChanged(final Boolean transferAll) {
                        if (transferAll) {
                            // TODO update transfer all ui; lock transfer amount; copy full allowable transfer to
                            //  amount; enable next button
                        } else {
                            // TODO update transfer all ui; unlock transfer amount; reset amount to empty; disable
                            //  next button
                        }
                    }
                });
    }

    private void showTransferDestination(@NonNull final HyperwalletTransferMethod transferMethod) {
        TextView transferIcon = getView().findViewById(R.id.transfer_destination_icon);
        TextView transferTitle = getView().findViewById(R.id.transfer_destination_title);
        TextView transferCountry = getView().findViewById(R.id.transfer_destination_description_1);
        TextView transferIdentifier = getView().findViewById(R.id.transfer_destination_description_2);

        String type = transferMethod.getField(TYPE);
        String transferMethodIdentification = getTransferMethodDetail(transferIdentifier.getContext(), transferMethod,
                type);
        Locale locale = new Locale.Builder().setRegion(
                transferMethod.getField(TRANSFER_METHOD_COUNTRY)).build();

        transferIdentifier.setText(transferMethodIdentification);
        transferTitle.setText(getStringResourceByName(transferTitle.getContext(), type));
        transferIcon.setText(getStringFontIcon(transferIcon.getContext(), type));
        transferCountry.setText(locale.getDisplayName());

        mTransferCurrency.setText(transferMethod.getField(TRANSFER_METHOD_CURRENCY));
        mTransferDestination.setVisibility(View.VISIBLE);
    }
}
