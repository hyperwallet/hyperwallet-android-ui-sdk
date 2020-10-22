/*
 * The MIT License (MIT)
 * Copyright (c) 2019 Hyperwallet Systems Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.hyperwallet.android.ui.receipt.viewmodel;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hyperwallet.android.Configuration;
import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.transfermethod.PrepaidCard;
import com.hyperwallet.android.model.user.User;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.common.view.ProgramModel;
import com.hyperwallet.android.ui.transfermethod.repository.PrepaidCardRepository;
import com.hyperwallet.android.ui.user.repository.UserRepository;

import java.util.List;

public class TabbedListReceiptsViewModel extends ViewModel {
    private UserRepository mUserRepository;
    private PrepaidCardRepository mPrepaidCardRepository;
    private MutableLiveData<User> mUser = new MutableLiveData<User>();
    private MutableLiveData<List<PrepaidCard>> mPrepaidCards = new MutableLiveData<List<PrepaidCard>>();
    private MutableLiveData<Event<Errors>> mErrors = new MutableLiveData<Event<Errors>>();
    public ProgramModel mProgramModel;
    private boolean mIsInitialized;

    public MutableLiveData<User> getUser() {
        return mUser;
    }

    public MutableLiveData<List<PrepaidCard>> getPrepaidCards() {
        return mPrepaidCards;
    }

    @VisibleForTesting
    Hyperwallet getHyperwallet() {
        return Hyperwallet.getDefault();
    }

    public MutableLiveData<Event<Errors>> getErrors() {
        return mErrors;
    }

    public TabbedListReceiptsViewModel(
            UserRepository mUserRepository,
            PrepaidCardRepository prepaidCardRepository) {
        this.mUserRepository = mUserRepository;
        this.mPrepaidCardRepository = prepaidCardRepository;
    }

    public void initialize() {
        if (!mIsInitialized) {
            mIsInitialized = true;
            if (!ProgramModel.isCardModel(mProgramModel)) {
                loadUser();
            }
            loadPrepaidCards();
        }
    }

    @VisibleForTesting
    void loadUser() {
        mUserRepository.loadUser(new UserRepository.LoadUserCallback() {
            @Override
            public void onUserLoaded(@NonNull User user) {
                TabbedListReceiptsViewModel.this.mUser.postValue(user);
            }

            @Override
            public void onError(@NonNull Errors errors) {
                TabbedListReceiptsViewModel.this.mErrors.postValue(new Event(errors));
            }
        });
    }

    public void initProgramModel() {
        getProgramModel();
    }

    @VisibleForTesting
    ProgramModel getProgramModel() {
        getHyperwallet().getConfiguration(new HyperwalletListener<Configuration>() {
            @Override
            public void onSuccess(@Nullable Configuration result) {
                if (result != null && !result.getProgramModel().isEmpty()) {
                    mProgramModel = ProgramModel.valueOf(result.getProgramModel());
                }
            }

            @Override
            public void onFailure(HyperwalletException exception) {

            }

            @Override
            public Handler getHandler() {
                return null;
            }
        });
        return mProgramModel;
    }

    @VisibleForTesting
    void loadPrepaidCards() {
        mPrepaidCardRepository.loadPrepaidCards(new PrepaidCardRepository.LoadPrepaidCardsCallback() {
            @Override
            public void onPrepaidCardListLoaded(@NonNull List<PrepaidCard> prepaidCardList) {
                if (!prepaidCardList.isEmpty()) {
                    TabbedListReceiptsViewModel.this.mPrepaidCards.postValue(prepaidCardList);
                }
            }

            @Override
            public void onError(@NonNull Errors errors) {
                if (errors != null) {
                    TabbedListReceiptsViewModel.this.mErrors.postValue(new Event(errors));
                }
            }
        });
    }

    public void retry() {
        loadUser();
    }

    public static class TabbedListReceiptsViewModelFactory implements ViewModelProvider.Factory {
        private final UserRepository userRepository;
        private final PrepaidCardRepository prepaidCardRepository;

        public TabbedListReceiptsViewModelFactory(
                UserRepository userRepository,
                PrepaidCardRepository prepaidCardRepository) {
            this.userRepository = userRepository;
            this.prepaidCardRepository = prepaidCardRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(TabbedListReceiptsViewModel.class)) {
                return (T) new TabbedListReceiptsViewModel(userRepository, prepaidCardRepository);
            }
            throw new IllegalArgumentException(
                    "Expecting ViewModel class: " + TabbedListReceiptsViewModel.class.getName());
        }
    }
}
