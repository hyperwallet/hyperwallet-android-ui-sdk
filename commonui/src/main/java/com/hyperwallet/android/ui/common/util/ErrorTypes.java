package com.hyperwallet.android.ui.common.util;

import static com.hyperwallet.android.ExceptionMapper.EC_AUTHENTICATION_TOKEN_PROVIDER_EXCEPTION;
import static com.hyperwallet.android.ExceptionMapper.EC_IO_EXCEPTION;
import static com.hyperwallet.android.ExceptionMapper.EC_JSON_EXCEPTION;
import static com.hyperwallet.android.ExceptionMapper.EC_JSON_PARSE_EXCEPTION;
import static com.hyperwallet.android.ExceptionMapper.EC_UNEXPECTED_EXCEPTION;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorTypes {
    // Error Tags
    public static final String ERROR_CODE = "error_code";
    public static final String ERROR_TYPE = "error_type";
    public static final String ERROR_MESSAGE = "error_message";
    public static final String ERROR_FIELD_NAME = "erfd";
    public static final String ERROR_DESCRIPTION = "error_description";

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

    public static String getErrorType(String errorCode) {
        switch (errorCode) {
            case EC_UNEXPECTED_EXCEPTION:
            case EC_JSON_EXCEPTION:
            case EC_JSON_PARSE_EXCEPTION:
            case EC_AUTHENTICATION_TOKEN_PROVIDER_EXCEPTION:
                errorCode = SDK_ERROR;
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
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        new Exception("Insights detected error").printStackTrace(printWriter);
        String stacked = writer.toString();
        printWriter.close();

        return stacked;
    }
}
