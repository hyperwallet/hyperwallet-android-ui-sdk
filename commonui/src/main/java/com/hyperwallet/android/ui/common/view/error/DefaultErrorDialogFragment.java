/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Hyperwallet Systems Inc.
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
package com.hyperwallet.android.ui.common.view.error;

import static com.hyperwallet.android.ExceptionMapper.EC_AUTHENTICATION_TOKEN_PROVIDER_EXCEPTION;
import static com.hyperwallet.android.ExceptionMapper.EC_IO_EXCEPTION;
import static com.hyperwallet.android.ExceptionMapper.EC_JSON_EXCEPTION;
import static com.hyperwallet.android.ExceptionMapper.EC_JSON_PARSE_EXCEPTION;
import static com.hyperwallet.android.ExceptionMapper.EC_UNEXPECTED_EXCEPTION;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.ui.common.R;

import java.util.ArrayList;
import java.util.List;

public class DefaultErrorDialogFragment extends DialogFragment {

    public static final int RESULT_ERROR = -100;
    public static final String TAG = DefaultErrorDialogFragment.class.getName();
    private static final String ARGUMENT_ERROR_KEY =
            "Hyperwallet:" + DefaultErrorDialogFragment.class.getName() + ":Error:Key";

    private DefaultErrorDialogFragmentContract.Presenter mPresenter;

    /**
     * Please do not use this to have instance of DefaultErrorDialogFragment this is reserved for android framework
     */
    public DefaultErrorDialogFragment() {
        setRetainInstance(true);
    }

    /**
     * Builds Hyperwallet Error Dialogue
     *
     * @param errors List of Errors @see {@link Error}
     */
    public static DefaultErrorDialogFragment newInstance(@NonNull List<HyperwalletError> errors) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ARGUMENT_ERROR_KEY, new ArrayList<>(errors));

        DefaultErrorDialogFragment errorDialogFragment = new DefaultErrorDialogFragment();
        errorDialogFragment.setArguments(bundle);
        return errorDialogFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = new DefaultErrorDialogFragmentPresenter();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        requireActivity().setResult(RESULT_ERROR);
        requireActivity().finish();
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
    public Dialog onCreateDialog(@Nullable Bundle state) {
        ArrayList<HyperwalletError> errors = getArguments().getParcelableArrayList(ARGUMENT_ERROR_KEY);
        if (errors == null) {
            errors = new ArrayList<>(1);
        }

        if (errors.isEmpty()) {
            HyperwalletError error = new HyperwalletError(getString(R.string.unexpected_exception),
                    EC_UNEXPECTED_EXCEPTION);
            errors.add(error);
        }

        String message = mPresenter.buildDialogMessage(errors, getResources());

        return buildDialog(errors.get(0).getCode(), message);
    }

    private AlertDialog buildDialog(@NonNull String errorCode, @NonNull String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(requireContext(), R.style.Theme_Hyperwallet_Alert));
        builder.setMessage(message);
        switch (errorCode) {
            case EC_UNEXPECTED_EXCEPTION:
            case EC_JSON_EXCEPTION:
            case EC_JSON_PARSE_EXCEPTION:
            case EC_AUTHENTICATION_TOKEN_PROVIDER_EXCEPTION:
                builder.setTitle(requireContext().getString(R.string.error_dialog_unexpected_title))
                        .setPositiveButton(getResources().getString(R.string.close_button_label),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requireActivity().setResult(RESULT_ERROR);
                                        requireActivity().finish();
                                    }
                                });
                break;
            case EC_IO_EXCEPTION: // connection error
                builder.setTitle(requireContext().getString(R.string.error_dialog_connectivity_title))
                        .setNegativeButton(getResources().getString(R.string.cancel_button_label),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requireActivity().setResult(RESULT_ERROR);
                                        requireActivity().finish();
                                    }
                                });
                if (requireActivity() instanceof OnNetworkErrorCallback) {
                    builder.setPositiveButton(getResources().getString(R.string.try_again_button_label),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dismissAllowingStateLoss();
                                    ((OnNetworkErrorCallback) requireActivity()).retry();
                                }
                            });
                }
                break;
            default: // normal rest errors, we will give the user a chance to fix input values from form
                builder.setTitle(requireContext().getString(R.string.error_dialog_title))
                        .setPositiveButton(getResources().getString(R.string.close_button_label), null);
        }
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
