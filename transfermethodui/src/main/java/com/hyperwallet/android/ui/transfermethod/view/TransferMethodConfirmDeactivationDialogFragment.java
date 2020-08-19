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
 * NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.hyperwallet.android.ui.transfermethod.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.hyperwallet.android.ui.R;

public class TransferMethodConfirmDeactivationDialogFragment extends DialogFragment {

    public static final String TAG = "Hyperwallet:" + TransferMethodConfirmDeactivationDialogFragment.class.getName();
    private static final String ARGUMENT_KEY_TRANSFER_DESTINATION = "ARGUMENT_KEY_TRANSFER_DESTINATION";

    public static TransferMethodConfirmDeactivationDialogFragment newInstance(
            @NonNull final String transferDestination) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_KEY_TRANSFER_DESTINATION, transferDestination);

        TransferMethodConfirmDeactivationDialogFragment dialogFragment =
                new TransferMethodConfirmDeactivationDialogFragment();
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    public void show(@NonNull FragmentManager manager) {
        show(manager, TAG);
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(requireContext(), R.style.Theme_Hyperwallet_Confirmation_Dialog));
        builder.setMessage(requireContext().getString(R.string.mobileAreYouSure))
                .setTitle(R.string.mobileRemoveEAconfirm)
                .setNegativeButton(R.string.cancelButtonLabel, null);
        if (getActivity() instanceof OnTransferMethodDeactivateCallback) {
            builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismissAllowingStateLoss();
                    ((OnTransferMethodDeactivateCallback) getActivity()).confirm();
                }
            });
        }
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
