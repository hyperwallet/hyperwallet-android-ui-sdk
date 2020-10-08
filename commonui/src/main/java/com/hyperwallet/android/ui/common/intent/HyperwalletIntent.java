/*
 * Copyright 2019 Hyperwallet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package com.hyperwallet.android.ui.common.intent;

/**
 * Hyperwallet Intents used for inter SDK communication
 */
public final class HyperwalletIntent {

    private HyperwalletIntent() {
    }

    /**
     * SDK Broadcast Payload Key
     */
    public static final String HYPERWALLET_LOCAL_BROADCAST_PAYLOAD_KEY = "hyperwallet-local-broadcast-payload";

    /**
     * SDK Error unavailable constant
     */
    public static final String ERROR_SDK_MODULE_UNAVAILABLE = "ERROR_SDK_MODULE_UNAVAILABLE";

    /**
     * Action for SELECT TRANSFER METHOD UI SDK
     */
    public static final String ACTION_SELECT_TRANSFER_METHOD = "com.hyperwallet.intent.action.SELECT_TRANSFER_METHOD";

    /**
     * Select Transfer method request code
     */
    public static final short SELECT_TRANSFER_METHOD_REQUEST_CODE = 100;

    /**
     * Add Transfer method request code
     */
    public static final short ADD_TRANSFER_METHOD_REQUEST_CODE = 101;

    /**
     * Select transfer destination request code
     */
    public static final short SELECT_TRANSFER_DESTINATION_REQUEST_CODE = 102;

    /**
     * Schedule transfer
     */
    public static final short SCHEDULE_TRANSFER_REQUEST_CODE = 103;

    /**
     * Select transfer source request code
     */
    public static final short SELECT_TRANSFER_SOURCE_REQUEST_CODE = 104;

    /**
     * SDK Broadcast payload error
     */
    public static final String AUTHENTICATION_ERROR_PAYLOAD = "HYPERWALLET_AUTHENTICATION_ERROR_PAYLOAD";

    /**
     * SDK Broadcast error action
     */
    public static final String AUTHENTICATION_ERROR_ACTION = "HYPERWALLET_AUTHENTICATION_ERROR_ACTION";


    /**
     * Transfer method added, extra activity parcelable transfer method payload
     */
    public static final String EXTRA_TRANSFER_METHOD_ADDED = "EXTRA_TRANSFER_METHOD_ADDED";


}
