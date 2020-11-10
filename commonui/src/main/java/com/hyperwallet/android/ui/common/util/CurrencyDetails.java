package com.hyperwallet.android.ui.common.util;


public class CurrencyDetails {

    private Integer id;
    private String name;
    private String currencyCode;
    private String isoCurrencyCode;
    private String baseUnit;
    private Integer denominationAmount;
    private Integer decimals;
    private Integer hiddenDecimals;
    private Integer exchangeable;
    private Integer governmentIssued;
    private Integer groupingUsed;
    private Integer fxTransactionVisible;
    private String displayedAs;
    private String symbol;

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

    public String getIsoCurrencyCode() {
        return isoCurrencyCode;
    }

    public void setIsoCurrencyCode(String isoCurrencyCode) {
        this.isoCurrencyCode = isoCurrencyCode;
    }

    public String getBaseUnit() {
        return baseUnit;
    }

    public void setBaseUnit(String baseUnit) {
        this.baseUnit = baseUnit;
    }

    public Integer getDenominationAmount() {
        return denominationAmount;
    }

    public void setDenominationAmount(Integer denominationAmount) {
        this.denominationAmount = denominationAmount;
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

    public Integer getGovernmentIssued() {
        return governmentIssued;
    }

    public void setGovernmentIssued(Integer governmentIssued) {
        this.governmentIssued = governmentIssued;
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
