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
package com.hyperwallet.android.ui.transfermethod.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.transfermethod.PrepaidCard;

import java.util.List;

/**
 * Prepaid card Repository Contract
 */
public interface PrepaidCardRepository {
    /**
     * Load prepaid cards information
     *
     * @param callback @see {@link PrepaidCardRepository.LoadPrepaidCardsCallback}
     */
    void loadPrepaidCards(@NonNull final LoadPrepaidCardsCallback callback);

    /**
     * get prepaid card information
     *
     * @param callback @see {@link PrepaidCardRepository.LoadPrepaidCardCallback}
     */
    void getPrepaidCard(@NonNull String token, LoadPrepaidCardCallback callback);

    /**
     * Callback interface that responses to action when invoked to
     * Load prepaid cards information
     * <p>
     * When prepaid cards is properly loaded
     * {@link PrepaidCardRepository.LoadPrepaidCardsCallback#onPrepaidCardListLoaded(List)}
     * is invoked otherwise {@link PrepaidCardRepository.LoadPrepaidCardsCallback#onError(Errors)}
     * is called to further log or show error information
     */
    interface LoadPrepaidCardsCallback {

        void onPrepaidCardListLoaded(@Nullable final List<PrepaidCard> prepaidCardList);

        void onError(@NonNull final Errors errors);
    }

    /**
     * Callback interface that responses to action when invoked to
     * Load prepaid card information
     * <p>
     * When prepaid card is properly loaded
     * {@link PrepaidCardRepository.LoadPrepaidCardCallback#onPrepaidCardLoaded(PrepaidCard)}
     * is invoked otherwise {@link PrepaidCardRepository.LoadPrepaidCardCallback#onError(Errors)}
     * is called to further log or show error information
     */
    interface LoadPrepaidCardCallback {

        void onPrepaidCardLoaded(@Nullable final PrepaidCard prepaidCard);

        void onError(@NonNull final Errors errors);
    }
}