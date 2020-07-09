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

public class CountrySelectionDialogFragment extends DialogFragment implements ToolbarEventListener {

    public static final String TAG = CountrySelectionDialogFragment.class.getName();
    private static final String ARGUMENT_COUNTRY_NAME_CODE_MAP = "ARGUMENT_COUNTRY_NAME_CODE_MAP";
    private static final String ARGUMENT_SEARCH_COUNTRY_NAME_QUERY = "ARGUMENT_SEARCH_COUNTRY_NAME_QUERY";
    private static final String ARGUMENT_SELECTED_COUNTRY_NAME = "ARGUMENT_SELECTED_COUNTRY_NAME";
    private static final int MAX_NO_SEARCH_COUNT = 20;

    private Adapter mAdapter;
    private TreeMap<String, String> mCountryNameCodeMap;
    private CountrySelectionItemClickListener mCountrySelectionItemClickListener;
    private String mSearchCountryNameQuery;
    private String mSelectedCountryName;
    private RecyclerView mRecyclerView;

    public static CountrySelectionDialogFragment newInstance(@NonNull final TreeMap<String, String> countryNameCodeMap,
            @NonNull final String selectedCountryName) {
        CountrySelectionDialogFragment countrySelectionDialogFragment = new CountrySelectionDialogFragment();
        countrySelectionDialogFragment.mCountryNameCodeMap = countryNameCodeMap;
        countrySelectionDialogFragment.mSelectedCountryName = selectedCountryName;
        countrySelectionDialogFragment.mSearchCountryNameQuery = "";

        Bundle bundle = new Bundle();
        bundle.putSerializable(ARGUMENT_COUNTRY_NAME_CODE_MAP, countrySelectionDialogFragment.mCountryNameCodeMap);
        bundle.putString(ARGUMENT_SELECTED_COUNTRY_NAME, countrySelectionDialogFragment.mSelectedCountryName);
        bundle.putString(ARGUMENT_SEARCH_COUNTRY_NAME_QUERY, countrySelectionDialogFragment.mSearchCountryNameQuery);
        countrySelectionDialogFragment.setArguments(bundle);

        return countrySelectionDialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCountrySelectionItemClickListener = (CountrySelectionItemClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + CountrySelectionItemClickListener.class.getCanonicalName());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_country_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.country_selection_toolbar);
        toolbar.setTitle(R.string.mobileCountryRegion);
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

        mRecyclerView = view.findViewById(R.id.country_selection_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mCountryNameCodeMap = new TreeMap<>(
                    (Map<String, String>) savedInstanceState.getSerializable(ARGUMENT_COUNTRY_NAME_CODE_MAP));
            mSelectedCountryName = savedInstanceState.getString(ARGUMENT_SELECTED_COUNTRY_NAME);
            mSearchCountryNameQuery = savedInstanceState.getString(ARGUMENT_SEARCH_COUNTRY_NAME_QUERY);
        } else {
            mCountryNameCodeMap = (TreeMap) getArguments().getSerializable(ARGUMENT_COUNTRY_NAME_CODE_MAP);
            mSelectedCountryName = getArguments().getString(ARGUMENT_SELECTED_COUNTRY_NAME);
            mSearchCountryNameQuery = getArguments().getString(ARGUMENT_SEARCH_COUNTRY_NAME_QUERY);
        }
        setHasOptionsMenu(mCountryNameCodeMap.size() > MAX_NO_SEARCH_COUNT);
        mAdapter = new Adapter(mCountryNameCodeMap, mSelectedCountryName, mCountrySelectionItemClickListener, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.scrollToPosition(mAdapter.getPositionFromCountryName(mSelectedCountryName));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(ARGUMENT_COUNTRY_NAME_CODE_MAP, mCountryNameCodeMap);
        outState.putString(ARGUMENT_SELECTED_COUNTRY_NAME, mSelectedCountryName);
        outState.putString(ARGUMENT_SEARCH_COUNTRY_NAME_QUERY, mSearchCountryNameQuery);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_country_selection, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.country_selection_search_item);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchCountryNameQuery = query;
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSearchCountryNameQuery = newText;
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });

        if (!mSearchCountryNameQuery.isEmpty()) {
            searchView.clearFocus();
            searchItem.expandActionView();
            searchView.setQuery(mSearchCountryNameQuery, true);
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
    public void hideSoftKey(@NonNull View focusedView) {
        InputMethodManager inputMethodManager = (InputMethodManager) focusedView.getContext().getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
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

    public interface CountrySelectionItemClickListener {
        void onCountryItemClicked(String countryCode);
    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> implements Filterable {

        private TreeMap<String, String> mCountryNameCodeFilteredMap;
        private TreeMap<String, String> mCountryNameCodeMap;
        private List<String> mCountryNames;
        private CountrySelectionItemClickListener mCountrySelectionItemClickListener;
        private String mSelectedCountryName;
        private ToolbarEventListener mToolbarEventListener;

        Adapter(final TreeMap<String, String> countryNameCodeMap, final String selectedCountryName,
                final CountrySelectionItemClickListener countrySelectionItemClickListener,
                final ToolbarEventListener toolbarEventListener) {
            mCountryNames = new ArrayList<>(countryNameCodeMap.keySet());
            mCountryNameCodeMap = countryNameCodeMap;
            mCountryNameCodeFilteredMap = countryNameCodeMap;
            mSelectedCountryName = selectedCountryName;
            mCountrySelectionItemClickListener = countrySelectionItemClickListener;
            mToolbarEventListener = toolbarEventListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View itemCountryView = layoutInflater.inflate(R.layout.item_country, parent, false);

            return new ViewHolder(itemCountryView, mCountrySelectionItemClickListener);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String countryName = mCountryNames.get(position);
            holder.bind(countryName);
        }

        @Override
        public int getItemCount() {
            return mCountryNames.size();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                private TreeMap<String, String> countryNameCodeFiltered;

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    if (constraint.length() == 0) {
                        countryNameCodeFiltered = mCountryNameCodeMap;
                    } else {
                        countryNameCodeFiltered = new TreeMap<>();
                        for (String countryName : mCountryNameCodeMap.keySet()) {
                            if (countryName.toLowerCase(Locale.ROOT).contains(
                                    constraint.toString().toLowerCase(Locale.ROOT))) {
                                countryNameCodeFiltered.put(countryName, mCountryNameCodeMap.get(countryName));
                            }
                        }
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = countryNameCodeFiltered;
                    filterResults.count = countryNameCodeFiltered.size();
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    mCountryNameCodeFilteredMap = (TreeMap<String, String>) results.values;
                    mCountryNames = new ArrayList<>(mCountryNameCodeFilteredMap.keySet());
                    notifyDataSetChanged();
                }
            };
        }

        @Override
        public void onViewRecycled(@NonNull ViewHolder holder) {
            holder.recycle();
        }

        int getPositionFromCountryName(@NonNull String countryName) {
            for (int i = 0; i < mCountryNames.size(); i++) {
                if (countryName.equals(mCountryNames.get(i))) {
                    return i;
                }
            }
            return 0;
        }

        String getItemCountryCode(int position) {
            return mCountryNameCodeFilteredMap.get(mCountryNames.get(position));
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final CountrySelectionItemClickListener mCountrySelectionItemClickListener;
            private final ImageView mCountryItemSelectedImage;
            private final TextView mCountryName;

            ViewHolder(@NonNull final View itemView,
                    @NonNull final CountrySelectionItemClickListener countrySelectionItemClickListener) {
                super(itemView);
                mCountryName = itemView.findViewById(R.id.country_name);
                mCountryItemSelectedImage = itemView.findViewById(R.id.country_item_selected_image);
                mCountrySelectionItemClickListener = countrySelectionItemClickListener;
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                String countryCode = getItemCountryCode(position);
                mToolbarEventListener.hideSoftKey(v);
                mToolbarEventListener.onClose();
                mCountrySelectionItemClickListener.onCountryItemClicked(countryCode);
            }

            void bind(String countryName) {
                itemView.setOnClickListener(this);
                mCountryName.setText(countryName);
                if (countryName.equals(mSelectedCountryName)) {
                    mCountryItemSelectedImage.setVisibility(View.VISIBLE);
                } else {
                    mCountryItemSelectedImage.setVisibility(View.GONE);
                }
            }

            void recycle() {
                itemView.setOnClickListener(null);
            }
        }
    }
}
