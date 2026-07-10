package com.xyra.schemecraft.model;

import java.io.Serializable;

public class Currency implements Serializable {
    private static final long serialVersionUID = 1L;

    private String currencyId;
    private String currencyName;
    private boolean isActive;
    private String symbol;

    public Currency() {
    }

    public Currency(String currencyId, String currencyName, boolean isActive, String symbol) {
        this.currencyId = currencyId;
        this.currencyName = currencyName;
        this.isActive = isActive;
        this.symbol = symbol;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "currencyId='" + currencyId + '\'' +
                ", currencyName='" + currencyName + '\'' +
                ", isActive=" + isActive +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}
