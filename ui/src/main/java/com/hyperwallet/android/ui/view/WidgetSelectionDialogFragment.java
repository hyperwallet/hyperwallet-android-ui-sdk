/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Hyperwallet Systems Inc.
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
package com.hyperwallet.android.ui.view;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.hyperwallet_ui.R;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class WidgetSelectionDialogFragment extends DialogFragment implements ToolbarEventListener {

    public static final String TAG = WidgetSelectionDialogFragment.class.getName();
    private static final String ARGUMENT_NAME_VALUE_MAP = "ARGUMENT_NAME_VALUE_MAP";
    private static final String ARGUMENT_SELECTED_NAME = "ARGUMENT_SELECTED_NAME";
    private static final String ARGUMENT_SELECTION_LABEL = "ARGUMENT_SELECTION_LABEL";
    private static final String ARGUMENT_SELECTION_FIELD_NAME = "ARGUMENT_SELECTION_FIELD_NAME";

    private Adapter mAdapter;
    private String mFieldName;
    private TreeMap<String, String> mNameValueMap;
    private String mSelectedName;
    private String mSelectionLabel;
    private WidgetSelectionItemListener mWidgetSelectionItemListener;

    public static WidgetSelectionDialogFragment newInstance(@NonNull final TreeMap<String, String> nameValueMap,
            @NonNull final String selectedName, @NonNull final String selectionLabel, @NonNull final String fieldName) {

        WidgetSelectionDialogFragment widgetSelectionDialogFragment = new WidgetSelectionDialogFragment();
        widgetSelectionDialogFragment.mNameValueMap = nameValueMap;
        widgetSelectionDialogFragment.mSelectedName = selectedName;
        widgetSelectionDialogFragment.mSelectionLabel = selectionLabel;
        widgetSelectionDialogFragment.mFieldName = fieldName;

        Bundle bundle = new Bundle();
        bundle.putSerializable(ARGUMENT_NAME_VALUE_MAP, widgetSelectionDialogFragment.mNameValueMap);
        bundle.putString(ARGUMENT_SELECTED_NAME, widgetSelectionDialogFragment.mSelectedName);
        bundle.putString(ARGUMENT_SELECTION_LABEL, widgetSelectionDialogFragment.mSelectionLabel);
        bundle.putString(ARGUMENT_SELECTION_FIELD_NAME, widgetSelectionDialogFragment.mFieldName);
        widgetSelectionDialogFragment.setArguments(bundle);

        return widgetSelectionDialogFragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(ARGUMENT_NAME_VALUE_MAP, mNameValueMap);
        outState.putString(ARGUMENT_SELECTED_NAME, mSelectedName);
        outState.putString(ARGUMENT_SELECTION_LABEL, mSelectionLabel);
        outState.putString(ARGUMENT_SELECTION_FIELD_NAME, mFieldName);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mNameValueMap = (TreeMap) savedInstanceState.getSerializable(ARGUMENT_NAME_VALUE_MAP);
            mSelectedName = savedInstanceState.getString(ARGUMENT_SELECTED_NAME);
            mSelectionLabel = savedInstanceState.getString(ARGUMENT_SELECTION_LABEL);
            mFieldName = savedInstanceState.getString(ARGUMENT_SELECTION_FIELD_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null) { // framework recreating previously destroyed instance
            mNameValueMap = (TreeMap) savedInstanceState.getSerializable(ARGUMENT_NAME_VALUE_MAP);
            mSelectedName = savedInstanceState.getString(ARGUMENT_SELECTED_NAME);
            mSelectionLabel = savedInstanceState.getString(ARGUMENT_SELECTION_LABEL);
            mFieldName = savedInstanceState.getString(ARGUMENT_SELECTION_FIELD_NAME);
        } else { // equivalent to WidgetSelectionDialogFragment#newInstance
            mNameValueMap = (TreeMap) getArguments().getSerializable(ARGUMENT_NAME_VALUE_MAP);
            mSelectedName = getArguments().getString(ARGUMENT_SELECTED_NAME);
            mSelectionLabel = getArguments().getString(ARGUMENT_SELECTION_LABEL);
            mFieldName = getArguments().getString(ARGUMENT_SELECTION_FIELD_NAME);
        }

        View rootView = inflater.inflate(R.layout.dialog_fragment_widget_selection, container, false);

        Toolbar toolbar = rootView.findViewById(R.id.input_selection_toolbar);
        toolbar.setTitle(mSelectionLabel);
        toolbar.setNavigationIcon(R.drawable.ic_close_14dp);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClose();
                getFragmentManager().popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                dismiss();
            }
        });
        onView();

        mAdapter = new Adapter(mNameValueMap, mSelectedName, this, this, mFieldName, mWidgetSelectionItemListener);
        RecyclerView recyclerView = rootView.findViewById(R.id.input_selection_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        recyclerView.scrollToPosition(mAdapter.getItemPosition(mSelectedName));
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mWidgetSelectionItemListener = (WidgetSelectionItemListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + WidgetSelectionItemListener.class.getCanonicalName());
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
        //not supported for now, since there's no search key, will be enabled when profile is supported
    }

    public interface WidgetSelectionItemListener {
        void onWidgetSelectionItemClicked(@NonNull final String selectedValue, @NonNull final String fieldName);
    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private final TreeMap<String, String> mNameValueMap;
        private final String mSelectedName;
        private final List<String> mSelectionList;
        private final ToolbarEventListener mToolbarEventListener;
        private final Fragment mFragment;
        private final String mFieldName;
        private final WidgetSelectionItemListener mWidgetSelectionItemListener;

        Adapter(@NonNull final TreeMap<String, String> nameValueMap, @NonNull final String selectedName,
                @NonNull final Fragment fragment, @NonNull final ToolbarEventListener toolbarEventListener,
                @NonNull final String fieldName,
                @NonNull final WidgetSelectionItemListener widgetSelectionItemListener) {
            mSelectionList = new ArrayList<>(nameValueMap.keySet());
            mSelectedName = selectedName;
            mNameValueMap = nameValueMap;
            mToolbarEventListener = toolbarEventListener;
            mFragment = fragment;
            mFieldName = fieldName;
            mWidgetSelectionItemListener = widgetSelectionItemListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View itemGenericSelection = layoutInflater.inflate(R.layout.item_widget_selection, parent, false);

            return new ViewHolder(itemGenericSelection, mFragment, mToolbarEventListener,
                    mFieldName, mWidgetSelectionItemListener);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String name = mSelectionList.get(position);
            holder.bind(name);
        }

        @Override
        public int getItemCount() {
            return mSelectionList.size();
        }

        @Override
        public void onViewRecycled(@NonNull ViewHolder holder) {
            holder.recycle();
        }

        int getItemPosition(@NonNull String selectedName) {
            for (int i = 0; i < mSelectionList.size(); i++) {
                if (selectedName.equals(mSelectionList.get(i))) {
                    return i;
                }
            }
            return 0;
        }

        String getItemValue(int position) {
            return mNameValueMap.get(mSelectionList.get(position));
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView mSelectName;
            final ImageView mSelectItemImage;
            final ToolbarEventListener mToolbarEventListener;
            final Fragment mFragment;
            final String mFieldName;
            final WidgetSelectionItemListener mWidgetSelectionItemListener;

            ViewHolder(@NonNull final View itemView, @NonNull final Fragment fragment,
                    @NonNull final ToolbarEventListener toolbarEventListener, @NonNull final String fieldName,
                    @NonNull WidgetSelectionItemListener widgetSelectionItemListener) {
                super(itemView);

                mSelectName = itemView.findViewById(R.id.select_name);
                mSelectItemImage = itemView.findViewById(R.id.item_select_image);
                mToolbarEventListener = toolbarEventListener;
                mFragment = fragment;
                mWidgetSelectionItemListener = widgetSelectionItemListener;
                mFieldName = fieldName;
            }

            @Override
            public void onClick(View v) {
                mToolbarEventListener.onClose();
                mWidgetSelectionItemListener.onWidgetSelectionItemClicked(getItemValue(getAdapterPosition()),
                        mFieldName);
            }

            void bind(String name) {
                itemView.setOnClickListener(this);
                mSelectName.setText(name);
                if (name.equals(mSelectedName)) {
                    mSelectItemImage.setVisibility(View.VISIBLE);
                } else {
                    mSelectItemImage.setVisibility(View.GONE);
                }
            }

            void recycle() {
                itemView.setOnClickListener(null);
            }
        }
    }

    public interface WidgetSelectionItemType {
        void onWidgetSelectionItemClicked(@NonNull String selectedValue);
    }
}
