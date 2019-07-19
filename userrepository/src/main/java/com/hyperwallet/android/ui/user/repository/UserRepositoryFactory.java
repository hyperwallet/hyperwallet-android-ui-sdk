package com.hyperwallet.android.ui.user.repository;

public class UserRepositoryFactory {
    private static UserRepositoryFactory sInstance;
    private UserRepository mUserRepository;

    private UserRepositoryFactory() {
        mUserRepository = new UserRepositoryImpl();
    }

    public static synchronized UserRepositoryFactory getInstance() {
        if (sInstance == null) {
            sInstance = new UserRepositoryFactory();
        }
        return sInstance;
    }

    public static void clearInstance() {
        sInstance = null;
    }

    public UserRepository getUserRepository() {
        return mUserRepository;
    }
}
