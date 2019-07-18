package com.hyperwallet.android.ui.transfermethod.repository;

public class TransferMethodRepositoryFactory {
    private static TransferMethodRepositoryFactory sInstance;
    private TransferMethodRepository mTransferMethodRepository;

    private TransferMethodRepositoryFactory() {
        mTransferMethodRepository = new TransferMethodRepositoryImpl();
    }

    public static synchronized TransferMethodRepositoryFactory getInstance() {
        if (sInstance == null) {
            sInstance = new TransferMethodRepositoryFactory();
        }
        return sInstance;
    }

    public static void clearInstance() {
        sInstance = null;
    }

    public TransferMethodRepository getTransferMethodRepository() {
        return mTransferMethodRepository;
    }
}
