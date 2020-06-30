/*
 * The MIT License (MIT)
 * Copyright (c) 2020 Hyperwallet Systems Inc.
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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.hyperwallet.android.ui.transfer.R;

import java.util.Currency;
import java.util.Locale;

public class TransferConfirmationDialogFragment extends DialogFragment {

    public static final String TAG = TransferConfirmationDialogFragment.class.getName();
    private static final String ARGUMENT_KEY_AMOUNT = "ARGUMENT_KEY_AMOUNT";
    private static final String ARGUMENT_KEY_CURRENCY_CODE = "ARGUMENT_KEY_CURRENCY_CODE";
    private static final String ARGUMENT_KEY_TRANSFER_DESTINATION = "ARGUMENT_KEY_TRANSFER_DESTINATION";

    public static TransferConfirmationDialogFragment newInstance(@NonNull final String amount,
            @NonNull final String currencyCode, @NonNull final String transferDestination) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_KEY_AMOUNT, amount);
        bundle.putString(ARGUMENT_KEY_CURRENCY_CODE, currencyCode);
        bundle.putString(ARGUMENT_KEY_TRANSFER_DESTINATION, transferDestination);

        TransferConfirmationDialogFragment fragment = new TransferConfirmationDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public void show(@NonNull FragmentManager manager) {
        show(manager, TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle state) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(requireContext(),
                        com.hyperwallet.android.ui.common.R.style.Theme_Hyperwallet_Confirmation_Dialog));
        builder.setTitle(requireContext().getString(R.string.mobileTransferSuccessMsg,
                Currency.getInstance(getArguments().getString(ARGUMENT_KEY_CURRENCY_CODE)).getSymbol(
                        Locale.getDefault()),
                getArguments().getString(ARGUMENT_KEY_AMOUNT)));
        builder.setMessage(requireContext().getString(R.string.mobileTransferSuccessDetails,
                getArguments().getString(ARGUMENT_KEY_TRANSFER_DESTINATION)));

        builder.setPositiveButton(getResources().getString(R.string.doneButtonLabel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requireActivity().finish();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
