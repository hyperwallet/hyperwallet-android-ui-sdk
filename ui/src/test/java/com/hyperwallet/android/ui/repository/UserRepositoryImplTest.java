package com.hyperwallet.android.ui.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static com.hyperwallet.android.model.HyperwalletUser.ProfileTypes.INDIVIDUAL;
import static com.hyperwallet.android.model.HyperwalletUser.UserStatuses.PRE_ACTIVATED;
import static com.hyperwallet.android.model.HyperwalletUser.VerificationStatuses.NOT_REQUIRED;

import com.hyperwallet.android.Hyperwallet;
import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.listener.HyperwalletListener;
import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.model.HyperwalletErrors;
import com.hyperwallet.android.model.HyperwalletUser;

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
public class UserRepositoryImplTest {
    @Mock
    private Hyperwallet mHyperwallet;
    @Mock
    UserRepository.LoadUserCallback mMockCallback;
    @Captor
    private ArgumentCaptor<HyperwalletErrors> mErrorCaptor;
    @Captor
    private ArgumentCaptor<HyperwalletUser> mUserCaptor;
    @Rule
    public MockitoRule mMockito = MockitoJUnit.rule();
    @Spy
    UserRepositoryImpl mUserRepository;

    @Before
    public void setup() {
        doReturn(mHyperwallet).when(mUserRepository).getHyperwallet();
    }

    @Test
    public void testLoadUser_returnsUser() {
        HyperwalletUser.Builder builder = new HyperwalletUser.Builder();
        final HyperwalletUser user = builder
                .token("usr-f9154016-94e8-4686-a840-075688ac07b5")
                .status(PRE_ACTIVATED)
                .verificationStatus(NOT_REQUIRED)
                .createdOn("2017-10-30T22:15:45")
                .clientUserId("123456")
                .profileType(INDIVIDUAL)
                .firstName("Some")
                .lastName("Guy")
                .dateOfBirth("1991-01-01")
                .email("testUser@hyperwallet.com")
                .addressLine1("575 Market Street")
                .city("San Francisco")
                .stateProvince("CA")
                .country("US")
                .postalCode("94105")
                .language("en")
                .programToken("prg-83836cdf-2ce2-4696-8bc5-f1b86077238c")
                .build();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[0];
                listener.onSuccess(user);
                return listener;
            }
        }).when(mHyperwallet).getUser(ArgumentMatchers.<HyperwalletListener<HyperwalletUser>>any());

        mUserRepository.loadUser(mMockCallback);

        verify(mMockCallback).onUserLoaded(mUserCaptor.capture());
        verify(mMockCallback, never()).onError(any(HyperwalletErrors.class));

        HyperwalletUser resultUser = mUserCaptor.getValue();
        assertThat(resultUser.getToken(), is("usr-f9154016-94e8-4686-a840-075688ac07b5"));
        assertThat(resultUser.getStatus(), is(PRE_ACTIVATED));
        assertThat(resultUser.getVerificationStatus(), is(NOT_REQUIRED));
        assertThat(resultUser.getCreatedOn(), is("2017-10-30T22:15:45"));
        assertThat(resultUser.getClientUserId(), is("123456"));
        assertThat(resultUser.getProfileType(), is(INDIVIDUAL));
        assertThat(resultUser.getFirstName(), is("Some"));
        assertThat(resultUser.getLastName(), is("Guy"));
        assertThat(resultUser.getDateOfBirth(), is("1991-01-01"));
        assertThat(resultUser.getEmail(), is("testUser@hyperwallet.com"));
        assertThat(resultUser.getAddressLine1(), is("575 Market Street"));
        assertThat(resultUser.getCity(), is("San Francisco"));
        assertThat(resultUser.getStateProvince(), is("CA"));
        assertThat(resultUser.getCountry(), is("US"));
        assertThat(resultUser.getPostalCode(), is("94105"));
        assertThat(resultUser.getLanguage(), is("en"));
        assertThat(resultUser.getProgramToken(), is("prg-83836cdf-2ce2-4696-8bc5-f1b86077238c"));
    }

    @Test
    public void testLoadUser_returnsNoUser() {

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[0];
                listener.onSuccess(null);
                return listener;
            }
        }).when(mHyperwallet).getUser(ArgumentMatchers.<HyperwalletListener<HyperwalletUser>>any());

        mUserRepository.loadUser(mMockCallback);

        verify(mMockCallback).onUserLoaded(mUserCaptor.capture());
        verify(mMockCallback, never()).onError(any(HyperwalletErrors.class));

        HyperwalletUser user = mUserCaptor.getValue();
        assertThat(user, is(nullValue()));
    }


    @Test
    public void testLoadUser_withError() {

        final HyperwalletError error = new HyperwalletError("test message", "TEST_CODE");

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[0];
                List<HyperwalletError> errorList = new ArrayList<>();
                errorList.add(error);
                HyperwalletErrors errors = new HyperwalletErrors(errorList);
                listener.onFailure(new HyperwalletException(errors));
                return listener;
            }
        }).when(mHyperwallet).getUser(ArgumentMatchers.<HyperwalletListener<HyperwalletUser>>any());

        mUserRepository.loadUser(mMockCallback);

        verify(mMockCallback, never()).onUserLoaded(ArgumentMatchers.<HyperwalletUser>any());
        verify(mMockCallback).onError(mErrorCaptor.capture());

        assertThat(mErrorCaptor.getValue().getErrors(), hasItem(error));
    }

    @Test
    public void testRefreshUser_checkHyperwalletCallGetUser() {
        HyperwalletUser.Builder builder = new HyperwalletUser.Builder();
        final HyperwalletUser user = builder
                .token("usr-f9154016-94e8-4686-a840-075688ac07b5")
                .profileType(INDIVIDUAL)
                .build();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[0];
                listener.onSuccess(user);
                return listener;
            }
        }).when(mHyperwallet).getUser(ArgumentMatchers.<HyperwalletListener<HyperwalletUser>>any());

        mUserRepository.loadUser(mMockCallback);

        verify(mHyperwallet).getUser(ArgumentMatchers.<HyperwalletListener<HyperwalletUser>>any());

        mUserRepository.loadUser(mMockCallback);
        verify(mHyperwallet).getUser(ArgumentMatchers.<HyperwalletListener<HyperwalletUser>>any());

        mUserRepository.refreshUser();
        mUserRepository.loadUser(mMockCallback);
        verify(mHyperwallet, times(2)).getUser(ArgumentMatchers.<HyperwalletListener<HyperwalletUser>>any());

    }
}
