package com.hyperwallet.android.ui.receipt.util;

import com.hyperwallet.android.HyperwalletAuthenticationTokenListener;
import com.hyperwallet.android.HyperwalletAuthenticationTokenProvider;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TestAuthenticationProvider implements HyperwalletAuthenticationTokenProvider {

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    private static final String mBaseUrl = "http://localhost:8080/rest/v3/users/{0}/authentication-token";
    private static final String mUserToken = "user_token";

    @Override
    public void retrieveAuthenticationToken(final HyperwalletAuthenticationTokenListener authenticationTokenListener) {

        OkHttpClient client = new OkHttpClient();

        String payload = "{}";
        String baseUrl = MessageFormat.format(mBaseUrl, mUserToken);

        RequestBody body = RequestBody.create(JSON, payload);
        Request request = new Request.Builder()
                .url(baseUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                authenticationTokenListener.onFailure(UUID.randomUUID(), e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                authenticationTokenListener.onSuccess(response.body().string());
            }
        });
    }
}
