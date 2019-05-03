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
import static org.mockito.Mockito.verify;

import static com.hyperwallet.android.model.HyperwalletUser.ProfileTypes.INDIVIDUAL;
import static com.hyperwallet.android.model.HyperwalletUser.UserFields.CLIENT_USER_ID;
import static com.hyperwallet.android.model.HyperwalletUser.UserFields.CREATED_ON;
import static com.hyperwallet.android.model.HyperwalletUser.UserFields.PROFILE_TYPE;
import static com.hyperwallet.android.model.HyperwalletUser.UserFields.STATUS;
import static com.hyperwallet.android.model.HyperwalletUser.UserFields.TOKEN;
import static com.hyperwallet.android.model.HyperwalletUser.UserFields.VERIFICATION_STATUS;
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
    public void testLoadUser_returnValidData() {
        HyperwalletUser.Builder builder = new HyperwalletUser.Builder();
        final HyperwalletUser user = builder
                .token("usr-f9154016-94e8-4686-a840-075688ac07b5")
                .status(PRE_ACTIVATED)
                .verificationStatus(NOT_REQUIRED)
                .createdOn("2017-10-30T22:15:45")
                .clientUserId("CSK7b8Ffch")
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
        UserRepository.LoadUserCallback mockCallback = mock(UserRepository.LoadUserCallback.class);

        mUserRepository.loadUser(mockCallback);

        verify(mockCallback).onUserLoaded(mUserCaptor.capture());
        verify(mockCallback, never()).onError(any(HyperwalletErrors.class));

        HyperwalletUser resultUser = mUserCaptor.getValue();
        assertThat(user.getField(TOKEN), is("usr-f9154016-94e8-4686-a840-075688ac07b5"));
        assertThat(user.getField(STATUS), is(PRE_ACTIVATED));
        assertThat(user.getField(VERIFICATION_STATUS), is(NOT_REQUIRED));
        assertThat(user.getField(CREATED_ON), is("2017-10-30T22:15:45"));
        assertThat(user.getField(CLIENT_USER_ID), is("CSK7b8Ffch"));
        assertThat(user.getField(PROFILE_TYPE), is(INDIVIDUAL));
        assertThat(user.getField(HyperwalletUser.UserFields.FIRST_NAME), is("Some"));
        assertThat(user.getField(HyperwalletUser.UserFields.LAST_NAME), is("Guy"));
        assertThat(user.getField(HyperwalletUser.UserFields.DATE_OF_BIRTH), is("1991-01-01"));
        assertThat(user.getField(HyperwalletUser.UserFields.EMAIL), is("testUser@hyperwallet.com"));
        assertThat(user.getField(HyperwalletUser.UserFields.ADDRESS_LINE_1), is("575 Market Street"));
        assertThat(user.getField(HyperwalletUser.UserFields.CITY), is("San Francisco"));
        assertThat(user.getField(HyperwalletUser.UserFields.STATE_PROVINCE), is("CA"));
        assertThat(user.getField(HyperwalletUser.UserFields.COUNTRY), is("US"));
        assertThat(user.getField(HyperwalletUser.UserFields.POSTAL_CODE), is("94105"));
        assertThat(user.getField(HyperwalletUser.UserFields.LANGUAGE), is("en"));
        assertThat(user.getField(HyperwalletUser.UserFields.PROGRAM_TOKEN),
                is("prg-83836cdf-2ce2-4696-8bc5-f1b86077238c"));
    }

    @Test
    public void testLoadUser_returnEmptyUserData() {

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                HyperwalletListener listener = (HyperwalletListener) invocation.getArguments()[0];
                listener.onSuccess(null);
                return listener;
            }
        }).when(mHyperwallet).getUser(ArgumentMatchers.<HyperwalletListener<HyperwalletUser>>any());
        UserRepository.LoadUserCallback mockCallback = mock(UserRepository.LoadUserCallback.class);

        mUserRepository.loadUser(mockCallback);

        verify(mockCallback).onUserLoaded(mUserCaptor.capture());
        verify(mockCallback, never()).onError(any(HyperwalletErrors.class));

        HyperwalletUser user = mUserCaptor.getValue();
        assertThat(user, is(nullValue()));
    }


    @Test
    public void testLoadUser_returnErrors() {

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
        UserRepository.LoadUserCallback mockCallback = mock(
                UserRepository.LoadUserCallback.class);

        mUserRepository.loadUser(mockCallback);

        verify(mockCallback, never()).onUserLoaded(ArgumentMatchers.<HyperwalletUser>any());
        verify(mockCallback).onError(mErrorCaptor.capture());

        assertThat(mErrorCaptor.getValue().getErrors(), hasItem(error));
    }
}
