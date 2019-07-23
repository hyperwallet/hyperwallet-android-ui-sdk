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

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepository;

import java.util.List;

public class ListTransferMethodPresenter implements ListTransferMethodContract.Presenter {

    private final TransferMethodRepository mTransferMethodRepository;
    private final ListTransferMethodContract.View mView;

    public ListTransferMethodPresenter(TransferMethodRepository repository, ListTransferMethodContract.View view) {
        mTransferMethodRepository = repository;
        mView = view;
    }

    @Override
    public void loadTransferMethods() {
        mView.showProgressBar();
        mTransferMethodRepository.loadTransferMethods(new TransferMethodRepository.LoadTransferMethodListCallback() {
            @Override
            public void onTransferMethodListLoaded(List<HyperwalletTransferMethod> transferMethods) {
                if (!mView.isActive()) {
                    return;
                }
                mView.hideProgressBar();
                mView.displayTransferMethods(transferMethods);
            }

            @Override
            public void onError(HyperwalletErrors errors) {
                if (!mView.isActive()) {
                    return;
                }
                mView.hideProgressBar();
                mView.showErrorListTransferMethods(errors.getErrors());
            }
        });
    }

    @Override
    public void deactivateTransferMethod(@NonNull final HyperwalletTransferMethod transferMethod) {
        mView.showProgressBar();
        mTransferMethodRepository.deactivateTransferMethod(transferMethod,
                new TransferMethodRepository.DeactivateTransferMethodCallback() {
                    @Override
                    public void onTransferMethodDeactivated(
                            @NonNull final StatusTransition statusTransition) {
                        if (!mView.isActive()) {
                            return;
                        }
                        mView.hideProgressBar();
                        mView.notifyTransferMethodDeactivated(statusTransition);

                    }

                    @Override
                    public void onError(HyperwalletErrors errors) {
                        if (!mView.isActive()) {
                            return;
                        }
                        mView.hideProgressBar();
                        mView.showErrorDeactivateTransferMethod(errors.getErrors());
                    }
                });
    }
}
