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
package com.hyperwallet.android.common.view.error;

import android.content.res.Resources;

import androidx.annotation.NonNull;

import com.hyperwallet.android.model.HyperwalletError;

import java.util.List;

/**
 * Presenter Contract for building an {@link DefaultErrorDialogFragment}
 */
interface DefaultErrorDialogFragmentContract {

    interface Presenter {
        /**
         * Build message from a list of {@link HyperwalletError}.
         *
         * @param errors    List of the {@link HyperwalletError}
         * @param resources {@link Resources}
         * @return Message based on the {@link HyperwalletError#getCode()}
         */
        @NonNull
        String buildDialogMessage(@NonNull List<HyperwalletError> errors, @NonNull Resources resources);
    }
}
