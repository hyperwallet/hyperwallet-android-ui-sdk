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

### Declare and initialize the UI SDK components as necessary
```java
private HyperwalletReceiptUi mHyperwalletReceiptUi;
private HyperwalletTransferMethodUi mHyperwalletTransferMethodUi;
private HyperwalletTransferUi mHyperwalletTransferUi;

@Override
protected void onCreate(Bundle savedInstanceState) {
    mHyperwalletReceiptUi = HyperwalletReceiptUi.getInstance(hyperwalletAuthenticationTokenProvider);
    mHyperwalletTransferMethodUi = HyperwalletTransferMethodUi.getInstance(hyperwalletAuthenticationTokenProvider);
    mHyperwalletTransferUi = HyperwalletTransferUi.getInstance(hyperwalletAuthenticationTokenProvider);
}
```

### List the user's transfer methods
```java
@Override
public void onClick(View view) {
    Intent intent = mHyperwalletTransferMethodUi.getIntentListTransferMethodActivity(MainActivity.this);
    startActivity(intent);
}
```


### Select a transfer method type available by country and currency
```java
@Override
public void onClick(View view) {
    Intent intent = mHyperwalletTransferMethodUi.getIntentSelectTransferMethodActivity(MainActivity.this);
    startActivity(intent);
}
```

### Create/Add a transfer method
The form fields are based on the country, currency, user's profile type, and transfer method type should be passed to this Activity to create a new Transfer Method for those values.
```java
@Override
public void onClick(View view) {
    Intent intent = mHyperwalletTransferMethodUi.getIntentAddTransferMethodActivity(
            MainActivity.this,
            "US",
            "USD",
            "BANK_ACCOUNT",
            "INDIVIDUAL");
    startActivity(intent);
}
```

### Lists the user's receipts
```java
public void onClick(View view) {
    Intent intent = mHyperwalletReceiptUi.getIntentListUserReceiptActivity(MainActivity.this);
    startActivity(intent);
}
```

### Lists the prepaid card's receipts
```java
public void onClick(View view) {
    Intent intent = mHyperwalletReceiptUi.getIntentListPrepaidCardReceiptActivity(MainActivity.this, "trm-12345");
    startActivity(intent);
}
```

## NotificationCenter Events
```java
// TODO: 2019-10-03  
```

## Customize the visual style

UI SDK leverages the `res/values` folder for styling. Simply add your own `.xml` file in your project with the same name and override values inside of it as you see fit.
All the `.xml` from the SDK are listed below for your convenience:

**Common UI**
* commonui/res/values/colors.xml
* commonui/res/values/dimens.xml
* commonui/res/values/strings.xml
* commonui/res/values/styles.xml

**Receipt UI**
* receiptui/res/values/colors.xml
* receiptui/res/values/dimens.xml
* receiptui/res/values/strings.xml
* receiptui/res/values/styles.xml

**Transfer Method UI**
* transfermethodui/res/values/ic_launcher_background.xml
* transfermethodui/res/values/dimens.xml
* transfermethodui/res/values/strings.xml
* transfermethodui/res/values/styles.xml

### Custom Hyperwallet Styles
To create consistency, several components are packaged in their own theme. These all have the word `Hyperwallet` and need to be overriden if you want a component to have its own theme.
Listed below are the names of these styles:

**commonui/res/values/styles.xml**

| Style Name | Parent Style |
|:----------|:------------|
| Widget.Hyperwallet.ProgressBar | @style/Widget.AppCompat.ProgressBar |
| Widget.Hyperwallet.ProgressBar.CreateButton | @style/Widget.AppCompat.ProgressBar |
| TextAppearance.Hyperwallet.Headline4 | @style/TextAppearance.MaterialComponents.Headline4 |
| ThemeOverlay.Hyperwallet.Selector.ActionBar | @style/ThemeOverlay.AppCompat.ActionBar |
| "Base.Hyperwallet.RecyclerView | @style/Widget.AppCompat.ListView |
| Selector.Hyperwallet.RecyclerView | @style/Base.Hyperwallet.RecyclerView |
| Widget.Hyperwallet.CollapsingToolbar | @style/Widget.Design.CollapsingToolbar |
| Widget.Hyperwallet.FloatingActionButton | @style/Widget.MaterialComponents.FloatingActionButton |
| TextAppearance.Hyperwallet.Body2 | @style/Base.TextAppearance.Hyperwallet.Body2 |
| TextAppearance.Hyperwallet.Subtitle2 | @style/TextAppearance.MaterialComponents.Subtitle2 |
| Base.TextAppearance.Hyperwallet.Body2 | @style/TextAppearance.MaterialComponents.Body2 |
| Widget.Hyperwallet.Button | @style/Widget.AppCompat.Button |
| Widget.Hyperwallet.Text.Button | @style/Widget.Hyperwallet.Button |
| TextAppearance.Hyperwallet.Headline6 | @style/TextAppearance.MaterialComponents.Headline6 |
| Widget.Hyperwallet.TextInputLayout | Base.Widget.MaterialComponents.TextInputLayout |
| Widget.Hyperwallet.TextInputLayout.Disabled | Widget.Hyperwallet.TextInputLayout |
| Widget.Hyperwallet.TextInputEditText | Base.Widget.MaterialComponents.TextInputEditText |
| Theme.Hyperwallet.Confirmation.Dialog | Base.Theme.MaterialComponents.Light |
| Widget.Hyperwallet.Confirmation.Dialog.Alert | Base.Theme.MaterialComponents.Light.Dialog |
| Widget.Hyperwallet.Confirmation.Button.Negative | Widget.MaterialComponents.Button.TextButton |
| Widget.Hyperwallet.Confirmation.Button.Positive | Widget.MaterialComponents.Button.TextButton |
| Theme.Hyperwallet.Alert | Base.Theme.MaterialComponents.Light |
| Widget.Hyperwallet.Dialog.Alert | Base.Theme.MaterialComponents.Light.Dialog |
| Widget.Hyperwallet.Button.Negative | Widget.MaterialComponents.Button.TextButton.Dialog |
| Widget.Hyperwallet.Button.Positive | Widget.MaterialComponents.Button.TextButton.Dialog |
| TextAppearance.Hyperwallet.Dialog.Alert.Title | TextAppearance.MaterialComponents.Headline6 |
| TextAppearance.Hyperwallet.Dialog.Alert.Body | TextAppearance.MaterialComponents.Body1 |
| Widget.Hyperwallet.DatePicker | Theme.AppCompat.Light.Dialog |
| TextAppearance.Hyperwallet.Subtitle1 | @style/TextAppearance.MaterialComponents.Subtitle1 |
| Widget.Hyperwallet.Switch | Theme.AppCompat.Light |

**receiptui/res/values/styles.xml**

| Style Name | Parent Style |
|:----------|:------------|
| Widget.Hyperwallet.ProgressBar.Receipts | @style/Widget.AppCompat.ProgressBar |
| TextAppearance.Hyperwallet.Positive | TextAppearance.MaterialComponents.Subtitle1 |
| TextAppearance.Hyperwallet.Negative | TextAppearance.MaterialComponents.Subtitle1 |

## Error Handling
In Hyperwallet UI SDK, we categorize HyperwalletException into three groups:
* Unexpected Error
* Connectivity Issue (network errors)
* Business Errors (invalid input) 

### Unexpected Error
On `Unexpected Error`, a DialogFragment that only contains the `OK` button will be shown in the UI.

### Connectivity Issue (network errors)
On `Connectivity Issue`, a DialogFragment containing `Cancel` and `Try Again` will be shown on the UI. Network errors can happen due to connectivity issues for reasons including (but not limited to) poor-quality network connection and request timeouts from the server.

### Business Errors (invalid input)
Business errors happen when the Hyperwallet platform has found invalid information or some business restriction related to the data submitted and requires action from the user.

## License
The Hyperwallet Android UI SDK is open source and available under the [MIT](https://github.com/hyperwallet/hyperwallet-android-ui-sdk/blob/master/LICENSE) license.
