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

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.transfermethod.PrepaidCard;
import com.hyperwallet.android.model.user.User;
import com.hyperwallet.android.ui.common.repository.Event;
import com.hyperwallet.android.ui.receipt.repository.PrepaidCardRepository;
import com.hyperwallet.android.ui.user.repository.UserRepository;

import java.util.List;

public class TabbedListReceiptViewModel extends ViewModel {
    public static final String TAG = TabbedListReceiptViewModel.class.getSimpleName();
    private UserRepository userRepository;
    private PrepaidCardRepository prepaidCardRepository;
    public MutableLiveData<User> user = new MutableLiveData<User>();
    public MutableLiveData<List<PrepaidCard>> prepaidCards = new MutableLiveData<List<PrepaidCard>>();
    public MutableLiveData errors = new MutableLiveData<Event<Errors>>();

    public TabbedListReceiptViewModel(
            UserRepository mUserRepository,
            PrepaidCardRepository prepaidCardRepository) {
        this.userRepository = mUserRepository;
        this.prepaidCardRepository = prepaidCardRepository;
    }

    public void initialize() {
        loadUser();
    }

    private void loadUser() {
        userRepository.loadUser(new UserRepository.LoadUserCallback() {
            @Override
            public void onUserLoaded(@NonNull User user) {
                TabbedListReceiptViewModel.this.user.postValue(user);
                loadPrepaidCards();
            }

            @Override
            public void onError(@NonNull Errors errors) {
                TabbedListReceiptViewModel.this.errors.postValue(errors);
            }
        });
    }

    private void loadPrepaidCards() {
        prepaidCardRepository.loadPrepaidCards(new PrepaidCardRepository.LoadPrepaidCardsCallback() {
            @Override
            public void onPrepaidCardLoaded(@NonNull List<PrepaidCard> prepaidCardList) {
                if (!prepaidCardList.isEmpty()) {
                    TabbedListReceiptViewModel.this.prepaidCards.postValue(prepaidCardList);
                }
            }

            @Override
            public void onError(@NonNull Errors errors) {
                if (errors != null) {
                    TabbedListReceiptViewModel.this.errors.postValue(new Event(errors));
                }
            }
        });
    }

    public static class TabbedListReceiptViewModelFactory implements ViewModelProvider.Factory {
        private final UserRepository userRepository;
        private final PrepaidCardRepository prepaidCardRepository;

        public TabbedListReceiptViewModelFactory(
                UserRepository userRepository,
                PrepaidCardRepository prepaidCardRepository) {
            this.userRepository = userRepository;
            this.prepaidCardRepository = prepaidCardRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(TabbedListReceiptViewModel.class)) {
                return (T) new TabbedListReceiptViewModel(userRepository, prepaidCardRepository);
            }
            throw new IllegalArgumentException(
                    "Expecting ViewModel class: " + TabbedListReceiptViewModel.class.getName());
        }
    }
}
