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
package com.hyperwallet.android.ui.receipt.view;

import static com.hyperwallet.android.model.transfermethod.TransferMethod.TransferMethodFields.TOKEN;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.hyperwallet.android.model.transfermethod.PrepaidCard;
import com.hyperwallet.android.ui.common.view.ProgramModel;
import com.hyperwallet.android.ui.receipt.R;
import com.hyperwallet.android.ui.receipt.viewmodel.TabbedListReceiptsViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TabbedListReceiptsFragment extends Fragment {

    private ListReceiptsViewPagerAdapter mListReceiptsViewPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private LinearLayout mHeader;

    private TabbedListReceiptsViewModel mTabbedListReceiptsViewModel;

    public TabbedListReceiptsFragment() {
    }

    public static TabbedListReceiptsFragment newInstance() {
        TabbedListReceiptsFragment fragment = new TabbedListReceiptsFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tabbed_list_receipt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = view.findViewById(R.id.receipts_pager);
        mTabLayout = view.findViewById(R.id.tab_layout);
        mHeader = view.findViewById(R.id.transactions_header);
    }

    void retry() {
        mTabbedListReceiptsViewModel.retry();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof TabbedListReceiptsActivity) {
            mHeader.setVisibility(View.GONE);
        }
        mTabbedListReceiptsViewModel = ViewModelProviders.of(requireActivity()).get(TabbedListReceiptsViewModel.class);
        mListReceiptsViewPagerAdapter = new ListReceiptsViewPagerAdapter(getFragmentManager(),
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, new ArrayList<Parcelable>(),
                getResources());
        mTabbedListReceiptsViewModel.initProgramModel();
        if (ProgramModel.isWalletModel(mTabbedListReceiptsViewModel.mProgramModel)) {
            mListReceiptsViewPagerAdapter.addInitialItem(null);
        }
        mViewPager.setAdapter(mListReceiptsViewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabbedListReceiptsViewModel.initialize();
        registerObservers();
    }

    private void retryCurrentListFragment() {
        int position = mViewPager.getCurrentItem();
        ((ListReceiptsFragment) mViewPager.getAdapter().instantiateItem(mViewPager, position)).retry();
    }

    private void registerObservers() {
        mTabbedListReceiptsViewModel.getPrepaidCards().observe(this, new Observer<List<PrepaidCard>>() {
            @Override
            public void onChanged(List<PrepaidCard> prepaidCards) {
                if (!prepaidCards.isEmpty()) {
                    if (ProgramModel.isWalletModel(mTabbedListReceiptsViewModel.mProgramModel)) {
                        mTabLayout.setVisibility(View.VISIBLE);
                        mListReceiptsViewPagerAdapter.sortPrepaidCards(prepaidCards);
                        for (PrepaidCard prepaidCard : prepaidCards) {
                            mListReceiptsViewPagerAdapter.addItem(prepaidCard);
                        }
                    } else if (ProgramModel.isCardModel(mTabbedListReceiptsViewModel.mProgramModel)) {
                        if (prepaidCards.size() > 1) {
                            mTabLayout.setVisibility(View.VISIBLE);
                            mListReceiptsViewPagerAdapter.sortPrepaidCards(prepaidCards);
                            for (PrepaidCard prepaidCard : prepaidCards) {
                                mListReceiptsViewPagerAdapter.addItem(prepaidCard);
                            }
                        } else {
                            mTabLayout.setVisibility(View.GONE);
                            mListReceiptsViewPagerAdapter.addItem(prepaidCards.get(0));
                        }
                    }
                    mListReceiptsViewPagerAdapter.notifyDataSetChanged();
                } else {
                    mTabLayout.setVisibility(View.GONE);
                }
            }
        });

        mTabbedListReceiptsViewModel.getRetryListReceipts().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                retryCurrentListFragment();
            }
        });
    }

    class ListReceiptsViewPagerAdapter extends FragmentStatePagerAdapter {

        private List<Parcelable> sources;
        private Resources resources;

        public ListReceiptsViewPagerAdapter(@NonNull FragmentManager fm, int behavior, List<Parcelable> mSources,
                Resources mResources) {
            super(fm, behavior);
            this.resources = mResources;
            this.sources = mSources;
        }


        public void addInitialItem(Parcelable data) {
            sources.add(data);
        }

        public void addItem(Parcelable data) {
            addUniqueItem(data);
        }

        /**
         * Implementation for adding unique @{@link PrepaidCard} cards only in the ArrayList.
         */
        private void addUniqueItem(Parcelable card) {
            boolean found = false;
            for (Parcelable item : sources) {
                if (item instanceof PrepaidCard && card instanceof PrepaidCard && ((PrepaidCard) item).getField(
                        TOKEN).equals(((PrepaidCard) card).getField(TOKEN))) {
                    found = true;
                }

            }
            if (!found) {
                sources.add(card);
            }
        }

        @Override
        public int getCount() {
            return sources.size();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (sources.get(position) instanceof PrepaidCard) {
                PrepaidCard prepaidCard = (PrepaidCard) sources.get(position);
                String token = "";
                if (prepaidCard != null && prepaidCard.getField(TOKEN) != null) {
                    token = prepaidCard.getField(TOKEN);
                }
                return ListReceiptsFragment.newInstance(token);
            }
            return ListReceiptsFragment.newInstance();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (sources.get(position) instanceof PrepaidCard) {
                PrepaidCard prepaidCard = (PrepaidCard) sources.get(position);
                String cardNumber = prepaidCard.getCardNumber();
                String prepaidCardBrand = resources.getString(R.string.visa);
                if (prepaidCard.getCardBrand().equalsIgnoreCase(resources.getString(R.string.mastercard))) {
                    prepaidCardBrand = resources.getString(R.string.mastercard);
                }
                return prepaidCardBrand.concat("\u0020\u2022\u2022\u2022\u2022\u0020").concat(
                        cardNumber.substring(cardNumber.length() - 4, cardNumber.length()));
            }

            return resources.getString(R.string.mobileAvailableBalances);
        }

        private void sortPrepaidCards(List<PrepaidCard> prepaidCards) {
            Collections.sort(prepaidCards, new Comparator<PrepaidCard>() {
                @Override
                public int compare(PrepaidCard firstPrepaid, PrepaidCard secondPrepaid) {
                    if (firstPrepaid == null || secondPrepaid == null
                            || firstPrepaid.getPrimaryCardToken() == null
                            || secondPrepaid.getPrimaryCardToken() == null) {
                        return 0;
                    }
                    return firstPrepaid.getPrimaryCardToken().compareTo(secondPrepaid.getPrimaryCardToken());
                }
            });
        }
    }
}
