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
package com.hyperwallet.android.receipt.view;

import static com.hyperwallet.android.receipt.view.ReceiptViewUtil.AMOUNT_FORMAT;
import static com.hyperwallet.android.receipt.view.ReceiptViewUtil.DETAIL_DATE_TIME_12H_FORMAT;
import static com.hyperwallet.android.receipt.view.ReceiptViewUtil.DETAIL_DATE_TIME_24H_FORMAT;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.common.util.DateUtils;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.model.receipt.ReceiptDetails;
import com.hyperwallet.android.receipt.R;
import com.hyperwallet.android.receipt.viewmodel.ReceiptDetailViewModel;

import java.text.DecimalFormat;

public class ReceiptDetailFragment extends Fragment {

    private ReceiptDetailViewModel mReceiptDetailViewModel;

    public ReceiptDetailFragment() {
        setRetainInstance(true);
    }

    public static ReceiptDetailFragment newInstance() {
        ReceiptDetailFragment fragment = new ReceiptDetailFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiptDetailViewModel = ViewModelProviders.of(requireActivity()).get(ReceiptDetailViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receipt_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Receipt receipt = mReceiptDetailViewModel.getReceipt();

        // transactions
        ReceiptViewUtil util = new ReceiptViewUtil();
        util.setTransactionView(receipt, view);

        // fee details
        if (!TextUtils.isEmpty(receipt.getFee())) {
            view.findViewById(R.id.fee_details_layout).setVisibility(View.VISIBLE);
            double feeAmount = Double.parseDouble(receipt.getFee());
            double transferAmount = Double.parseDouble(receipt.getAmount());
            double amount = transferAmount - feeAmount;
            DecimalFormat decimalFormat = new DecimalFormat(AMOUNT_FORMAT);

            TextView amountView = view.findViewById(R.id.details_amount_value);
            amountView.setText(view.getContext().getString(R.string.amount_view_format,
                    decimalFormat.format(amount), receipt.getCurrency()));

            TextView fee = view.findViewById(R.id.details_fee_value);
            fee.setText(view.getContext().getString(R.string.amount_view_format,
                    decimalFormat.format(feeAmount), receipt.getCurrency()));

            TextView transfer = view.findViewById(R.id.details_transfer_amount_value);
            transfer.setText(view.getContext().getString(R.string.amount_view_format,
                    decimalFormat.format(transferAmount), receipt.getCurrency()));
        }

        // receipt details
        TextView receiptId = view.findViewById(R.id.receipt_id_value);
        receiptId.setText(receipt.getJournalId());
        TextView date = view.findViewById(R.id.date_value);
        date.setText(DateUtils.toDateFormat(DateUtils.
                        fromDateTimeString(receipt.getCreatedOn()),
                DateFormat.is24HourFormat(view.getContext()) ? DETAIL_DATE_TIME_24H_FORMAT
                        : DETAIL_DATE_TIME_12H_FORMAT));

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

            if (!TextUtils.isEmpty(receiptDetails.getNotes())) {
                setViewInformation(R.id.notes_layout, R.id.notes_value,
                        view, receiptDetails.getNotes());
            }

            if (!TextUtils.isEmpty(receiptDetails.getWebsite())) {
                setViewInformation(R.id.website_layout, R.id.website_value,
                        view, receiptDetails.getWebsite());
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
