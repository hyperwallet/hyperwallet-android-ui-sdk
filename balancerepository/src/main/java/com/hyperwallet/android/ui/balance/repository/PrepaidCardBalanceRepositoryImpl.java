/*
 * Copyright 2018 Hyperwallet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.hyperwallet.android.ui.balance.repository;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.balance.Balance;
import com.hyperwallet.android.model.balance.PrepaidCardBalanceQueryParam;
import com.hyperwallet.android.model.paging.PageList;

import java.util.ArrayList;

public class PrepaidCardBalanceRepositoryImpl implements PrepaidCardBalanceRepository {

    private final Handler mHandler = new Handler();
    private final int LIMIT = 100;

    @VisibleForTesting
    Hyperwallet getHyperwallet() {
        return Hyperwallet.getDefault();
    }


    @Override
    public void loadPrepaidCardBalances(String prepaidCardToken,
            @NonNull final LoadPrepaidCardBalanceCallback callback) {
        PrepaidCardBalanceQueryParam queryParam = new PrepaidCardBalanceQueryParam.Builder()
                .limit(LIMIT)
                .sortByCurrencyAsc()
                .build();

        getHyperwallet().listPrepaidCardBalances(prepaidCardToken, queryParam,
                new HyperwalletListener<PageList<Balance>>() {
                    @Override
                    public void onSuccess(@Nullable PageList<Balance> result) {
                        callback.onPrepaidCardBalanceLoaded(
                                result != null ? result.getDataList() : new ArrayList<Balance>());
                    }

                    @Override
                    public void onFailure(HyperwalletException exception) {
                        callback.onError(exception.getErrors());
                    }

                    @Override
                    public Handler getHandler() {
                        return mHandler;
                    }
                });
    }
}
