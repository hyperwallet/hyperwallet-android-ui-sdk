package com.hyperwallet.android.ui.receipt.viewmodel;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import androidx.annotation.NonNull;

import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.transfermethod.PrepaidCard;
import com.hyperwallet.android.ui.transfermethod.repository.PrepaidCardRepository;
import com.hyperwallet.android.ui.transfermethod.repository.PrepaidCardRepositoryImpl;
import com.hyperwallet.android.ui.user.repository.UserRepository;
import com.hyperwallet.android.ui.user.repository.UserRepositoryImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class TabbedListReceiptsViewModelTest {
    private UserRepository mUserRepository;
    private PrepaidCardRepository mPrepaidCardRepository;
    private PrepaidCardRepository.LoadPrepaidCardsCallback mLoadPrepaidCardsCallback;
    private TabbedListReceiptsViewModel.TabbedListReceiptsViewModelFactory mTabbedListReceiptsViewModelFactory;
    private TabbedListReceiptsViewModel mTabbedListReceiptsViewModel;

    @Before
    public void initializedViewModel() {
        mPrepaidCardRepository = spy(new PrepaidCardRepositoryImpl());
        mUserRepository = spy(new UserRepositoryImpl());
        mLoadPrepaidCardsCallback = spy(new PrepaidCardRepository.LoadPrepaidCardsCallback() {
            @Override
            public void onPrepaidCardsLoaded(@NonNull List<PrepaidCard> prepaidCardList) {

            }

            @Override
            public void onError(@NonNull Errors errors) {

            }
        });
        mTabbedListReceiptsViewModelFactory = new TabbedListReceiptsViewModel.TabbedListReceiptsViewModelFactory(
                mUserRepository, mPrepaidCardRepository);
        mTabbedListReceiptsViewModel = mTabbedListReceiptsViewModelFactory.create(TabbedListReceiptsViewModel.class);
    }

    @Test
    public void testListPrepaidCardReceiptViewModel() {
        verify(mPrepaidCardRepository, never()).loadPrepaidCards(mLoadPrepaidCardsCallback);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTabbedListReceiptViewModelFactory_throwsExceptionOnInvalidClassArgument() {
        mTabbedListReceiptsViewModelFactory.create(ReceiptDetailViewModel.class);
    }
}
