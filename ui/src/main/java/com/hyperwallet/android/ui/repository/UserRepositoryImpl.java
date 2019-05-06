package com.hyperwallet.android.ui.repository;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.HyperwalletUser;

public class UserRepositoryImpl implements UserRepository {

    private Handler mHandler = new Handler();
    private HyperwalletUser mUser;

    @VisibleForTesting
    Hyperwallet getHyperwallet() {
        return Hyperwallet.getDefault();
    }

    @Override
    public void loadUser(@NonNull final LoadUserCallback callback) {
        if (mUser == null) {
            getHyperwallet().getUser(new HyperwalletListener<HyperwalletUser>() {
                @Override
                public void onSuccess(@Nullable HyperwalletUser result) {
                    mUser = result;
                    callback.onUserLoaded(mUser);
                }

                @Override
                public void onFailure(HyperwalletException exception) {
                    callback.onError(exception.getHyperwalletErrors());
                }

                @Override
                public Handler getHandler() {
                    return mHandler;
                }
            });
        } else {
            callback.onUserLoaded(mUser);
        }
    }
}
