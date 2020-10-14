package com.hyperwallet.android.ui.receipt.viewmodel;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import androidx.annotation.NonNull;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.HyperwalletAuthenticationTokenListener;
import com.hyperwallet.android.HyperwalletAuthenticationTokenProvider;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.transfermethod.PrepaidCard;
import com.hyperwallet.android.ui.transfermethod.repository.PrepaidCardRepository;
import com.hyperwallet.android.ui.transfermethod.repository.PrepaidCardRepositoryImpl;
import com.hyperwallet.android.ui.user.repository.UserRepository;
import com.hyperwallet.android.ui.user.repository.UserRepositoryImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class TabbedListReceiptsViewModelTest {
    private UserRepository mUserRepository;
    private PrepaidCardRepository mPrepaidCardRepository;
    private PrepaidCardRepository.LoadPrepaidCardsCallback mLoadPrepaidCardsCallback;
    private TabbedListReceiptsViewModel.TabbedListReceiptsViewModelFactory mTabbedListReceiptsViewModelFactory;
    private TabbedListReceiptsViewModel mTabbedListReceiptsViewModel;
    private Hyperwallet mHyperwallet;
    private HyperwalletAuthenticationTokenProvider mHyperwalletAuthenticationTokenProvider;

    @Before
    public void initializedViewModel() {
        mPrepaidCardRepository = spy(new PrepaidCardRepositoryImpl());
        mUserRepository = spy(new UserRepositoryImpl());
        mLoadPrepaidCardsCallback = spy(new PrepaidCardRepository.LoadPrepaidCardsCallback() {
            @Override
            public void onPrepaidCardListLoaded(@NonNull List<PrepaidCard> prepaidCardList) {

            }

            @Override
            public void onError(@NonNull Errors errors) {

            }
        });

        mHyperwalletAuthenticationTokenProvider =
                spy(new HyperwalletAuthenticationTokenProvider() {
                    @Override
                    public void retrieveAuthenticationToken(
                            HyperwalletAuthenticationTokenListener authenticationTokenListener) {

                    }
                });

        mHyperwallet = spy(Hyperwallet.getInstance(mHyperwalletAuthenticationTokenProvider));
        mTabbedListReceiptsViewModelFactory = new TabbedListReceiptsViewModel.TabbedListReceiptsViewModelFactory(
                mUserRepository, mPrepaidCardRepository);
        mTabbedListReceiptsViewModel = spy(
                mTabbedListReceiptsViewModelFactory.create(TabbedListReceiptsViewModel.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                PrepaidCardRepository.LoadPrepaidCardsCallback callback = invocation.getArgument(0);
                callback.onPrepaidCardListLoaded(new ArrayList<PrepaidCard>());
                return callback;
            }
        }).when(mPrepaidCardRepository).loadPrepaidCards(ArgumentMatchers.any(
                PrepaidCardRepository.LoadPrepaidCardsCallback.class));
    }

    @Test
    public void testGetUser_returnsLiveData() {
        assertThat(mTabbedListReceiptsViewModel.getUser(), is(notNullValue()));
    }

    @Test
    public void testGetErrors_returnsLiveData() {
        assertThat(mTabbedListReceiptsViewModel.getErrors(), is(notNullValue()));
    }

    @Test
    public void testGetReceiptList_returnsLiveData() {
        assertThat(mTabbedListReceiptsViewModel.getPrepaidCards(), is(notNullValue()));
    }

    @Test
    public void testListPrepaidCardReceiptViewModel() {
        verify(mPrepaidCardRepository, never()).loadPrepaidCards(mLoadPrepaidCardsCallback);
    }

    @Test
    public void testInit_verifyInitializedOnce() {
        mTabbedListReceiptsViewModel.initialize();
        verify(mTabbedListReceiptsViewModel).loadUser();
        // call again. multiple calls to init should only register 1 call to repository
        mTabbedListReceiptsViewModel.initialize();
        verify(mTabbedListReceiptsViewModel).loadUser();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTabbedListReceiptViewModelFactory_throwsExceptionOnInvalidClassArgument() {
        mTabbedListReceiptsViewModelFactory.create(ReceiptDetailViewModel.class);
    }

    @Test
    public void testRetry_loadInitial() {
        mTabbedListReceiptsViewModel.retry();
        verify(mTabbedListReceiptsViewModel).loadUser();
    }
}
