package com.hyperwallet.android.common.repository;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class ReceiptRepositoryFactory {


    private static ReceiptRepositoryFactory sInstance;
    private final Map<String, ReceiptRepository> mReceiptRepositoryInstances = new HashMap<>();

    private ReceiptRepositoryFactory() {
    }

    public static synchronized ReceiptRepositoryFactory getInstance() {
        if (sInstance == null) {
            sInstance = new ReceiptRepositoryFactory();
        }
        return sInstance;
    }


    public synchronized ReceiptRepository getReceiptRepository(@NonNull final String receiptSourceToken) {
        if (!mReceiptRepositoryInstances.containsKey(receiptSourceToken)) {
            mReceiptRepositoryInstances.put(receiptSourceToken, new ReceiptRepositoryImpl(receiptSourceToken));
        }
        return mReceiptRepositoryInstances.get(receiptSourceToken);
    }


}
