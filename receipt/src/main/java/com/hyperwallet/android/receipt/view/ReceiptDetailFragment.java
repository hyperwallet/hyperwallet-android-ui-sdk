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
import static com.hyperwallet.android.receipt.view.ReceiptViewUtil.DETAIL_DATE_FORMAT;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.common.util.DateUtils;
import com.hyperwallet.android.model.receipt.Receipt;
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
                fromDateTimeString(receipt.getCreatedOn()), DETAIL_DATE_FORMAT));

        if (receipt.getDetails() != null) {
            if (!TextUtils.isEmpty(receipt.getDetails().getCharityName())) {
                view.findViewById(R.id.charity_layout).setVisibility(View.VISIBLE);
                TextView charity = view.findViewById(R.id.charity_value);
                charity.setText(receipt.getDetails().getCharityName());
            }

            if (!TextUtils.isEmpty(receipt.getDetails().getCheckNumber())) {
                view.findViewById(R.id.check_number_layout).setVisibility(View.VISIBLE);
                TextView check = view.findViewById(R.id.check_number_value);
                check.setText(receipt.getDetails().getCheckNumber());
            }

            if (!TextUtils.isEmpty(receipt.getDetails().getClientPaymentId())) {
                view.findViewById(R.id.client_id_layout).setVisibility(View.VISIBLE);
                TextView client = view.findViewById(R.id.client_id_value);
                client.setText(receipt.getDetails().getClientPaymentId());
            }

            if (!TextUtils.isEmpty(receipt.getDetails().getNotes())) {
                view.findViewById(R.id.notes_layout).setVisibility(View.VISIBLE);
                TextView notes = view.findViewById(R.id.notes_value);
                notes.setText(receipt.getDetails().getNotes());
            }

            if (!TextUtils.isEmpty(receipt.getDetails().getWebsite())) {
                view.findViewById(R.id.website_layout).setVisibility(View.VISIBLE);
                TextView website = view.findViewById(R.id.notes_value);
                website.setText(receipt.getDetails().getWebsite());
            }
        }
    }
}
