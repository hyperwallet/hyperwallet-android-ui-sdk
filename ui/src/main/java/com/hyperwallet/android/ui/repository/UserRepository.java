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
