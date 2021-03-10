package com.hyperwallet.android.ui.balance.repository;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.Errors;
import com.hyperwallet.android.model.balance.Balance;
import com.hyperwallet.android.model.balance.BalanceQueryParam;
import com.hyperwallet.android.model.paging.PageList;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class UserBalanceRepositoryImplTest {
    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();

    @Rule
    public HyperwalletExternalResourceManager mResourceManager = new HyperwalletExternalResourceManager();

    @Mock
    UserBalanceRepository.LoadUserBalanceListCallback loadUserBalanceListCallback;
    @Spy
    UserBalanceRepositoryImpl userBalanceRepository;
    @Mock
    private Hyperwallet mHyperwallet;
    @Captor
    private ArgumentCaptor<Errors> mErrorCaptor;
    @Captor
    private ArgumentCaptor<List<Balance>> mUserBalanceCaptor;

    @Before
    public void setup() {
        doReturn(mHyperwallet).when(userBalanceRepository).getHyperwallet();
    }

    @Test
    public void testLoadUserBalances_returnsBalances() throws JSONException, HyperwalletException {

        final String response = mResourceManager.getResourceContent("user_balances.json");
        JSONObject jObject = new JSONObject(response);
        final PageList<Balance> balances = new PageList<>(jObject, Balance.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(balances);
                return listener;
            }
        }).when(mHyperwallet).listUserBalances((BalanceQueryParam) any(), any(HyperwalletListener.class));

        userBalanceRepository.loadUserBalances(loadUserBalanceListCallback);
        verify(userBalanceRepository).getHyperwallet();
        verify(loadUserBalanceListCallback).onUserBalanceListLoaded(mUserBalanceCaptor.capture());
        verify(loadUserBalanceListCallback, never()).onError(any(Errors.class));

        assertThat(mUserBalanceCaptor.getValue().size(),is(2));

    }

    @Test
    public void testLoadUserBalances_returnsEmptyBalances() {

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                listener.onSuccess(null);
                return listener;
            }
        }).when(mHyperwallet).listUserBalances((BalanceQueryParam) any(), any(HyperwalletListener.class));

        userBalanceRepository.loadUserBalances(loadUserBalanceListCallback);
        verify(userBalanceRepository).getHyperwallet();
        verify(loadUserBalanceListCallback).onUserBalanceListLoaded(mUserBalanceCaptor.capture());
        verify(loadUserBalanceListCallback, never()).onError(any(Errors.class));

        assertThat(mUserBalanceCaptor.getValue().isEmpty(),is(true));
    }

    @Test
    public void testLoadUserBalances_onFailure() {

        final Error error = new Error("test message", "TEST_CODE");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[1];
                List<Error> errorList = new ArrayList<>();
                errorList.add(error);
                Errors errors = new Errors(errorList);
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).listUserBalances((BalanceQueryParam) any(), any(HyperwalletListener.class));

        userBalanceRepository.loadUserBalances(loadUserBalanceListCallback);

        verify(userBalanceRepository).getHyperwallet();
        verify(loadUserBalanceListCallback, never()).onUserBalanceListLoaded(ArgumentMatchers.<Balance>anyList());
        verify(loadUserBalanceListCallback).onError(mErrorCaptor.capture());

        assertThat(mErrorCaptor.getValue().getErrors(),hasItem(error));

    }
}
