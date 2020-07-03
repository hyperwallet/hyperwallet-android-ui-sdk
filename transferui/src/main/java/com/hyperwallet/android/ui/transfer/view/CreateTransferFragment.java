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

import static com.hyperwallet.android.model.transfer.Transfer.CURRENCY_NUMERIC_SEPARATOR;
import static com.hyperwallet.android.model.transfer.Transfer.EMPTY_STRING;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TOKEN;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_CURRENCY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.ADD_TRANSFER_METHOD_REQUEST_CODE;
import static com.hyperwallet.android.ui.common.intent.HyperwalletIntent.SELECT_TRANSFER_DESTINATION_REQUEST_CODE;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getStringFontIcon;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getStringResourceByName;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getTransferMethodDetail;

import android.content.Context;
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

    private static final String NOTES_TAG = "NOTES_TAGGED";
    private static final String ELLIPSIS = "...";
    private static final int NOTES_MAX_LINE_LENGTH = 40;

    private View mProgressBar;
    private CreateTransferViewModel mCreateTransferViewModel;
    private EditText mTransferAmount;
    private TextView mTransferCurrency;
    private TextView mTransferCurrencyCode;
    private TextView mTransferAllFundsSummary;
    private EditText mTransferNotes;
    private View mTransferDestination;
    private View mAddTransferDestination;
    private View mTransferHeaderContainerError;
    private TextView mTransferDestinationError;
    private View mTransferAmountErrorContainer;
    private TextView mTransferAmountError;

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
    public void onStop() {
        mCreateTransferViewModel.setCreateQuoteLoading(Boolean.FALSE);
        super.onStop();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressBar = view.findViewById(R.id.progress_bar);
        mTransferAllFundsSummary = view.findViewById(R.id.transfer_summary);

        mTransferCurrency = view.findViewById(R.id.transfer_amount_currency);
        mTransferCurrency.setText(EMPTY_STRING);
        mTransferCurrencyCode = view.findViewById(R.id.transfer_amount_currency_code);
        mTransferCurrencyCode.setText(EMPTY_STRING);

        View transferHeader = view.findViewById(R.id.transfer_funds_header_container);
        if (getActivity() instanceof CreateTransferActivity) {
            transferHeader.setVisibility(View.GONE);
        }

        // transfer all funds
        TextView transferAllFunds = view.findViewById(R.id.transfer_all_funds);
        transferAllFunds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCreateTransferViewModel.setTransferAllAvailableFunds(Boolean.TRUE);
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
        mTransferAmountErrorContainer = view.findViewById(R.id.transfer_amount_error_container);
        mTransferAmountError = view.findViewById(R.id.transfer_amount_error);
        mTransferAmount = view.findViewById(R.id.transfer_amount);
        mTransferAmount.requestFocus();
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

    private boolean isCreateTransferValid() {
        boolean valid = true;
        if (!isValidAmount(mCreateTransferViewModel.getTransferAmount().getValue())) {
            setAmountError(requireContext().getString(R.string.transferAmountInvalid));
            valid = false;
        }

        if (mCreateTransferViewModel.isTransferDestinationUnknown()) {
            mTransferHeaderContainerError.setVisibility(View.VISIBLE);
            valid = false;
        }

        return valid;
    }

    private boolean isValidAmount(final String amount) {
        if (TextUtils.isEmpty(amount)) {
            return false;
        }

        if (getResources().getString(R.string.defaultTransferAmount).equals(amount)) {
            return false;
        }

        try {
            Double.parseDouble(amount.replace(CURRENCY_NUMERIC_SEPARATOR, EMPTY_STRING));
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private void setAmountError(final String errorMessage) {
        if (TextUtils.isEmpty(errorMessage)) {
            mTransferAmountErrorContainer.setVisibility(View.INVISIBLE);
        } else {
            mTransferAmountErrorContainer.setVisibility(View.VISIBLE);
            mTransferAmountError.setText(errorMessage);
        }
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

            private static final int MAX_AMOUNT_WHOLE_NUMBER = 12;
            private static final int MAX_AMOUNT_DECIMAL = 2;
            private static final String AMOUNT_DECIMAL = ".";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (before != count) {
                    String amount = s.toString();
                    String[] comp = amount.split("\\.");
                    StringBuilder builder = new StringBuilder();

                    if (comp.length == 2) {
                        String whole = comp[0];
                        String decimal = comp[1];

                        if (whole.length() <= MAX_AMOUNT_WHOLE_NUMBER) {
                            builder.append(whole);
                        } else {
                            builder.append(whole.substring(0, MAX_AMOUNT_WHOLE_NUMBER));
                        }

                        if (decimal.length() <= MAX_AMOUNT_DECIMAL) {
                            builder.append(AMOUNT_DECIMAL).append(decimal);
                        } else {
                            builder.append(AMOUNT_DECIMAL).append(decimal.substring(0, MAX_AMOUNT_DECIMAL));
                        }
                    } else {
                        if (amount.contains(AMOUNT_DECIMAL)) {
                            if (amount.lastIndexOf(AMOUNT_DECIMAL) == (amount.length() - 1)
                                    && amount.length() <= MAX_AMOUNT_WHOLE_NUMBER + 1) {
                                builder.append(amount);
                            } else {
                                builder.append(amount.substring(0, MAX_AMOUNT_WHOLE_NUMBER + 1));
                            }
                        } else {
                            if (amount.length() <= MAX_AMOUNT_WHOLE_NUMBER) {
                                builder.append(amount);
                            } else {
                                builder.append(amount.substring(0, MAX_AMOUNT_WHOLE_NUMBER));
                            }
                        }
                    }

                    mCreateTransferViewModel.setTransferAmount(builder.toString());
                    setAmountError(null);
                    Context context = CreateTransferFragment.this.getActivity();

                    switch (builder.toString().length()) {
                        case 9:
                        case 10:
                        case 11: // stage one dimension changes
                            mTransferCurrencyCode.setPadding(
                                    context.getResources().getDimensionPixelSize(R.dimen.amount_padding),
                                    context.getResources().getDimensionPixelSize(R.dimen.currency_code_padding_2),
                                    context.getResources().getDimensionPixelSize(R.dimen.amount_padding), 0);
                            mTransferCurrency.setPadding(0, 0, 0,
                                    context.getResources().getDimensionPixelSize(R.dimen.currency_padding_2));
                            mTransferCurrencyCode.setTextSize(22);
                            mTransferAmount.setTextSize(50);
                            break;
                        case 12:
                        case 13:
                        case 14:
                        case 15: // stage two dimension changes
                            mTransferCurrencyCode.setPadding(
                                    context.getResources().getDimensionPixelSize(R.dimen.amount_padding),
                                    context.getResources().getDimensionPixelSize(R.dimen.currency_code_padding_3),
                                    context.getResources().getDimensionPixelSize(R.dimen.amount_padding), 0);
                            mTransferCurrency.setPadding(0, 0, 0,
                                    context.getResources().getDimensionPixelSize(R.dimen.currency_padding_3));
                            mTransferCurrencyCode.setTextSize(15);
                            mTransferAmount.setTextSize(35);
                            mTransferCurrency.setTextSize(14);
                            break;
                        default: // back to original dimensions
                            mTransferCurrencyCode.setPadding(
                                    context.getResources().getDimensionPixelSize(R.dimen.amount_padding),
                                    context.getResources().getDimensionPixelSize(R.dimen.currency_code_padding),
                                    context.getResources().getDimensionPixelSize(R.dimen.amount_padding), 0);
                            mTransferCurrency.setPadding(0, 0, 0,
                                    context.getResources().getDimensionPixelSize(R.dimen.currency_padding));
                            mTransferCurrencyCode.setTextSize(26);
                            mTransferAmount.setTextSize(60);
                            mTransferCurrency.setTextSize(17);
                    }
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
                if (hasFocus) {
                    if (mTransferNotes.getTag() != null && mTransferNotes.getTag().toString().equals(NOTES_TAG)) {
                        mTransferNotes.setTag(null);
                    }
                    mTransferNotes.setText(mCreateTransferViewModel.getTransferNotes().getValue());
                } else {
                    mCreateTransferViewModel.setTransferNotes(((EditText) v).getText().toString());
                    if (mCreateTransferViewModel.getTransferNotes().getValue() != null
                            && mCreateTransferViewModel.getTransferNotes().getValue().length()
                            > NOTES_MAX_LINE_LENGTH) {
                        mTransferNotes.setTag(NOTES_TAG);
                    } else {
                        mTransferNotes.setTag(null);
                    }
                }
            }
        });

        mTransferNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (before != count && mTransferNotes.getTag() == null) {
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
                            setAmountError(event.getContent().getMessage());
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
                            mTransferAmount.setText(mCreateTransferViewModel.getTransferAmount().getValue());
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
                if (mTransferNotes.getTag() != null && mTransferNotes.getTag().toString().equals(NOTES_TAG)) {
                    if (!TextUtils.isEmpty(notes)) {
                        String withEllipsis = notes.substring(0, NOTES_MAX_LINE_LENGTH - ELLIPSIS.length()) + ELLIPSIS;
                        mTransferNotes.setText(withEllipsis);
                    } else {
                        mTransferNotes.setTag(null); // looks like this is reset before it being focused
                    }
                } else {
                    mTransferNotes.setText(notes);
                    mTransferNotes.setSelection(TextUtils.isEmpty(notes) ? 0 : notes.length());
                }
            }
        });

        mCreateTransferViewModel.isCreateQuoteLoading().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean loading) {
                if (loading) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    disableInputControls();
                } else {
                    mProgressBar.setVisibility(View.GONE);
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
                Currency.getInstance(transferMethod.getField(TRANSFER_METHOD_CURRENCY)).getSymbol(Locale.getDefault()));
        mTransferDestination.setVisibility(View.VISIBLE);
    }
}
