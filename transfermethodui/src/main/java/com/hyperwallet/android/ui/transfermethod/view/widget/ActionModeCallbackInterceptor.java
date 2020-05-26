package com.hyperwallet.android.ui.transfermethod.view.widget;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

public class ActionModeCallbackInterceptor implements ActionMode.Callback {

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }
}
