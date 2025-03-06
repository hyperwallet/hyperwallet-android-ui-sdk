package com.hyperwallet.android.ui.receipt.viewmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.hyperwallet.android.Configuration;
import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.HyperwalletAuthenticationTokenListener;
import com.hyperwallet.android.HyperwalletAuthenticationTokenProvider;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.paging.PageList;
import com.hyperwallet.android.model.transfermethod.PrepaidCard;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.transfermethod.repository.PrepaidCardRepository;
import com.hyperwallet.android.ui.transfermethod.repository.PrepaidCardRepositoryImpl;
import com.hyperwallet.android.ui.user.repository.UserRepository;
import com.hyperwallet.android.ui.user.repository.UserRepositoryImpl;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
    private HyperwalletAuthenticationTokenProvider mHyperwalletAuthenticationTokenProvider;

    @Mock
    private Hyperwallet mHyperwallet;
    private static Configuration mConfiguration;
    private static String mJwtToken;

    @Rule
    public HyperwalletExternalResourceManager mResourceManager = new HyperwalletExternalResourceManager();
    @Rule
    public InstantTaskExecutorRule mInstantTaskExecutor= new InstantTaskExecutorRule();

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
        }).when(mPrepaidCardRepository).loadPrepaidCards(any(
                PrepaidCardRepository.LoadPrepaidCardsCallback.class));

        try {
            mJwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9."
                    +
                    "eyJzdWIiOiJ1c3ItdG9rZW4iLCJpYXQiOjE1Nzk3MzAzOTIsImV4cCI6MTY3OTczMDk5MiwiYXVkIjoidXNyLXRva2VuIiwiaXNzIjoicHJnLXRva2VuIiwicmVzdC11cmkiOiJodHRwOi"
                    +
                    "8vbG9jYWxob3N0OjgwODAvcmVzdC92My8iLCJncmFwaHFsLXVyaSI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC9ncmFwaHFsIiwicHJvZ3JhbS1tb2RlbCI6IldBTExFVF9NT0RFTCJ9."
                    + "jpMquopCyBPT1n32OoZLu1PylxtTepuS_KrCZ-xLgDgy9JtcqMDy7zbiBDmj7AQqpGR9loaZq3YQDCjwp8Sy_Q";
            mConfiguration = new Configuration(mJwtToken);
        } catch (JSONException e) {
            fail("Unable to parse json response");
        }

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
    public void testGetPrepaidCardsList_returnsLiveData() {
        assertThat(mTabbedListReceiptsViewModel.getPrepaidCards(), is(notNullValue()));
    }

    @Test
    public void testListPrepaidCardsReceiptViewModel() {
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
    public void testRetry_loadListReceipts() {
        mTabbedListReceiptsViewModel.retry();
        assertThat(mTabbedListReceiptsViewModel.getRetryListReceipts(), is(notNullValue()));
    }

    @Test
    public void testGetProgramModel_success() {
        TabbedListReceiptsViewModel viewModel = spy(
                new TabbedListReceiptsViewModel.TabbedListReceiptsViewModelFactory(mUserRepository,
                        mPrepaidCardRepository
                ).create(TabbedListReceiptsViewModel.class));
        doReturn(mHyperwallet).when(viewModel).getHyperwallet();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArgument(0);
                listener.onSuccess(mConfiguration);
                return listener;
            }
        }).when(mHyperwallet).getConfiguration(any(HyperwalletListener.class));
        viewModel.getProgramModel();
        assertThat(mConfiguration, is(notNullValue()));
        assertThat(mConfiguration.getProgramModel(), is("WALLET_MODEL"));
        assertThat(viewModel.getProgramModel(), is(notNullValue()));
    }


    @Test
    public void testGetProgramModel_nullConfiguration() {
        TabbedListReceiptsViewModel viewModel = spy(
                new TabbedListReceiptsViewModel.TabbedListReceiptsViewModelFactory(mUserRepository,
                        mPrepaidCardRepository
                ).create(TabbedListReceiptsViewModel.class));
        doReturn(mHyperwallet).when(viewModel).getHyperwallet();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArgument(0);
                listener.onSuccess(null);
                return listener;
            }
        }).when(mHyperwallet).getConfiguration(any(HyperwalletListener.class));
        viewModel.getProgramModel();
        assertThat(mConfiguration, is(notNullValue()));
        assertThat(mConfiguration.getProgramModel(), is("WALLET_MODEL"));
        assertThat(viewModel.getProgramModel(), is(nullValue()));
    }


    @Test
    public void testLoadPrepaidCards_loadCallBack() throws JSONException, HyperwalletException {
        final String responseJson = mResourceManager.getResourceContent("prepaid_cards_response.json");
        JSONObject jsonObject = new JSONObject(responseJson);
        final PageList<PrepaidCard> prepaidCardList = new PageList<>(jsonObject, PrepaidCard.class);
        final List<PrepaidCard> mPrepaidCardList = prepaidCardList.getDataList();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                PrepaidCardRepository.LoadPrepaidCardsCallback callback = invocation.getArgument(0);
                callback.onPrepaidCardListLoaded(mPrepaidCardList);
                return callback;
            }
        }).when(mPrepaidCardRepository).loadPrepaidCards(any(
                PrepaidCardRepository.LoadPrepaidCardsCallback.class));

        mTabbedListReceiptsViewModel.loadPrepaidCards();

        assertThat(mTabbedListReceiptsViewModel.getPrepaidCards(), is(notNullValue()));
        assertThat(mTabbedListReceiptsViewModel.getPrepaidCards().getValue(), is(notNullValue()));
        assertThat(mTabbedListReceiptsViewModel.getPrepaidCards().getValue().size(), is(2));
    }

    @Test
    public void testLoadPrepaidCards_errorCallback() throws JSONException, HyperwalletException {

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                PrepaidCardRepository.LoadPrepaidCardsCallback callback = invocation.getArgument(0);
                callback.onError(null);
                return callback;
            }
        }).when(mPrepaidCardRepository).loadPrepaidCards(any(
                PrepaidCardRepository.LoadPrepaidCardsCallback.class));
        mTabbedListReceiptsViewModel.loadPrepaidCards();
        assertThat(mTabbedListReceiptsViewModel.getPrepaidCards().getValue(), is(nullValue()));
    }
}
