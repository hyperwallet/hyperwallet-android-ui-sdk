package com.hyperwallet.android.rule;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public final class HyperwalletMockWebServer extends TestWatcher {

    private MockWebServer mServer;
    private int port;

    @Override
    protected void starting(Description description) {
        super.starting(description);
        mServer = new MockWebServer();
        try {
            mServer.start(port);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start mock server", e);
        }
    }

    @Override
    protected void finished(Description description) {
        super.finished(description);
        try {
            mServer.shutdown();
            mServer.close();
        } catch (IOException e) {
            throw new IllegalStateException("Un error occurred when shutting down mock server", e);
        }
    }

    public HyperwalletMockWebServer(int port) {
        this.port = port;
    }

    public HyperwalletMockResponse mockResponse() {
        return new Builder(mServer).build();
    }

    public MockWebServer getServer() {
        return mServer;
    }

    public static class HyperwalletMockResponse {

        private String path;
        private String body;
        private int httpResponseCode;
        private Builder builder;

        HyperwalletMockResponse(Builder builder) {
            this.path = builder.path;
            this.httpResponseCode = builder.responseCode;
            this.body = builder.body;
            this.builder = builder;
        }

        public HyperwalletMockResponse withHttpResponseCode(final int code) {
            builder.responseCode(code);
            return builder.build();
        }

        public HyperwalletMockResponse withBody(final String body) {
            builder.body(body);
            return builder.build();
        }

        public void mock() {
            mockRequest();
        }

        private String mockRequest() {
            builder.server.enqueue(new MockResponse().setResponseCode(httpResponseCode).setBody(body));
            return builder.server.url(path).toString();
        }

    }

    private static class Builder {

        private String path;
        private String body;
        private int responseCode;
        private MockWebServer server;


        Builder(final MockWebServer server) {
            this.path = "";
            this.responseCode = HttpURLConnection.HTTP_OK;
            this.body = "";
            this.server = server;
        }

        Builder responseCode(final int code) {
            this.responseCode = code;
            return this;
        }

        Builder body(final String body) {
            this.body = body;
            return this;
        }

        HyperwalletMockResponse build() {
            return new HyperwalletMockResponse(this);
        }
    }
}
