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
                        com.hyperwallet.android.ui.common.R.style.Theme_Hyperwallet_Alert));
        builder.setTitle(requireContext().getString(R.string.mobileTransferSuccessMsg,
                getArguments().getString(ARGUMENT_KEY_AMOUNT),
                getArguments().getString(ARGUMENT_KEY_CURRENCY_CODE)));
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
