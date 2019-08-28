package com.hyperwallet.android.ui.common.view;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.hyperwallet.android.model.HyperwalletError;
import com.hyperwallet.android.ui.common.view.error.DefaultErrorDialogFragment;

import java.util.List;

public final class ActivityUtils {

    private ActivityUtils() {
    }
    
    /**
     * Initialize the fragment for Activity
     *
     * @param fragmentActivity specify context of the Fragment
     * @param fragment specify desired fragment
     * @param layout specify desired layout
     */
    public static void initFragment(@NonNull final FragmentActivity fragmentActivity, @NonNull final Fragment fragment,
                                    final int layout){
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(layout, fragment);
        fragmentTransaction.commit();
    }

    /**
     * Create and display the error dialog
     *
     * @param fragmentActivity specify context of the Fragment
     * @param errors specify the errors
     */
    public static void showError(@NonNull final FragmentActivity fragmentActivity,
                                 @NonNull final List<HyperwalletError> errors){
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        DefaultErrorDialogFragment fragment = (DefaultErrorDialogFragment)
                fragmentManager.findFragmentByTag(DefaultErrorDialogFragment.TAG);

        if (fragment == null) {
            fragment = DefaultErrorDialogFragment.newInstance(errors);
        }

        if (!fragment.isAdded()) {
            fragment.show(fragmentManager);
        }
    }
}
