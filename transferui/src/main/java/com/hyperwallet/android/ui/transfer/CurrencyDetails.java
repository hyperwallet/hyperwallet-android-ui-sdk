package com.hyperwallet.android.ui.transfer;


public class CurrencyDetails {

    private Integer id;
    private String name;
    private String currencyCode;
    private String isocurrencyCode;
    private String baseunit;
    private Integer denominationamount;
    private Integer decimals;
    private Integer hiddenDecimals;
    private String symbol;
    private Integer exchangeable;
    private Integer governmentissued;
    private Integer groupingUsed;
    private Integer fxTransactionVisible;
    private String displayedAs;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getIsocurrencyCode() {
        return isocurrencyCode;
    }

    public void setIsocurrencyCode(String isocurrencyCode) {
        this.isocurrencyCode = isocurrencyCode;
    }

    public String getBaseunit() {
        return baseunit;
    }

    public void setBaseunit(String baseunit) {
        this.baseunit = baseunit;
    }

    public Integer getDenominationamount() {
        return denominationamount;
    }

    public void setDenominationamount(Integer denominationamount) {
        this.denominationamount = denominationamount;
    }

    public Integer getDecimals() {
        return decimals;
    }

    public void setDecimals(Integer decimals) {
        this.decimals = decimals;
    }

    public Integer getHiddenDecimals() {
        return hiddenDecimals;
    }

    public void setHiddenDecimals(Integer hiddenDecimals) {
        this.hiddenDecimals = hiddenDecimals;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getExchangeable() {
        return exchangeable;
    }

    public void setExchangeable(Integer exchangeable) {
        this.exchangeable = exchangeable;
    }

    public Integer getGovernmentissued() {
        return governmentissued;
    }

    public void setGovernmentissued(Integer governmentissued) {
        this.governmentissued = governmentissued;
    }

    public Integer getGroupingUsed() {
        return groupingUsed;
    }

    public void setGroupingUsed(Integer groupingUsed) {
        this.groupingUsed = groupingUsed;
    }

    public Integer getFxTransactionVisible() {
        return fxTransactionVisible;
    }

    public void setFxTransactionVisible(Integer fxTransactionVisible) {
        this.fxTransactionVisible = fxTransactionVisible;
    }

    public String getDisplayedAs() {
        return displayedAs;
    }

    public void setDisplayedAs(String displayedAs) {
        this.displayedAs = displayedAs;
    }
}
