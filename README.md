# Hyperwallet Android UI SDK

Welcome to Hyperwallet's Android UI SDK. This out-of-the-box library will help you create transfer methods in your Android app, such as bank account, PayPal account, etc.

Note that this SDK is geared towards those who need both backend data and UI features. Please refer to [Hyperwallet Android Core SDK](https://github.com/hyperwallet/hyperwallet-android-sdk) if you decide to build your own UI.


## Prerequisites
* A Hyperwallet merchant account
* Set up your server to manage the user's authentication process on the Hyperwallet platform
* An Android IDE
* Android version >= 21

## Installation

To install Hyperwallet UI SDK, you just need to add the dependencies into your build.gradle file in Android Studio (or Gradle). For example:

```bash
api 'com.hyperwallet.android.ui:transfermethodui:1.0.0-beta03'
api 'com.hyperwallet.android.ui:receiptui:1.0.0-beta03'
```

## Initialization

After you're done installing the SDK, you need to initialize an instance in order to utilize UI SDK functions. Also you need to provide a  [HyperwalletAuthenticationTokenProvider](#Authentication) object to retrieve an authentication token.

```java
// initialize UI SDK
HyperwalletReceiptUi.getInstance(hyperwalletAuthenticationTokenProvider);
HyperwalletTransferMethodUi.getInstance(hyperwalletAuthenticationTokenProvider);

// use UI SDK functions
mHyperwalletTransferMethodUi.getIntentListTransferMethodActivity(MainActivity.this);
mHyperwalletReceiptUi.getIntentListUserReceiptActivity(MainActivity.this);
```

## Authentication
First of all, your server side should be able to send a POST request to Hyperwallet endpoint via Basic Authentication to retrieve an [authentication token](https://jwt.io/). An authentication token is a JSON Web Token that will be used to authenticate the User to the Hyperwallet platform. For example:

```
curl -X "POST" "https://api.sandbox.hyperwallet.com/rest/v3/users/{user-token}/authentication-token" \
-u userName:password \
-H "Content-type: application/json" \
-H "Accept: application/json"
```

The HyperwalletAuthenticationTokenProvider interface provides the Hyperwallet Android Core SDK with an abstraction to retrieve an authentication token. 

An example of how to implement HyperwalletAuthenticationTokenProvider:

```java

public class TestAuthenticationProvider implements HyperwalletAuthenticationTokenProvider {

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    private static final String mBaseUrl = "http://your/server/to/retrieve/authenticationToken";

    @Override
    public void retrieveAuthenticationToken(final HyperwalletAuthenticationTokenListener listener) {

        OkHttpClient client = new OkHttpClient();

        String payload = "{}";
        RequestBody body = RequestBody.create(JSON, payload);
        Request request = new Request.Builder()
                .url(baseUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFailure(UUID.randomUUID(), e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                listener.onSuccess(response.body().string());
            }
        });
    }
}

```
## Usage
The functions in UI SDK are available to use once the authentication is done.

### Using ```getIntentListTransferMethodActivity```

```
@Override
public void onClick(View view) {
    Intent it = mHyperwalletTransferMethodUi.getIntentListTransferMethodActivity(MainActivity.this);
    startActivity(it);
}
```

### Using ```getIntentSelectTransferMethodActivity```

```
@Override
public void onClick(View view) {
    Intent it = mHyperwalletTransferMethodUi.getIntentSelectTransferMethodActivity(MainActivity.this);
    startActivity(it);
}
```

### Using ```getIntentAddTransferMethodActivity```
```
@Override
public void onClick(View view) {
    Intent it = mHyperwalletTransferMethodUi.getIntentAddTransferMethodActivity(MainActivity.this, 
                                                                  "US", 
                                                                  "USD", 
                                                                  "BANK_ACCOUNT");
    startActivity(it);
}

```

## License
The Hyperwallet Android UI SDK is open source and available under the [MIT](https://github.com/hyperwallet/hyperwallet-android-ui-sdk/blob/master/LICENSE) license.
