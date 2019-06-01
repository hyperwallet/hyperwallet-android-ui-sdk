package com.hyperwallet.android.common.repository;

public class ReceiptRepositoryFactory {


    private static ReceiptRepositoryFactory sInstance;
    private ReceiptRepository mReceiptRepository;

    private ReceiptRepositoryFactory() {
        mReceiptRepository = new ReceiptRepositoryImpl();
    }

    public static synchronized ReceiptRepositoryFactory getInstance() {
        if (sInstance == null) {
            sInstance = new ReceiptRepositoryFactory();
        }
        return sInstance;
    }


    public ReceiptRepository getReceiptRepository() {
        return mReceiptRepository;
    }

}
