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
package com.hyperwallet.android.ui.transfer.view;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.ui.common.view.ToolbarEventListener;
import com.hyperwallet.android.ui.transfer.R;

import java.util.TreeMap;

public class ListTransferDestinationFragment extends DialogFragment implements ToolbarEventListener {

    public static final String TAG = ListTransferDestinationFragment.class.getSimpleName();
    private static final String ARGUMENT_CURRENCY_NAME_CODE_MAP = "ARGUMENT_CURRENCY_NAME_CODE_MAP";
    private static final String ARGUMENT_SEARCH_CURRENCY_NAME_QUERY = "ARGUMENT_SEARCH_CURRENCY_NAME_QUERY";
    private static final String ARGUMENT_SELECTED_CURRENCY_NAME = "ARGUMENT_SELECTED_CURRENCY_NAME";
    private static final int MAX_NO_SEARCH_COUNT = 20;

    private ListTransferDestinationAdapter mAdapter;
    private DestinationItemClickListener mDestinationItemClickListener;
    private String mSearchCurrencyNameQuery;
    private String mSelectedCurrencyName;
    private RecyclerView mRecyclerView;

    public static ListTransferDestinationFragment newInstance(
            @NonNull final TreeMap<String, String> currencyNameCodeMap,
            @NonNull final String selectedCurrencyName) {

        ListTransferDestinationFragment currencySelectionDialogFragment = new ListTransferDestinationFragment();
        currencySelectionDialogFragment.mCurrencyNameCodeMap = currencyNameCodeMap;
        currencySelectionDialogFragment.mSelectedCurrencyName = selectedCurrencyName;
        currencySelectionDialogFragment.mSearchCurrencyNameQuery = "";

        Bundle bundle = new Bundle();
        bundle.putSerializable(ARGUMENT_CURRENCY_NAME_CODE_MAP, currencySelectionDialogFragment.mCurrencyNameCodeMap);
        bundle.putString(ARGUMENT_SELECTED_CURRENCY_NAME, currencySelectionDialogFragment.mSelectedCurrencyName);
        bundle.putString(ARGUMENT_SEARCH_CURRENCY_NAME_QUERY, currencySelectionDialogFragment.mSearchCurrencyNameQuery);
        currencySelectionDialogFragment.setArguments(bundle);

        return currencySelectionDialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mDestinationItemClickListener = (DestinationItemClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement "
                    + DestinationItemClickListener.class.getCanonicalName());
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_transfer_destination, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.currency_selection_toolbar);
        toolbar.setTitle(R.string.destination);
        toolbar.setNavigationIcon(R.drawable.ic_close_14dp);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKey(v);
                onClose();
                getFragmentManager().popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                dismiss();
            }
        });

        onView();
        mRecyclerView = view.findViewById(R.id.currency_selection_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            mSelectedCurrencyName = savedInstanceState.getString(ARGUMENT_SELECTED_CURRENCY_NAME);
            mSearchCurrencyNameQuery = savedInstanceState.getString(ARGUMENT_SEARCH_CURRENCY_NAME_QUERY);
        } else {
            mSelectedCurrencyName = getArguments().getString(ARGUMENT_SELECTED_CURRENCY_NAME);
            mSearchCurrencyNameQuery = getArguments().getString(ARGUMENT_SEARCH_CURRENCY_NAME_QUERY);
        }

        mAdapter = new ListTransferDestinationAdapter(mCurrencyNameCodeMap, mSelectedCurrencyName,
                mDestinationItemClickListener, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(ARGUMENT_CURRENCY_NAME_CODE_MAP, mCurrencyNameCodeMap);
        outState.putString(ARGUMENT_SELECTED_CURRENCY_NAME, mSelectedCurrencyName);
        outState.putString(ARGUMENT_SEARCH_CURRENCY_NAME_QUERY, mSearchCurrencyNameQuery);
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClose() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
    }

    @Override
    public void onView() {
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getActivity().getWindow().setStatusBarColor(
                    ContextCompat.getColor(getContext(), R.color.regularColorPrimary));
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    @Override
    public void hideSoftKey(@NonNull View focusedView) {
        InputMethodManager inputMethodManager = (InputMethodManager) focusedView.getContext().getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
    }

    public interface DestinationItemClickListener {
        void onDestinationItemClicked(int position);
    }
}
