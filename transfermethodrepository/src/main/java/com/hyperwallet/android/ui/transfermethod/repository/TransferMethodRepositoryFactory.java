package com.hyperwallet.android.ui.transfermethod.repository;

public class TransferMethodRepositoryFactory {
    private static TransferMethodRepositoryFactory sInstance;
    private TransferMethodRepository mTransferMethodRepository;
    private TransferMethodConfigurationRepository mTransferMethodConfigurationRepository;

    private TransferMethodRepositoryFactory() {
        mTransferMethodRepository = new TransferMethodRepositoryImpl();
        mTransferMethodConfigurationRepository = new TransferMethodConfigurationRepositoryImpl();
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

    public TransferMethodConfigurationRepository getTransferMethodConfigurationRepository() {
        return mTransferMethodConfigurationRepository;
    }
}
