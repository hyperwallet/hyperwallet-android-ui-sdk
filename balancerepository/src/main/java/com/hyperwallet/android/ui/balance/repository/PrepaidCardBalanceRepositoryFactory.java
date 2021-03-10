package com.hyperwallet.android.ui.balance.repository;

public class PrepaidCardBalanceRepositoryFactory {
    private static PrepaidCardBalanceRepositoryFactory sInstance;
    private PrepaidCardBalanceRepository mPrepaidCardBalanceRepository;

    private PrepaidCardBalanceRepositoryFactory() {
        mPrepaidCardBalanceRepository = new PrepaidCardBalanceRepositoryImpl();
    }

    public static synchronized PrepaidCardBalanceRepositoryFactory getInstance() {
        if (sInstance == null) {
            sInstance = new PrepaidCardBalanceRepositoryFactory();
        }
        return sInstance;
    }

    public static void clearInstance() {
        sInstance = null;
    }

    public PrepaidCardBalanceRepository getPrepaidCardBalanceRepository() {
        return mPrepaidCardBalanceRepository;
    }

}
