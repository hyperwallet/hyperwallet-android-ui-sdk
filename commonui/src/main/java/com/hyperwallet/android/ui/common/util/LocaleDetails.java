package com.hyperwallet.android.ui.common.util;

public class LocaleDetails {
    private String language;
    private String countryCode;

    public LocaleDetails(String language, String countryCode) {
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
