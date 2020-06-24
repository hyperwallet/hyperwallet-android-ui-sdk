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

import static android.app.Activity.RESULT_OK;

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.ADD_TRANSFER_METHOD_REQUEST_CODE;
import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.SELECT_TRANSFER_DESTINATION_REQUEST_CODE;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getStringFontIcon;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getStringResourceByName;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getTransferMethodDetail;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputLayout;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.transfer.Transfer;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.ui.common.intent.HyperwalletIntent;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.common.view.OneClickListener;
import com.hyperwallet.android.ui.transfer.R;
import com.hyperwallet.android.ui.transfer.viewmodel.CreateTransferViewModel;

import java.util.Currency;
import java.util.Locale;


/**
 * Create Transfer Fragment
 */
public class CreateTransferFragment extends Fragment {

    private static final String EMPTY_STRING = "";
    private View mProgressBar;
    private CreateTransferViewModel mCreateTransferViewModel;
    private EditText mTransferAmount;
    private TextView mTransferCurrency;
    private TextView mTransferCurrencyCode;
    private TextView mTransferAllFundsSummary;
    private EditText mTransferNotes;
    private View mTransferNextButtonProgress;
    private View mTransferDestination;
    private View mAddTransferDestination;
    private TextInputLayout mTransferAmountLayout;
    private View mTransferHeaderContainerError;
    private TextView mTransferDestinationError;

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
        mTransferAllFundsSummary = view.findViewById(R.id.transfer_summary);
        mTransferNextButtonProgress = view.findViewById(R.id.transfer_action_button_progress_bar);

        mTransferCurrency = view.findViewById(R.id.transfer_amount_currency);
        mTransferCurrency.setText(EMPTY_STRING);
        mTransferCurrencyCode = view.findViewById(R.id.transfer_amount_currency_code);
        mTransferCurrencyCode.setText(EMPTY_STRING);

        TextView transferAllFunds = view.findViewById(R.id.transfer_all_funds);
        transferAllFunds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCreateTransferViewModel.setTransferAllAvailableFunds(true);
            }
        });

        // next button
        Button continueButton = view.findViewById(R.id.transfer_action_button);
        continueButton.setOnClickListener(new OneClickListener() {
            @Override
            public void onOneClick(View v) {
                if (isCreateTransferValid()) {
                    mCreateTransferViewModel.createTransfer();
                }
            }
        });

        // transfer amount
        mTransferAmountLayout = view.findViewById(R.id.transfer_amount_layout);
        mTransferAmount = view.findViewById(R.id.transfer_amount);
        prepareTransferAmount();

        // transfer notes;
        mTransferNotes = view.findViewById(R.id.transfer_notes);
        prepareTransferNotes();

        // transfer destination
        mTransferDestination = view.findViewById(R.id.transfer_destination);
        mTransferDestination.setOnClickListener(new OneClickListener() {
            @Override
            public void onOneClick(View v) {
                TransferMethod activeDestination =
                        mCreateTransferViewModel.getTransferDestination().getValue();
                Intent intent = new Intent(requireContext(), ListTransferDestinationActivity.class);
                intent.putExtra(ListTransferDestinationActivity.EXTRA_SELECTED_DESTINATION_TOKEN,
                        activeDestination.getField(TOKEN));
                startActivityForResult(intent, SELECT_TRANSFER_DESTINATION_REQUEST_CODE);
            }
        });

        // add transfer destination
        mTransferDestinationError = view.findViewById(R.id.transfer_destination_error);
        mTransferHeaderContainerError = view.findViewById(R.id.transfer_header_container_with_error);
        mAddTransferDestination = view.findViewById(R.id.add_transfer_destination);
        mAddTransferDestination.setOnClickListener(new OneClickListener() {
            @Override
            public void onOneClick(View v) {
                Intent intent = new Intent();
                intent.setAction(HyperwalletIntent.ACTION_SELECT_TRANSFER_METHOD);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                    startActivityForResult(intent, ADD_TRANSFER_METHOD_REQUEST_CODE);
                } else {
                    mCreateTransferViewModel.notifyModuleUnavailable();
                }
            }
        });

        registerTransferDestinationObservers();
        registerAvailableFundsObserver();
        registerTransferAmountObservers();
        registerErrorObservers();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCreateTransferViewModel.init(getResources().getString(R.string.defaultTransferAmount));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_TRANSFER_DESTINATION_REQUEST_CODE && data != null) {
                TransferMethod selectedTransferMethod = data.getParcelableExtra(
                        ListTransferDestinationActivity.EXTRA_SELECTED_DESTINATION_TOKEN);
                mCreateTransferViewModel.setTransferAmount(null);
                mCreateTransferViewModel.setTransferNotes(null);
                mCreateTransferViewModel.setTransferAllAvailableFunds(Boolean.FALSE);
                mCreateTransferViewModel.setTransferDestination(selectedTransferMethod);
            } else if (requestCode == ADD_TRANSFER_METHOD_REQUEST_CODE) {
                mCreateTransferViewModel.refreshTransferDestination();
            }
        }
    }

    void retry() {
        mCreateTransferViewModel.retry();
    }

    void reApplyFieldRules() {
        if (mCreateTransferViewModel.isTransferAllAvailableFunds().getValue()) {
            mTransferAmount.setEnabled(false);
            mTransferCurrency.setTextColor(getResources().getColor(R.color.colorButtonTextDisabled));
        }
    }

    private boolean isCreateTransferValid() {
        if (!isValidAmount(mCreateTransferViewModel.getTransferAmount().getValue())) {
            mTransferAmountLayout.setError(requireContext().getString(R.string.transferAmountInvalid));
            return false;
        }

        if (mCreateTransferViewModel.isTransferDestinationUnknown()) {
            mTransferHeaderContainerError.setVisibility(View.VISIBLE);
            return false;
        }

        return true;
    }

    private boolean isValidAmount(final String amount) {
        if (TextUtils.isEmpty(amount)) {
            return false;
        }

        if (getResources().getString(R.string.defaultTransferAmount).equals(amount)) {
            return false;
        }

        try {
            Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
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
                    mTransferAmountLayout.setError(null);
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

    private void registerErrorObservers() {
        mCreateTransferViewModel.getInvalidAmountError().observe(getViewLifecycleOwner(),
                new Observer<Event<Error>>() {
                    @Override
                    public void onChanged(@NonNull final Event<Error> event) {
                        if (!event.isContentConsumed()) {
                            mTransferAmountLayout.setError(event.getContent().getMessage());
                        }
                    }
                });

        mCreateTransferViewModel.getInvalidDestinationError().observe(getViewLifecycleOwner(),
                new Observer<Event<Error>>() {
                    @Override
                    public void onChanged(@NonNull final Event<Error> event) {
                        if (!event.isContentConsumed()) {
                            mTransferHeaderContainerError.setVisibility(View.VISIBLE);
                            mTransferDestinationError.setText(event.getContent().getMessage());
                        }
                    }
                });
    }

    private void registerTransferDestinationObservers() {
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
                new Observer<TransferMethod>() {
                    @Override
                    public void onChanged(final TransferMethod transferMethod) {
                        if (transferMethod != null) {
                            mAddTransferDestination.setVisibility(View.GONE);
                            mTransferHeaderContainerError.setVisibility(View.GONE);
                            mTransferDestination.setVisibility(View.VISIBLE);
                            showTransferDestination(transferMethod);
                            enableInputControls();
                        } else {
                            mTransferDestination.setVisibility(View.GONE);
                            mAddTransferDestination.setVisibility(View.VISIBLE);
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
                    String summary = requireContext().getString(R.string.mobileAvailableBalance,
                            transfer.getDestinationAmount(), transfer.getDestinationCurrency());
                    mTransferAllFundsSummary.setText(summary);
                    mTransferAllFundsSummary.setVisibility(View.VISIBLE);
                } else {
                    mTransferAllFundsSummary.setVisibility(View.GONE);
                }
            }
        });
    }

    private void registerTransferAmountObservers() {
        mCreateTransferViewModel.isTransferAllAvailableFunds().observe(getViewLifecycleOwner(),
                new Observer<Boolean>() {
                    @Override
                    public void onChanged(final Boolean transferAll) {
                        if (transferAll) {
                            Transfer transfer = mCreateTransferViewModel.getQuoteAvailableFunds().getValue();
                            if (transfer != null) {
                                mTransferCurrency.setTextColor(
                                        getResources().getColor(R.color.colorButtonTextDisabled));
                                mTransferAmount.getText().clear();
                                mTransferAmount.setText(transfer.getDestinationAmount());
                            }
                        } else {
                            mTransferCurrency.setTextColor(getResources().getColor(R.color.colorSecondaryDark));
                            mTransferAmount.setText(null);
                        }
                    }
                });

        mCreateTransferViewModel.getTransferAmount().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String amount) {
                String workingAmount = hardPlaceholder(amount);
                mTransferAmount.setText(TextUtils.isEmpty(workingAmount) ?
                        getResources().getText(R.string.defaultTransferAmount) : workingAmount);
                mTransferAmount.setSelection(TextUtils.isEmpty(workingAmount) ? 1 : workingAmount.length());
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
    }

    private String hardPlaceholder(final String amount) {
        if (amount != null && amount.startsWith(getResources().getString(R.string.defaultTransferAmount))) {
            return amount.substring(1);
        }
        return amount;
    }

    private void disableInputControls() {
        mTransferCurrency.setTextColor(getResources().getColor(R.color.colorButtonTextDisabled));
        mTransferAmount.setEnabled(false);
        mTransferNotes.setEnabled(false);
        mTransferDestination.setEnabled(false);
        mAddTransferDestination.setEnabled(false);
    }

    private void enableInputControls() {
        mTransferCurrency.setTextColor(getResources().getColor(R.color.colorSecondaryDark));
        mTransferAmount.setEnabled(true);
        mTransferNotes.setEnabled(true);
        mTransferDestination.setEnabled(true);
        mAddTransferDestination.setEnabled(true);
    }

    private void showTransferDestination(@NonNull final TransferMethod transferMethod) {
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
        mTransferCurrencyCode.setText(
                Currency.getInstance(transferMethod.getField(TRANSFER_METHOD_CURRENCY)).getSymbol(Locale.ROOT));
        mTransferDestination.setVisibility(View.VISIBLE);
    }
}
