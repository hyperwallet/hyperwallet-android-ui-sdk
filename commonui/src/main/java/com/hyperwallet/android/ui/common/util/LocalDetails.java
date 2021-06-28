package com.hyperwallet.android.ui.common.util;

public class LocalDetails {
    private String language;
    private String countryCode;

    public LocalDetails(String language, String countryCode) {
        this.language = language;
        this.countryCode = countryCode;
    }

    public String getLanguage() {
        return language;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
