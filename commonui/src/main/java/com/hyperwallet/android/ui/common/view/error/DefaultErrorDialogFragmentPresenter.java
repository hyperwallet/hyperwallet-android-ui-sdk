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

import android.content.res.Resources;

import androidx.annotation.NonNull;

import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.sdk.R;
import com.hyperwallet.android.ui.common.util.ErrorTypes;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the DefaultErrorDialogFragmentContract.Presenter
 */
public class DefaultErrorDialogFragmentPresenter implements DefaultErrorDialogFragmentContract.Presenter {

    @NonNull
    @Override
    public String buildDialogMessage(@NonNull List<HyperwalletError> errors, @NonNull Resources resources) {
        String message;
        HyperwalletError error = errors.get(0);
        String errorType = ErrorTypes.getErrorType(error.getCode());

        switch (errorType) {
            case ErrorTypes.SDK_ERROR:
                message = resources.getString(R.string.unexpected_exception);
                break;
            case ErrorTypes.CONNECTION_ERROR:
                message = getMessageFromResources(errors, resources);
                break;
            default:
                message = error.getMessageFromResourceWhenAvailable(resources);
        }
        return message;
    }

    private String getMessageFromResources(@NonNull final List<HyperwalletError> errors,
            @NonNull final Resources resources) {
        StringBuilder messageBuilder = new StringBuilder();
        Iterator<HyperwalletError> iterator = errors.iterator();
        while (iterator.hasNext()) {
            messageBuilder.append(iterator.next().getMessageFromResourceWhenAvailable(resources));
            if (iterator.hasNext()) {
                messageBuilder.append("\n");
            }
        }

        return messageBuilder.toString();
    }
}
