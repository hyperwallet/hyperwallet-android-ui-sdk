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
package com.hyperwallet.android.ui.common.util;

import static com.hyperwallet.android.ExceptionMapper.EC_AUTHENTICATION_TOKEN_PROVIDER_EXCEPTION;
import static com.hyperwallet.android.ExceptionMapper.EC_IO_EXCEPTION;
import static com.hyperwallet.android.ExceptionMapper.EC_JSON_EXCEPTION;
import static com.hyperwallet.android.ExceptionMapper.EC_JSON_PARSE_EXCEPTION;
import static com.hyperwallet.android.ExceptionMapper.EC_UNEXPECTED_EXCEPTION;

import android.util.Log;

public class ErrorTypes {
    /**
     * SDK_ERROR indicates an unexpected error the component could not handle.
     */
    public static final String SDK_ERROR = "EXCEPTION";

    /**
     * CONNECTION_ERROR indicates an error relating to network connectivity.
     */
    public static final String CONNECTION_ERROR = "CONNECTION";

    /**
     * API_ERROR indicates an expected or caught error due to user action.
     */
    public static final String API_ERROR = "API";

    /**
     * FORM_ERROR indicates an expected error that is coming from a form.
     */
    public static final String FORM_ERROR = "FORM";

    /**
     * AUTH_TOKEN_ERROR indicates an expected error that is coming when authentication token error occurred.
     */
    public static final String AUTH_TOKEN_ERROR = "AUTH_TOKEN_ERROR";

    public static String getErrorType(String errorCode) {
        switch (errorCode) {
            case EC_UNEXPECTED_EXCEPTION:
            case EC_JSON_EXCEPTION:
            case EC_JSON_PARSE_EXCEPTION:
                errorCode = SDK_ERROR;
                break;
            case EC_AUTHENTICATION_TOKEN_PROVIDER_EXCEPTION:
                errorCode = AUTH_TOKEN_ERROR;
                break;
            case EC_IO_EXCEPTION: // connection error
                errorCode = CONNECTION_ERROR;
                break;
            default: // normal rest errors, we will give the user a chance to fix input values from form
                errorCode = API_ERROR;
        }
        return errorCode;
    }

    public static String getStackTrace() {
        return Log.getStackTraceString(new Exception("Insights detected error"));
    }
}
