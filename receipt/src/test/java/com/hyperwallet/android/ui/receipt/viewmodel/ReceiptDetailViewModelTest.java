package com.hyperwallet.android.ui.receipt.viewmodel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import static com.hyperwallet.android.ui.receipt.view.ReceiptDetailActivity.EXTRA_RECEIPT;

import android.content.Intent;

import androidx.lifecycle.ViewModelProviders;

import com.hyperwallet.android.exception.HyperwalletException;
import com.hyperwallet.android.model.paging.HyperwalletPageList;
import com.hyperwallet.android.model.receipt.Receipt;
import com.hyperwallet.android.rule.HyperwalletExternalResourceManager;
import com.hyperwallet.android.ui.receipt.view.ReceiptDetailActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ReceiptDetailViewModelTest {

    @Rule
    public HyperwalletExternalResourceManager mExternalResourceManager = new HyperwalletExternalResourceManager();

    @Test
    public void testReceiptDetailViewModelInstance_isInitializedProperly() throws JSONException, HyperwalletException {

        String json = mExternalResourceManager.getResourceContent("prepaid_card_receipt_list_response.json");
        JSONObject jsonObject = new JSONObject(json);
        final HyperwalletPageList<Receipt> response = new HyperwalletPageList<>(jsonObject, Receipt.class);

        Intent intent = new Intent();
        intent.putExtra(EXTRA_RECEIPT, response.getDataList().get(0));

        ReceiptDetailActivity activity = Robolectric.buildActivity(ReceiptDetailActivity.class, intent).setup().get();

        ReceiptDetailViewModel model = ViewModelProviders.of(activity).get(ReceiptDetailViewModel.class);

        assertThat(model, is(notNullValue()));
    }
}
