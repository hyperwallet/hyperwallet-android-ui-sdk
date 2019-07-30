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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

    private static final String EMPTY_STRING = "";
    private static final String ZERO_AMOUNT = "0.00";
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
    private Switch mTransferAllSwitch;

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

        mTransferCurrency = view.findViewById(R.id.transfer_amount_currency);
        mTransferCurrency.setText(EMPTY_STRING);

        mTransferAllFundsSummary = view.findViewById(R.id.transfer_summary);
        String defaultSummary = requireContext().getString(R.string.transfer_summary_label, ZERO_AMOUNT, EMPTY_STRING);
        mTransferAllFundsSummary.setText(defaultSummary);

        mTransferNextButtonProgress = view.findViewById(R.id.transfer_action_button_progress_bar);

        // next button
        mTransferNextButton = view.findViewById(R.id.transfer_action_button);
        mTransferNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCreateTransferViewModel.createTransfer();
            }
        });
        disableNextButton();

        // transfer amount
        mTransferAmount = view.findViewById(R.id.transfer_amount);
        prepareTransferAmount();

        // transfer notes;
        mTransferNotes = view.findViewById(R.id.transfer_notes);
        prepareTransferNotes();

        // transfer destination
        mTransferDestination = view.findViewById(R.id.transfer_destination);
        mTransferDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO temporary action for transfer destination clicked; it should open up transfer method list UI
                Snackbar.make(mTransferDestination, "LIST Transfer clicked", Snackbar.LENGTH_SHORT).show();
            }
        });

        // add transfer destination
        mAddTransferDestination = view.findViewById(R.id.add_transfer_destination);
        mAddTransferDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO temporary action for transfer destination clicked; it should open up add transfer method UI
                Snackbar.make(mAddTransferDestination, "ADD Transfer clicked", Snackbar.LENGTH_SHORT).show();
            }
        });

        // toggle button
        mTransferAllSwitch = view.findViewById(R.id.switchButton);
        mTransferAllSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCreateTransferViewModel.setTransferAllAvailableFunds(isChecked);
            }
        });
        registerTransferDestinationObserver();
        registerAvailableFundsObserver();
        registerTransferAmountObserver();
    }

    private void prepareTransferAmount() {
        mTransferAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mCreateTransferViewModel.setTransferAmount(((EditText) v).getText().toString());
                }
            }
        });
        mTransferAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (before != count) {
                    mCreateTransferViewModel.setTransferAmount(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void prepareTransferNotes() {
        mTransferNotes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mCreateTransferViewModel.setTransferNotes(((EditText) v).getText().toString());
                }
            }
        });
        mTransferNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (before != count) {
                    mCreateTransferViewModel.setTransferNotes(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void registerTransferDestinationObserver() {
        mCreateTransferViewModel.isLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(final Boolean loading) {
                if (loading) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    disableInputControls();
                } else {
                    mProgressBar.setVisibility(View.GONE);
                    enableInputControls();
                }
            }
        });

        mCreateTransferViewModel.getTransferDestination().observe(getViewLifecycleOwner(),
                new Observer<HyperwalletTransferMethod>() {
                    @Override
                    public void onChanged(final HyperwalletTransferMethod transferMethod) {
                        if (transferMethod != null) {
                            showTransferDestination(transferMethod);
                            enableInputControls();
                        } else {
                            mAddTransferDestination.setVisibility(View.VISIBLE);
                            mTransferDestination.setVisibility(View.GONE);
                            disableInputControls();
                        }
                    }
                });
    }

    private void registerAvailableFundsObserver() {
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
    }

    private void registerTransferAmountObserver() {
        mCreateTransferViewModel.isTransferAllAvailableFunds().observe(getViewLifecycleOwner(),
                new Observer<Boolean>() {
                    @Override
                    public void onChanged(final Boolean transferAll) {
                        if (transferAll) {
                            Transfer transfer = mCreateTransferViewModel.getQuoteAvailableFunds().getValue();
                            if (transfer != null) {
                                mTransferCurrency.setTextColor(
                                        getResources().getColor(R.color.colorButtonTextDisabled));
                                mTransferAmount.setEnabled(false);
                                mTransferAmount.setText(transfer.getDestinationAmount());
                                enableNextButton();
                            }
                        } else {
                            mTransferCurrency.setTextColor(getResources().getColor(R.color.colorSecondaryDark));
                            mTransferAmount.setEnabled(true);
                            mTransferAmount.setText(null);
                            disableNextButton();
                        }
                    }
                });

        mCreateTransferViewModel.getTransferAmount().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String amount) {
                mTransferAmount.setText(amount);
                mTransferAmount.setSelection(TextUtils.isEmpty(amount) ? 0 : amount.length());

                if (mCreateTransferViewModel.getTransferDestination().getValue() != null) {
                    enableNextButton();
                } else {
                    disableNextButton();
                }
            }
        });

        mCreateTransferViewModel.getTransferNotes().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String notes) {
                mTransferNotes.setText(notes);
                mTransferNotes.setSelection(TextUtils.isEmpty(notes) ? 0 : notes.length());
            }
        });

        mCreateTransferViewModel.isCreateQuoteLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                if (loading) {
                    mTransferNextButtonProgress.setVisibility(View.VISIBLE);
                    disableInputControls();
                } else {
                    mTransferNextButtonProgress.setVisibility(View.GONE);
                    enableInputControls();
                }
            }
        });

        mCreateTransferViewModel.getCreateTransfer().observe(getViewLifecycleOwner(), new Observer<Transfer>() {
            @Override
            public void onChanged(Transfer transfer) {
                // TODO create intent to call next Transfer confirmation screen with parcelable payload of Transfer
                try {
                    Snackbar.make(mTransferNextButton, transfer.toJsonString(), Snackbar.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Snackbar.make(mTransferNextButton, "Exception occurred! Transfer conversion",
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void disableInputControls() {
        mTransferAmount.setEnabled(false);
        mTransferNotes.setEnabled(false);
        mTransferAllSwitch.setEnabled(false);
        mTransferCurrency.setTextColor(getResources().getColor(R.color.colorButtonTextDisabled));
    }

    private void enableInputControls() {
        mTransferAmount.setEnabled(true);
        mTransferNotes.setEnabled(true);
        mTransferAllSwitch.setEnabled(true);
        mTransferCurrency.setTextColor(getResources().getColor(R.color.colorSecondaryDark));
    }

    private void enableNextButton() {
        mTransferNextButton.setEnabled(true);
        mTransferNextButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mTransferNextButton.setTextColor(getResources().getColor(R.color.regularColorPrimary));
    }

    private void disableNextButton() {
        mTransferNextButton.setEnabled(false);
        mTransferNextButton.setBackgroundColor(getResources().getColor(R.color.colorSecondaryDark));
        mTransferNextButton.setTextColor(getResources().getColor(R.color.colorButtonTextDisabled));
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
