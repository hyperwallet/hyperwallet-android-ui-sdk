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

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

/**
 * Data source factory for PrepaidCard receipt data source, that uses {@link DataSource.Factory} facility
 */
public class PrepaidCardReceiptDataSourceFactory extends DataSource.Factory {

    private final MutableLiveData<PrepaidCardReceiptDataSource> mDataSourceMutableLiveData;
    private final PrepaidCardReceiptDataSource mPrepaidCardReceiptDataSource;

    PrepaidCardReceiptDataSourceFactory(@NonNull final String token) {
        mPrepaidCardReceiptDataSource = new PrepaidCardReceiptDataSource(token);
        mDataSourceMutableLiveData = new MutableLiveData<>();
        mDataSourceMutableLiveData.setValue(mPrepaidCardReceiptDataSource);
    }

    /**
     * Returns observable members of prepaid card receipt data source
     */
    LiveData<PrepaidCardReceiptDataSource> getPrepaidCardReceiptDataSource() {
        return mDataSourceMutableLiveData;
    }

    /**
     * @see {@link DataSource.Factory#create()}
     */
    @NonNull
    @Override
    public DataSource create() {
        return mPrepaidCardReceiptDataSource;
    }
}
