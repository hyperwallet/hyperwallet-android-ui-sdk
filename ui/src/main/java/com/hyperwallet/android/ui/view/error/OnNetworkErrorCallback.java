package com.hyperwallet.android.ui.view.error;

import java.util.List;

/**
 * Retry callback @see {@link DefaultErrorDialogFragment#newInstance(List)}
 * */
public interface OnNetworkErrorCallback {
    /**
     * Gets invoked when Error occurred and its possible to retry operation
     * */
    void retry();
}
