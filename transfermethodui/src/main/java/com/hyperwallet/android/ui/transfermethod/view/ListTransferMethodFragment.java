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
package com.hyperwallet.android.ui.transfermethod.view;

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TRANSFER_METHOD_COUNTRY;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TYPE;
import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodTypes.PREPAID_CARD;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getStringFontIcon;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getStringResourceByName;
import static com.hyperwallet.android.ui.common.view.TransferMethodUtils.getTransferMethodDetail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyperwallet.android.model.Error;
import com.hyperwallet.android.model.StatusTransition;
import com.hyperwallet.android.model.transfermethod.TransferMethod;
import com.hyperwallet.android.ui.R;
import com.hyperwallet.android.ui.common.view.OneClickListener;
import com.hyperwallet.android.ui.transfermethod.TransferMethodLocalBroadcast;
import com.hyperwallet.android.ui.transfermethod.repository.TransferMethodRepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListTransferMethodFragment extends Fragment implements ListTransferMethodContract.View {

    static final String ARGUMENT_IS_TRANSFER_METHODS_RELOAD_NEEDED = "ARGUMENT_IS_TRANSFER_METHODS_RELOAD_NEEDED";


    private static final String ARGUMENT_TRANSFER_METHOD_LIST = "ARGUMENT_TRANSFER_METHOD_LIST";

    private View mEmptyListView;
    private ListTransferMethodAdapter mListTransferMethodAdapter;
    private ListTransferMethodContract.Presenter mPresenter;
    private View mProgressBar;
    private ArrayList<TransferMethod> mTransferMethodList;
    private OnAddNewTransferMethodSelected mOnAddNewTransferMethodSelected;
    private OnTransferMethodContextMenuItemSelected mOnTransferMethodContextMenuItemSelected;
    private OnDeactivateTransferMethodNetworkErrorCallback mOnDeactivateTransferMethodNetworkErrorCallback;
    private OnLoadTransferMethodNetworkErrorCallback mOnLoadTransferMethodNetworkErrorCallback;
    private boolean mIsTransferMethodsReloadNeeded;
    private RecyclerView recyclerView;

    /**
     * Please don't use this constructor this is reserved for Android Core Framework
     * use
     *
     * @see {@link ListTransferMethodFragment#newInstance()} instead.
     */
    public ListTransferMethodFragment() {
    }

    public static ListTransferMethodFragment newInstance() {
        ListTransferMethodFragment fragment = new ListTransferMethodFragment();
        fragment.mTransferMethodList = new ArrayList<>(1);

        Bundle internalState = new Bundle();
        internalState.putParcelableArrayList(ARGUMENT_TRANSFER_METHOD_LIST, fragment.mTransferMethodList);
        fragment.setArguments(internalState);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnAddNewTransferMethodSelected = (OnAddNewTransferMethodSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + OnAddNewTransferMethodSelected.class.getCanonicalName());
        }

        try {
            mOnTransferMethodContextMenuItemSelected = (OnTransferMethodContextMenuItemSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + OnTransferMethodContextMenuItemSelected.class.getCanonicalName());
        }

        try {
            mOnDeactivateTransferMethodNetworkErrorCallback = (OnDeactivateTransferMethodNetworkErrorCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + OnDeactivateTransferMethodNetworkErrorCallback.class.getCanonicalName());
        }

        try {
            mOnLoadTransferMethodNetworkErrorCallback = (OnLoadTransferMethodNetworkErrorCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement "
                    + OnLoadTransferMethodNetworkErrorCallback.class.getCanonicalName());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mTransferMethodList = savedInstanceState.getParcelableArrayList(ARGUMENT_TRANSFER_METHOD_LIST);
            mIsTransferMethodsReloadNeeded = savedInstanceState.getBoolean(ARGUMENT_IS_TRANSFER_METHODS_RELOAD_NEEDED);
        } else {
            mTransferMethodList = getArguments().getParcelableArrayList(ARGUMENT_TRANSFER_METHOD_LIST);
            mIsTransferMethodsReloadNeeded = true;
        }
        mListTransferMethodAdapter = new ListTransferMethodAdapter(mTransferMethodList,
                mOnTransferMethodContextMenuItemSelected);

        recyclerView.setAdapter(mListTransferMethodAdapter);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_transfer_method, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = view.findViewById(R.id.list_transfer_method_progress_bar_layout);
        mEmptyListView = view.findViewById(R.id.empty_transfer_method_list_layout);
        Button button = view.findViewById(R.id.add_account_button);
        button.setOnClickListener(new OneClickListener() {
            @Override
            public void onOneClick(View v) {
                mOnAddNewTransferMethodSelected.showSelectTransferMethodView();
            }
        });

        recyclerView = view.findViewById(R.id.list_transfer_method_item);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TransferMethodRepositoryFactory factory = TransferMethodRepositoryFactory.getInstance();
        mPresenter = new ListTransferMethodPresenter(factory.getTransferMethodRepository(), this);
    }


    @Override
    public void onResume() {
        super.onResume();
        mIsTransferMethodsReloadNeeded = getArguments().getBoolean(ARGUMENT_IS_TRANSFER_METHODS_RELOAD_NEEDED, true);
        loadTransferMethodsList(mIsTransferMethodsReloadNeeded);
    }

    private void loadTransferMethodsList(boolean shouldReload) {
        if (shouldReload) {
            getArguments().putBoolean(ARGUMENT_IS_TRANSFER_METHODS_RELOAD_NEEDED, false);
            mPresenter.loadTransferMethods();
        } else {
            displayTransferMethods(mTransferMethodList);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(ARGUMENT_TRANSFER_METHOD_LIST, mTransferMethodList);
        outState.putBoolean(ARGUMENT_IS_TRANSFER_METHODS_RELOAD_NEEDED, mIsTransferMethodsReloadNeeded);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void confirmTransferMethodDeactivation(@NonNull final TransferMethod transferMethod) {
        mPresenter.deactivateTransferMethod(transferMethod);
    }

    @Override
    public void displayTransferMethods(@Nullable final List<TransferMethod> transferMethodList) {
        if (transferMethodList != null && !transferMethodList.isEmpty()) {
            mTransferMethodList = new ArrayList<>(transferMethodList);
            initializeNonEmptyListView();
            mIsTransferMethodsReloadNeeded = false;
        } else {
            mTransferMethodList = new ArrayList<>(1);
            mEmptyListView.setVisibility(View.VISIBLE);
            mListTransferMethodAdapter.replaceData(mTransferMethodList);
        }
    }

    @Override
    public void notifyTransferMethodDeactivated(
            @NonNull final StatusTransition statusTransition) {
        Intent intent = TransferMethodLocalBroadcast.createBroadcastIntentTransferMethodDeactivated(
                statusTransition);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        mPresenter.loadTransferMethods();
    }

    @Override
    public void showErrorListTransferMethods(@NonNull List<Error> errors) {
        mOnLoadTransferMethodNetworkErrorCallback.showErrorsLoadTransferMethods(errors);
    }

    @Override
    public void showErrorDeactivateTransferMethod(@NonNull List<Error> errors) {
        mOnDeactivateTransferMethodNetworkErrorCallback.showErrorsDeactivateTransferMethod(errors);
    }

    @Override
    public void initiateAddTransferMethodFlow() {
    }

    @Override
    public void initiateAddTransferMethodFlowResult() {
    }

    @Override
    public void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void initializeNonEmptyListView() {
        mEmptyListView.setVisibility(View.GONE);
        mListTransferMethodAdapter.replaceData(mTransferMethodList);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void loadTransferMethods() {
        mPresenter.loadTransferMethods();
    }

    interface OnTransferMethodContextMenuItemSelected {

        void showConfirmationDialog(@NonNull final TransferMethod transferMethod);

        void invokeTransferMethodEdit(@NonNull final TransferMethod transferMethod);
    }

    interface OnAddNewTransferMethodSelected {

        void showSelectTransferMethodView();
    }

    interface OnDeactivateTransferMethodNetworkErrorCallback {

        void showErrorsDeactivateTransferMethod(@NonNull final List<Error> errors);
    }

    interface OnLoadTransferMethodNetworkErrorCallback {

        void showErrorsLoadTransferMethods(@NonNull final List<Error> errors);
    }

    private static class ListTransferMethodAdapter extends RecyclerView.Adapter<ListTransferMethodAdapter.ViewHolder> {
        private List<TransferMethod> mTransferMethodList;
        private OnTransferMethodContextMenuItemSelected mOnTransferMethodContextMenuItemSelected;

        ListTransferMethodAdapter(final List<TransferMethod> transferMethodList,
                final OnTransferMethodContextMenuItemSelected onTransferMethodContextMenuSelection) {
            mTransferMethodList = transferMethodList;
            mOnTransferMethodContextMenuItemSelected = onTransferMethodContextMenuSelection;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
            LayoutInflater layout = LayoutInflater.from(viewGroup.getContext());
            View itemViewLayout = layout.inflate(R.layout.item_transfer_method_type, viewGroup, false);
            return new ViewHolder(itemViewLayout);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            final TransferMethod transferMethod = mTransferMethodList.get(position);
            viewHolder.bind(transferMethod);
        }

        @Override
        public void onViewRecycled(@NonNull ViewHolder holder) {
            holder.recycle();
        }


        @Override
        public int getItemCount() {
            return mTransferMethodList.size();
        }

        void replaceData(List<TransferMethod> transferMethodList) {
            mTransferMethodList = transferMethodList;
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView mTitle;
            private final TextView mTransferMethodCountry;
            private final TextView mTransferMethodIdentification;
            private final TextView mIcon;
            private final ImageButton mImageButton;

            ViewHolder(@NonNull final View itemView) {
                super(itemView);
                mTitle = itemView.findViewById(R.id.transfer_method_type_title);
                mIcon = itemView.findViewById(R.id.transfer_method_type_icon);
                mTransferMethodCountry = itemView.findViewById(R.id.transfer_method_type_description_1);
                mTransferMethodIdentification = itemView.findViewById(R.id.transfer_method_type_description_2);
                mImageButton = itemView.findViewById(R.id.transfer_method_context_button);
            }


            void bind(@NonNull final TransferMethod transferMethod) {
                String type = transferMethod.getField(TYPE);
                final String transferMethodIdentification = getTransferMethodDetail(
                        mTransferMethodIdentification.getContext(),
                        transferMethod,
                        type);

                mTitle.setText(
                        getStringResourceByName(mTitle.getContext(), type));

                Locale locale = new Locale.Builder().setRegion(
                        transferMethod.getField(TRANSFER_METHOD_COUNTRY)).build();
                mIcon.setText(getStringFontIcon(mIcon.getContext(), type));
                if (type.equals(PREPAID_CARD)) {
                    mImageButton.setVisibility(View.GONE);
                    mTransferMethodCountry.setText(transferMethodIdentification);
                    mTransferMethodIdentification.setText(
                            mTransferMethodIdentification.getContext().getString(R.string.prepaidCardManagementInfo));
                } else {
                    mImageButton.setVisibility(View.VISIBLE);
                    mTransferMethodCountry.setText(locale.getDisplayName());
                    mTransferMethodIdentification.setText(transferMethodIdentification);
                }

                mImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    @SuppressWarnings("RestrictedApi")
                    public void onClick(View v) {
                        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                        popupMenu.inflate(R.menu.transfer_method_context_menu);
                        popupMenu.setGravity(Gravity.END);
                        if (popupMenu.getMenu() instanceof MenuBuilder) {
                            MenuBuilder menuBuilder = (MenuBuilder) popupMenu.getMenu();
                            menuBuilder.setOptionalIconsVisible(true);
                        }
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if (item.getItemId() == R.id.remove_account_context_option) {
                                    mOnTransferMethodContextMenuItemSelected.showConfirmationDialog(transferMethod);
                                    return true;
                                } if (item.getItemId() == R.id.edit_account_context_option) {
                                    mOnTransferMethodContextMenuItemSelected.invokeTransferMethodEdit(transferMethod);
                                    return true;
                                }
                                return false;
                            }
                        });

                        popupMenu.show();
                    }
                });
            }

            void recycle() {
                mImageButton.setOnClickListener(null);
            }
        }
    }
}
