/*
 * Copyright 2018 Hyperwallet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.hyperwallet.android.ui.repository;

import com.hyperwallet.android.ui.user.repository.UserRepository;
import com.hyperwallet.android.ui.user.repository.UserRepositoryImpl;

public class RepositoryFactory {
    private static RepositoryFactory sInstance;
    private TransferMethodConfigurationRepository mTransferMethodConfigurationRepository;
    private TransferMethodRepository mTransferMethodRepository;
    private UserRepository mUserRepository;

    private RepositoryFactory() {
        mTransferMethodConfigurationRepository = new TransferMethodConfigurationRepositoryImpl();
        mTransferMethodRepository = new TransferMethodRepositoryImpl();
        mUserRepository = new UserRepositoryImpl();
    }

    public static synchronized RepositoryFactory getInstance() {
        if (sInstance == null) {
            sInstance = new RepositoryFactory();
        }
        return sInstance;
    }

    public static void clearInstance() {
        sInstance = null;
    }

    public TransferMethodConfigurationRepository getTransferMethodConfigurationRepository() {
        return mTransferMethodConfigurationRepository;
    }

    public TransferMethodRepository getTransferMethodRepository() {
        return mTransferMethodRepository;
    }

    public UserRepository getUserRepository() {
        return mUserRepository;
    }
}
