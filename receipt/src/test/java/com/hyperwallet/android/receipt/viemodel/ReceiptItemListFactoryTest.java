package com.hyperwallet.android.receipt.viemodel;

import com.hyperwallet.android.model.transfermethod.HyperwalletTransferMethod;
import com.hyperwallet.android.receipt.rule.HyperwalletExternalResourceManager;

import org.json.JSONException;
import org.json.JSONObject;

//@RunWith(RobolectricTestRunner.class)
public class ReceiptItemListFactoryTest {

    //@Rule
    private HyperwalletExternalResourceManager mExternalResourceManager = new HyperwalletExternalResourceManager();

    //@Test
    public void testConsolidateList_WithResult() throws JSONException {
        String json = mExternalResourceManager.getResourceContent("receipt_list_date_grouping_response.json");
        JSONObject object = new JSONObject(json);
        HyperwalletTransferMethod transferMethod = new HyperwalletTransferMethod(object);
    }
}
