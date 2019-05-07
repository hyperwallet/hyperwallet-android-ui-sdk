package com.hyperwallet.android.ui.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.HyperwalletUser;

/**
 * User Repository Contract
 */
public interface UserRepository {
    /**
     * Load user information
     *
     * @param callback @see {@link UserRepository.LoadUserCallback}
     */
    void loadUser(@NonNull final LoadUserCallback callback);

    /**
     * Set user to null
     */
    void refreshUser();

    /**
     * Callback interface that responses to action when invoked to
     * Load User information
     * <p>
     * When User is properly loaded
     * {@link UserRepository.LoadUserCallback#onUserLoaded(HyperwalletUser)}
     * is invoked otherwise {@link UserRepository.LoadUserCallback#onError(HyperwalletErrors)}
     * is called to further log or show error information
     */
    interface LoadUserCallback {

        void onUserLoaded(@Nullable final HyperwalletUser user);

        void onError(@NonNull final HyperwalletErrors errors);
    }
}
