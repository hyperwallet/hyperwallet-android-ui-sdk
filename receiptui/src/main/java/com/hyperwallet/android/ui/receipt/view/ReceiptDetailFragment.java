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

import static android.text.format.DateUtils.FORMAT_ABBREV_WEEKDAY;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;
import static android.text.format.DateUtils.FORMAT_SHOW_YEAR;
import static android.text.format.DateUtils.formatDateTime;

import static com.hyperwallet.android.model.receipt.Receipt.Entries.CREDIT;
import static com.hyperwallet.android.model.receipt.Receipt.Entries.DEBIT;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.model.receipt.ReceiptDetails;
import com.hyperwallet.android.ui.common.util.DateUtils;
import com.hyperwallet.android.ui.receipt.R;
import com.hyperwallet.android.ui.receipt.viewmodel.ReceiptDetailViewModel;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;

public class ReceiptDetailFragment extends Fragment {

    private static final String AMOUNT_FORMAT = "###0.00";
    private static final String DETAIL_TIMEZONE = "zzz";

    private ReceiptDetailViewModel mReceiptDetailViewModel;

    public ReceiptDetailFragment() {
    }

    public static ReceiptDetailFragment newInstance() {
        ReceiptDetailFragment fragment = new ReceiptDetailFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiptDetailViewModel = ViewModelProviders.of(requireActivity()).get(ReceiptDetailViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receipt_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onView();

        Receipt receipt = mReceiptDetailViewModel.getReceipt();

        // transactions
        setTransactionView(receipt, view);

        // receipt details
        setDetailsView(receipt, view);

        // fee details
        setFeeDetailsView(receipt, view);
    }

    private void onView() {
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getActivity().getWindow().setStatusBarColor(
                    ContextCompat.getColor(getContext(), R.color.statusBarColor));
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    // By design decision, this code is also repeated in ListReceiptFragment
    private void setTransactionView(@NonNull final Receipt receipt, @NonNull final View view) {
        TextView transactionTypeIcon = view.findViewById(R.id.transaction_type_icon);
        TextView transactionTitle = view.findViewById(R.id.transaction_title);
        TextView transactionDate = view.findViewById(R.id.transaction_date);
        TextView transactionAmount = view.findViewById(R.id.transaction_amount);
        TextView transactionCurrency = view.findViewById(R.id.transaction_currency);

        //TODO localization of currencies in consideration
        DecimalFormat decimalFormat = new DecimalFormat(AMOUNT_FORMAT);
        double amount = Double.parseDouble(receipt.getAmount());
        String formattedAmount = decimalFormat.format(amount);

        if (CREDIT.equals(receipt.getEntry())) {
            transactionAmount.setTextColor(transactionAmount.getContext()
                    .getResources().getColor(R.color.positiveColor));
            transactionAmount.setText(transactionAmount.getContext()
                    .getString(R.string.credit_sign, formattedAmount));
            transactionTypeIcon.setTextColor(transactionTypeIcon.getContext()
                    .getResources().getColor(R.color.positiveColor));
            transactionTypeIcon.setText(transactionTypeIcon.getContext().getText(R.string.credit));
        } else if (DEBIT.equals(receipt.getEntry())) {
            transactionAmount.setTextColor(transactionAmount.getContext()
                    .getResources().getColor(R.color.negativeColor));
            transactionAmount.setText(transactionAmount.getContext()
                    .getString(R.string.debit_sign, formattedAmount));
            transactionTypeIcon.setTextColor(transactionTypeIcon.getContext()
                    .getResources().getColor(R.color.negativeColor));
            transactionTypeIcon.setText(transactionTypeIcon.getContext().getText(R.string.debit));
        }

        transactionCurrency.setText(receipt.getCurrency());
        transactionTitle.setText(getTransactionTitle(receipt.getType(), transactionTitle.getContext()));
        Date date = DateUtils.fromDateTimeString(receipt.getCreatedOn());
        transactionDate.setText(formatDateTime(view.getContext(), date.getTime(),
                FORMAT_SHOW_DATE | FORMAT_SHOW_YEAR));
    }

    private String getTransactionTitle(@NonNull final String receiptType, @NonNull final Context context) {
        String showTitle = context.getResources().getString(R.string.unknown_type);
        int resourceId = context.getResources().getIdentifier(receiptType.toLowerCase(Locale.ROOT), "string",
                context.getPackageName());
        if (resourceId != 0) {
            showTitle = context.getResources().getString(resourceId);
        }

        return showTitle;
    }

    private void setFeeDetailsView(@NonNull final Receipt receipt, @NonNull final View view) {
        if (!TextUtils.isEmpty(receipt.getFee())) {
            view.findViewById(R.id.fee_details_layout).setVisibility(View.VISIBLE);
            double feeAmount = Double.parseDouble(receipt.getFee());
            double amount = Double.parseDouble(receipt.getAmount());
            double transferAmount = amount - feeAmount;

            //TODO localization of currencies in consideration
            DecimalFormat decimalFormat = new DecimalFormat(AMOUNT_FORMAT);

            TextView amountView = view.findViewById(R.id.details_amount_value);
            amountView.setText(view.getContext().getString(R.string.concat_string_view_format,
                    decimalFormat.format(amount), receipt.getCurrency()));

            TextView fee = view.findViewById(R.id.details_fee_value);
            fee.setText(view.getContext().getString(R.string.concat_string_view_format,
                    decimalFormat.format(feeAmount), receipt.getCurrency()));

            TextView transfer = view.findViewById(R.id.details_transfer_amount_value);
            transfer.setText(view.getContext().getString(R.string.concat_string_view_format,
                    decimalFormat.format(transferAmount), receipt.getCurrency()));
        }
    }

    private void setDetailsView(@NonNull final Receipt receipt, @NonNull final View view) {
        TextView receiptId = view.findViewById(R.id.receipt_id_value);
        receiptId.setText(receipt.getJournalId());
        TextView dateView = view.findViewById(R.id.date_value);

        Date date = DateUtils.fromDateTimeString(receipt.getCreatedOn());
        String timezone = DateUtils.toDateFormat(date, DETAIL_TIMEZONE);
        dateView.setText(view.getContext().getString(R.string.concat_string_view_format,
                formatDateTime(view.getContext(), date.getTime(),
                        FORMAT_SHOW_DATE | FORMAT_SHOW_TIME | FORMAT_SHOW_YEAR
                                | FORMAT_SHOW_WEEKDAY | FORMAT_ABBREV_WEEKDAY), timezone));

        if (receipt.getDetails() != null) {
            ReceiptDetails receiptDetails = receipt.getDetails();
            if (!TextUtils.isEmpty(receiptDetails.getCharityName())) {
                setViewInformation(R.id.charity_layout, R.id.charity_value,
                        view, receiptDetails.getCharityName());
            }

            if (!TextUtils.isEmpty(receiptDetails.getCheckNumber())) {
                setViewInformation(R.id.check_number_layout, R.id.check_number_value,
                        view, receiptDetails.getCheckNumber());
            }

            if (!TextUtils.isEmpty(receiptDetails.getClientPaymentId())) {
                setViewInformation(R.id.client_id_layout, R.id.client_id_value,
                        view, receiptDetails.getClientPaymentId());
            }

            if (!TextUtils.isEmpty(receiptDetails.getWebsite())) {
                setViewInformation(R.id.website_layout, R.id.website_value,
                        view, receiptDetails.getWebsite());
            }

            if (!TextUtils.isEmpty(receiptDetails.getNotes())) {
                setViewInformation(R.id.receipt_notes_information, R.id.notes_value,
                        view, receiptDetails.getNotes());
            }
        }
    }

    private void setViewInformation(@IdRes final int layout, @IdRes final int viewValue,
            @NonNull final View view, @NonNull final String value) {
        view.findViewById(layout).setVisibility(View.VISIBLE);
        TextView textView = view.findViewById(viewValue);
        textView.setText(value);
    }
}
