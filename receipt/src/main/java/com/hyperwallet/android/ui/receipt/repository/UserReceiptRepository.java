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
package com.hyperwallet.android.ui.receipt.repository;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.ui.common.viewmodel.Event;

/**
 * Receipt Repository Contract
 */
public interface UserReceiptRepository {

    /**
     * Load receipts information, consumer can subscribe to receipts data changes events
     *
     * @return live data paged receipts
     */
    LiveData<PagedList<Receipt>> loadReceipts();

    /**
     * Loading indicator consumer can subscribe to loading of data events
     *
     * @return live data <code>true</code> if load receipt is in loading state; <code>false</code> otherwise
     */
    LiveData<Boolean> isLoading();

    /**
     * Error information, consumer can subscribe of errors occur during data retrieval
     *
     * @return live event data list of errors if there's an error
     */
    LiveData<Event<HyperwalletErrors>> getErrors();

    /**
     * Reload receipt information, usually invoked when error is raised after the first load and consumer opts to retry
     * the last operation
     */
    void retryLoadReceipt();

}
