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
package com.hyperwallet.android.ui.common.view;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.ui.common.insight.HyperwalletInsight;
import com.hyperwallet.android.ui.common.util.ErrorTypes;
import com.hyperwallet.android.ui.common.util.ErrorUtils;
import com.hyperwallet.android.ui.common.view.error.DefaultErrorDialogFragment;

import java.util.List;

public final class ActivityUtils {

    private ActivityUtils() {
    }

    /**
     * Initialize the fragment for Activity
     *
     * @param fragmentActivity specify context of the Fragment
     * @param fragment         specify desired fragment
     * @param layout           specify desired layout
     */
    public static void initFragment(@NonNull final FragmentActivity fragmentActivity, @NonNull final Fragment fragment,
            final int layout) {
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(layout, fragment);
        fragmentTransaction.commit();
    }

    /**
     * Create and display the error dialog
     *
     * @param fragmentActivity specify context of the Fragment
     * @param errors           specify the errors
     */
    public static void showError(@NonNull final FragmentActivity fragmentActivity, @NonNull final String pageName,
            @NonNull final String pageGroup, @NonNull final List<HyperwalletError> errors) {
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        DefaultErrorDialogFragment fragment = (DefaultErrorDialogFragment) fragmentManager.findFragmentByTag(
                DefaultErrorDialogFragment.TAG);

        if (fragment == null) {
            fragment = DefaultErrorDialogFragment.newInstance(errors);
        }

        String errorMessage = errors.get(0).getMessage();
        if (errorMessage == null || errorMessage.trim().length() == 0) {
            errorMessage = ErrorUtils.getMessage(errors, fragmentActivity.getResources());
        }
        HyperwalletInsight.getInstance().trackError(fragmentActivity, pageName, pageGroup,
                new HyperwalletInsight.ErrorParamsBuilder()
                        .code(errors.get(0).getCode())
                        .message(errorMessage)
                        .fieldName(errors.get(0).getFieldName())
                        .type(ErrorTypes.getErrorType(errors.get(0).getCode()))
                        .description(ErrorTypes.getStackTrace())
                        .build());

        if (!fragment.isAdded()) {
            fragment.show(fragmentManager);
        }
    }
}
