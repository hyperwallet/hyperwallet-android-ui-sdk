package com.hyperwallet.android.transfer;

public class TransferRepositoryFactory {

    private static TransferRepositoryFactory sInstance;

    private TransferRepository mTransferRepository;


    private TransferRepositoryFactory() {
        mTransferRepository = new TransferRepositoryImpl();
    }

    public synchronized static TransferRepositoryFactory getInstance() {
        if (sInstance == null) {
            sInstance = new TransferRepositoryFactory();
        }
        return sInstance;
    }

    public TransferRepository getTransferRepository() {
        return mTransferRepository;
    }
}
