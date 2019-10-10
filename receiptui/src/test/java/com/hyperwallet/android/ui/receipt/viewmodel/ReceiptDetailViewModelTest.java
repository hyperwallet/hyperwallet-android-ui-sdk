package com.hyperwallet.android.ui.receipt.viewmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import static com.hyperwallet.android.ui.receipt.view.ReceiptDetailActivity.EXTRA_RECEIPT;

import android.content.Intent;

import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.model.paging.HyperwalletPageList;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.ui.receipt.view.ReceiptDetailActivity;
import com.hyperwallet.android.ui.testutils.rule.HyperwalletExternalResourceManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ReceiptDetailViewModelTest {

    @Rule
    public HyperwalletExternalResourceManager mExternalResourceManager = new HyperwalletExternalResourceManager();

    private Intent mIntent;
    private Receipt mReceipt;
    private HyperwalletPageList<Receipt> mReceiptList;

    @Before
    public void initialize() throws JSONException, HyperwalletException {
        String json = mExternalResourceManager.getResourceContent("prepaid_card_receipt_list_response.json");
        JSONObject jsonObject = new JSONObject(json);
        mReceiptList = new HyperwalletPageList<>(jsonObject, Receipt.class);

        mIntent = new Intent();
        mReceipt = mReceiptList.getDataList().get(0);
        mIntent.putExtra(EXTRA_RECEIPT, mReceipt);
    }

    @Test
    public void testReceiptDetailViewModel_isInitialized() {

        ReceiptDetailActivity activity = Robolectric.buildActivity(ReceiptDetailActivity.class, mIntent).setup().get();

        ReceiptDetailViewModel model = ViewModelProviders.of(activity).get(ReceiptDetailViewModel.class);

        assertThat(model, is(notNullValue()));
    }

    @Test
    public void testReceiptDetailViewModel_verifyDefaultValues() {
        ReceiptDetailActivity activity = Robolectric.buildActivity(ReceiptDetailActivity.class, mIntent).setup().get();

        ReceiptDetailViewModel model = ViewModelProviders.of(activity).get(ReceiptDetailViewModel.class);

        assertThat(model, is(notNullValue()));
        assertThat(model.getReceipt(), is(notNullValue()));
        assertThat(model.getReceipt(), is(mReceipt));
        assertThat(model.getReceipt().getDetails(), is(mReceipt.getDetails()));
    }

    @Test
    public void testReceiptDetailViewModel_verifyReceiptSet() {
        ReceiptDetailActivity activity = Robolectric.buildActivity(ReceiptDetailActivity.class, mIntent).setup().get();

        ReceiptDetailViewModel model = ViewModelProviders.of(activity).get(ReceiptDetailViewModel.class);

        // default receipt
        assertThat(model, is(notNullValue()));
        assertThat(model.getReceipt(), is(mReceipt));
        assertThat(model.getReceipt().getDetails(), is(mReceipt.getDetails()));

        // prepare and set new receipt
        Receipt newReceipt = mReceiptList.getDataList().get(1);
        model.setReceipt(newReceipt);

        // new receipt is now latest value of model.getReceipt
        assertThat(model.getReceipt(), is(not(mReceipt)));
        assertThat(model.getReceipt(), is(newReceipt));
        assertThat(model.getReceipt().getDetails(), is(newReceipt.getDetails()));
    }
}