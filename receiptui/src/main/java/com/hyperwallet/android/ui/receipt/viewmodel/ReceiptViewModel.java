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
package com.hyperwallet.android.ui.receipt.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagedList;

import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.ui.common.repository.Event;

public abstract class ReceiptViewModel extends ViewModel {

    /**
     * initialize the loadReceipt
     */
    public abstract void init();

    /**
     * @return live data of isloading information
     */
    public abstract LiveData<Boolean> isLoadingData();

    /**
     * @return live data of receipt errors {@link Errors}
     */
    public abstract LiveData<Event<Errors>> getReceiptErrors();

    /**
     * @return paged live data of receipts {@link Receipt}
     */
    public abstract LiveData<PagedList<Receipt>> getReceiptList();

    /**
     * Explicit invoke of load retry on receipts data
     */
    public abstract void retryLoadReceipts();

    /**
     * @return binding live data of detail navigation information
     */
    public abstract LiveData<Event<Receipt>> getDetailNavigation();

    /**
     * @param receipt {@link Receipt} object to set on navigating to Receipt details view
     */
    public abstract void setDetailNavigation(@NonNull final Receipt receipt);
}
