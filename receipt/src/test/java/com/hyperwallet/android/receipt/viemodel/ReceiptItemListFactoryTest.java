package com.hyperwallet.android.receipt.viemodel;

import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.receipt.rule.HyperwalletExternalResourceManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Locale;

@RunWith(RobolectricTestRunner.class)
public class ReceiptItemListFactoryTest {

    @Rule
    public HyperwalletExternalResourceManager mExternalResourceManager = new HyperwalletExternalResourceManager();

    @Test
    public void testConsolidateList_WithResult() throws JSONException {
        String json = mExternalResourceManager.getResourceContent("receipt_list_date_grouping_response.json");
        JSONObject object = new JSONObject(json);
        HyperwalletTransferMethod transferMethod = new HyperwalletTransferMethod(object);

        Locale locale = Locale.getDefault();
        locale.getDisplayName();
    }
}
