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
package com.hyperwallet.android.ui.transfermethod.view;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.ui.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class CurrencySelectionDialogFragment extends DialogFragment implements ToolbarEventListener {

    public static final String TAG = CurrencySelectionDialogFragment.class.getName();
    private static final String ARGUMENT_CURRENCY_NAME_CODE_MAP = "ARGUMENT_CURRENCY_NAME_CODE_MAP";
    private static final String ARGUMENT_SEARCH_CURRENCY_NAME_QUERY = "ARGUMENT_SEARCH_CURRENCY_NAME_QUERY";
    private static final String ARGUMENT_SELECTED_CURRENCY_NAME = "ARGUMENT_SELECTED_CURRENCY_NAME";
    private static final int MAX_NO_SEARCH_COUNT = 20;

    private Adapter mAdapter;
    private TreeMap<String, String> mCurrencyNameCodeMap;
    private CurrencySelectionItemClickListener mCurrencySelectionItemClickListener;
    private String mSearchCurrencyNameQuery;
    private String mSelectedCurrencyName;
    private RecyclerView mRecyclerView;

    public static CurrencySelectionDialogFragment newInstance(
            @NonNull final TreeMap<String, String> currencyNameCodeMap,
            @NonNull final String selectedCurrencyName) {

        CurrencySelectionDialogFragment currencySelectionDialogFragment = new CurrencySelectionDialogFragment();
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
            mCurrencySelectionItemClickListener = (CurrencySelectionItemClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement "
                    + CurrencySelectionItemClickListener.class.getCanonicalName());
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_currency_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.currency_selection_toolbar);
        toolbar.setTitle(R.string.mobileCurrencyLabel);
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
            mCurrencyNameCodeMap = new TreeMap<>(
                    (Map<String, String>) savedInstanceState.getSerializable(ARGUMENT_CURRENCY_NAME_CODE_MAP));
            mSelectedCurrencyName = savedInstanceState.getString(ARGUMENT_SELECTED_CURRENCY_NAME);
            mSearchCurrencyNameQuery = savedInstanceState.getString(ARGUMENT_SEARCH_CURRENCY_NAME_QUERY);
        } else {
            mCurrencyNameCodeMap = (TreeMap) getArguments().getSerializable(ARGUMENT_CURRENCY_NAME_CODE_MAP);
            mSelectedCurrencyName = getArguments().getString(ARGUMENT_SELECTED_CURRENCY_NAME);
            mSearchCurrencyNameQuery = getArguments().getString(ARGUMENT_SEARCH_CURRENCY_NAME_QUERY);
        }
        setHasOptionsMenu(mCurrencyNameCodeMap.size() > MAX_NO_SEARCH_COUNT);
        mAdapter = new Adapter(mCurrencyNameCodeMap, mSelectedCurrencyName, mCurrencySelectionItemClickListener, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.scrollToPosition(mAdapter.getPositionFromCurrencyName(mSelectedCurrencyName));
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_currency_selection, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.currency_selection_search_item);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchCurrencyNameQuery = query;
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSearchCurrencyNameQuery = newText;
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });

        if (!mSearchCurrencyNameQuery.isEmpty()) {
            searchView.clearFocus();
            searchItem.expandActionView();
            searchView.setQuery(mSearchCurrencyNameQuery, true);
        }
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

    public interface CurrencySelectionItemClickListener {
        void onCurrencyItemClicked(String currencyCode);
    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> implements Filterable {

        private TreeMap<String, String> mCurrencyNameCodeFilteredMap;
        private TreeMap<String, String> mCurrencyNameCodeMap;
        private List<String> mCurrencyNames;
        private CurrencySelectionItemClickListener mCurrencySelectionItemClickListener;
        private String mSelectedCurrencyName;
        private ToolbarEventListener mToolbarEventListener;

        Adapter(final TreeMap<String, String> currencyNameCodeMap, final String selectedCurrencyName,
                final CurrencySelectionItemClickListener currencySelectionItemClickListener,
                final ToolbarEventListener toolbarEventListener) {
            mCurrencyNames = new ArrayList<>(currencyNameCodeMap.keySet());
            mCurrencyNameCodeMap = currencyNameCodeMap;
            mSelectedCurrencyName = selectedCurrencyName;
            mCurrencySelectionItemClickListener = currencySelectionItemClickListener;
            mToolbarEventListener = toolbarEventListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View itemCurrencyView = layoutInflater.inflate(R.layout.item_currency, parent, false);

            return new ViewHolder(itemCurrencyView, mCurrencySelectionItemClickListener);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String currencyName = mCurrencyNames.get(position);
            holder.bind(currencyName);
        }

        @Override
        public int getItemCount() {
            return mCurrencyNames.size();
        }

        @Override
        public void onViewRecycled(@NonNull ViewHolder holder) {
            holder.recycle();
        }

        String getItemCurrencyCode(int position) {
            return mCurrencyNameCodeMap.get(mCurrencyNames.get(position));
        }

        int getPositionFromCurrencyName(@NonNull String currencyName) {
            for (int i = 0; i < mCurrencyNames.size(); i++) {
                if (currencyName.equals(mCurrencyNames.get(i))) {
                    return i;
                }
            }
            return 0;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                private TreeMap<String, String> currencyNameCodeFiltered;

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    if (constraint.length() == 0) {
                        currencyNameCodeFiltered = mCurrencyNameCodeMap;
                    } else {
                        currencyNameCodeFiltered = new TreeMap<>();
                        for (String countryName : mCurrencyNameCodeMap.keySet()) {
                            if (countryName.toLowerCase(Locale.ROOT).contains(
                                    constraint.toString().toLowerCase(Locale.ROOT))) {
                                currencyNameCodeFiltered.put(countryName, mCurrencyNameCodeMap.get(countryName));
                            }
                        }
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = currencyNameCodeFiltered;
                    filterResults.count = currencyNameCodeFiltered.size();
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    mCurrencyNameCodeFilteredMap = (TreeMap<String, String>) results.values;
                    mCurrencyNames = new ArrayList<>(mCurrencyNameCodeFilteredMap.keySet());
                    notifyDataSetChanged();
                }
            };
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final TextView mCurrencyName;
            private final TextView mCurrencyCode;
            private final ImageView mCurrencyItemSelectedImage;
            private final CurrencySelectionItemClickListener mCurrencySelectionItemClickListener;

            ViewHolder(@NonNull final View itemView,
                    @NonNull final CurrencySelectionItemClickListener currencySelectionItemClickListener) {
                super(itemView);
                itemView.setOnClickListener(this);

                mCurrencyName = itemView.findViewById(R.id.currency_name);
                mCurrencyCode = itemView.findViewById(R.id.currency_code);
                mCurrencyItemSelectedImage = itemView.findViewById(R.id.currency_item_selected_image);
                mCurrencySelectionItemClickListener = currencySelectionItemClickListener;
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                String currencyCode = getItemCurrencyCode(position);
                mToolbarEventListener.hideSoftKey(v);
                mToolbarEventListener.onClose();
                mCurrencySelectionItemClickListener.onCurrencyItemClicked(currencyCode);
            }

            void bind(String currencyName) {
                itemView.setOnClickListener(this);
                mCurrencyName.setText(currencyName);
                mCurrencyCode.setText(mCurrencyNameCodeMap.get(currencyName));
                if (currencyName.equals(mSelectedCurrencyName)) {
                    mCurrencyItemSelectedImage.setVisibility(View.VISIBLE);
                    mCurrencyCode.setVisibility(View.GONE);
                } else {
                    mCurrencyItemSelectedImage.setVisibility(View.GONE);
                    mCurrencyCode.setVisibility(View.VISIBLE);
                }
            }

            void recycle() {
                itemView.setOnClickListener(null);
            }
        }
    }
}
