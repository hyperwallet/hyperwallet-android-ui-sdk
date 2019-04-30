package com.hyperwallet.android.ui.view.error;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.hyperwallet.android.ExceptionMapper.EC_AUTHENTICATION_TOKEN_PROVIDER_EXCEPTION;
import static com.hyperwallet.android.ExceptionMapper.EC_IO_EXCEPTION;
import static com.hyperwallet.android.ExceptionMapper.EC_JSON_EXCEPTION;
import static com.hyperwallet.android.ExceptionMapper.EC_JSON_PARSE_EXCEPTION;
import static com.hyperwallet.android.ExceptionMapper.EC_UNEXPECTED_EXCEPTION;

import android.content.res.Resources;

import com.hyperwallet.android.hyperwallet_ui.R;
import com.hyperwallet.android.model.HyperwalletError;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class DefaultErrorDialogFragmentPresenterTest {

    private final DefaultErrorDialogFragmentPresenter presenter = new DefaultErrorDialogFragmentPresenter();

    @Test
    public void testBuildDialogMessage_buildIoExceptionMessage() {
        Resources resources = mock(Resources.class);
        List<HyperwalletError> errors = new ArrayList<>(2);
        errors.add(new HyperwalletError(R.string.authentication_token_provider_exception, EC_IO_EXCEPTION));
        when(resources.getString(R.string.authentication_token_provider_exception)).thenReturn("My IO message");
        String message = presenter.buildDialogMessage(errors, resources);

        assertThat(message, is("My IO message"));
    }

    @Test
    @Parameters(method = "testBuildCommonDialogMessageData")
    public void testBuildDialogMessage_buildCommonExceptionMessage(String errorCode) {
        Resources resources = mock(Resources.class);
        List<HyperwalletError> errors = new ArrayList<>(2);
        errors.add(new HyperwalletError(errorCode, errorCode));
        when(resources.getString(R.string.unexpected_exception)).thenReturn("Unexpected");
        String message = presenter.buildDialogMessage(errors, resources);

        assertThat(message, is("Unexpected"));
    }

    @Test
    public void testBuildDialogMessage_buildDefaultExceptionMessage() {
        Resources resources = mock(Resources.class);
        List<HyperwalletError> errors = new ArrayList<>(2);
        errors.add(new HyperwalletError("My default message", "my error code"));
        String message = presenter.buildDialogMessage(errors, resources);

        verify(resources, never()).getString(anyInt());
        assertThat(message, is("My default message"));
    }

    private Collection<String> testBuildCommonDialogMessageData() {
        return Arrays.asList(EC_UNEXPECTED_EXCEPTION,
                EC_JSON_EXCEPTION,
                EC_JSON_PARSE_EXCEPTION,
                EC_AUTHENTICATION_TOKEN_PROVIDER_EXCEPTION);
    }
}
