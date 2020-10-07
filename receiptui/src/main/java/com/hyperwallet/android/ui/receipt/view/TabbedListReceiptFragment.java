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
import com.hyperwallet.android.ui.receipt.R;
import com.hyperwallet.android.ui.receipt.viewmodel.TabbedListReceiptViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TabbedListReceiptFragment extends Fragment {

    private ListReceiptsViewPagerAdapter listReceiptsViewPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private LinearLayout header;

    private TabbedListReceiptViewModel receiptViewModel;

    public TabbedListReceiptFragment() {
    }

    public static TabbedListReceiptFragment newInstance() {
        TabbedListReceiptFragment fragment = new TabbedListReceiptFragment();
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
        viewPager = (ViewPager) view.findViewById(R.id.receipts_pager);
        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        header = (LinearLayout) view.findViewById(R.id.transactions_header);
    }

    void retry() {
        // mReceiptViewModel.retry();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof TabbedListReceiptActivity) {
            header.setVisibility(View.GONE);
        }
        receiptViewModel = ViewModelProviders.of(requireActivity()).get(TabbedListReceiptViewModel.class);
        listReceiptsViewPagerAdapter = new ListReceiptsViewPagerAdapter(getFragmentManager(),
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, new ArrayList<Parcelable>(),
                getResources());
        listReceiptsViewPagerAdapter.addInitialItem(null);
        viewPager.setAdapter(listReceiptsViewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        receiptViewModel.initialize();
        registerObservers();
    }

    private void registerObservers() {
        receiptViewModel.prepaidCards.observe(this, new Observer<List<PrepaidCard>>() {
            @Override
            public void onChanged(List<PrepaidCard> prepaidCards) {
                if (!prepaidCards.isEmpty()) {
                    boolean isWalletModel = true;
                    boolean isCardModel = false;

                    if (isWalletModel) {
                        tabLayout.setVisibility(View.VISIBLE);
                        sortToken(prepaidCards);
                        for (PrepaidCard prepaidCard : prepaidCards) {
                            listReceiptsViewPagerAdapter.addItem(prepaidCard);
                        }
                    } else if (isCardModel) {
                        if (prepaidCards.size() > 1) {
                            tabLayout.setVisibility(View.VISIBLE);
                            sortToken(prepaidCards);
                            for (PrepaidCard prepaidCard : prepaidCards) {
                                listReceiptsViewPagerAdapter.addItem(prepaidCard);
                            }
                        } else {
                            tabLayout.setVisibility(View.GONE);
                            listReceiptsViewPagerAdapter.addItem(prepaidCards.get(0));
                        }
                    }
                    listReceiptsViewPagerAdapter.notifyDataSetChanged();
                } else {
                    tabLayout.setVisibility(View.GONE);
                }
            }
        });

    }

    private void sortToken(List<PrepaidCard> prepaidCards) {
        Collections.sort(prepaidCards, new Comparator<PrepaidCard>() {
            @Override
            public int compare(PrepaidCard firstPrepaid, PrepaidCard secondPrepaid) {
                if (firstPrepaid == null || secondPrepaid == null
                        || firstPrepaid.getPrimaryCardToken() == null || secondPrepaid.getPrimaryCardToken() == null) {
                    return 0;
                }
                return firstPrepaid.getPrimaryCardToken().compareTo(secondPrepaid.getPrimaryCardToken());
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
                return ListReceiptFragment.newInstance(token);
            }
            return ListReceiptFragment.newInstance();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            if (sources.get(position) instanceof PrepaidCard) {
                PrepaidCard prepaidCard = (PrepaidCard) sources.get(position);
                String cardNumber = prepaidCard.getCardNumber();
                return resources.getString(R.string.prepaidCardTabLabel, prepaidCard.getCardBrand(),
                        "\u0020\u2022\u2022\u2022\u2022\u0020").concat(
                        cardNumber.substring(cardNumber.length() - 4, cardNumber.length()));
            }

            return resources.getString(R.string.mobileAvailableBalances);
        }
    }
}
