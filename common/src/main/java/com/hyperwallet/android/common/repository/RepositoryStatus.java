package com.hyperwallet.android.common.repository;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@StringDef({
        RepositoryStatus.RepositoryQueryStatus.LOADING,
        RepositoryStatus.RepositoryQueryStatus.IDLE,
})
public @interface RepositoryStatus {

    final class RepositoryQueryStatus {
        public static final String LOADING = "LOADING";
        public static final String IDLE = "IDLE";
    }

}
