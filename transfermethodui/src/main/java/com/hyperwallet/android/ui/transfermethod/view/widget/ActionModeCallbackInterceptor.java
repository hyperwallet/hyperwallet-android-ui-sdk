package com.hyperwallet.android.ui.transfermethod.view.widget;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

public class ActionModeCallbackInterceptor implements ActionMode.Callback {

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        try {
            CharSequence title = menu.findItem(android.R.id.paste).getTitle();
            menu.clear();
            menu.add(0, android.R.id.paste, 0, title);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }
}
