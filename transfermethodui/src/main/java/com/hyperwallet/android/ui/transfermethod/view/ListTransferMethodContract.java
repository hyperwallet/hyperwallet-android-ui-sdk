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
package com.hyperwallet.android.ui.transfermethod.view;

import androidx.annotation.NonNull;

import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.transfermethod.TransferMethod;

import java.util.List;

public interface ListTransferMethodContract {

    interface View {

        void displayTransferMethods(@NonNull final List<TransferMethod> transferMethodList);

        void notifyTransferMethodDeactivated(@NonNull final StatusTransition statusTransition);

        void showErrorListTransferMethods(@NonNull final List<Error> errors);

        void showErrorDeactivateTransferMethod(@NonNull final List<Error> errors);

        void initiateAddTransferMethodFlow();

        void initiateAddTransferMethodFlowResult();

        void confirmTransferMethodDeactivation(@NonNull TransferMethod transferMethod);

        void showProgressBar();

        void hideProgressBar();

        /**
         * Check the state of a View
         *
         * @return true when View is added to Container
         */
        boolean isActive();

        void loadTransferMethods();
    }

    interface Presenter {

        void loadTransferMethods();

        void deactivateTransferMethod(@NonNull final TransferMethod transferMethod);
    }
}
