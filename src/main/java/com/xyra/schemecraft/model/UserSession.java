package com.xyra.schemecraft.model;

import java.io.Serializable;

public class UserSession implements Serializable {
    private static final long serialVersionUID = 1L;

    private AccountBean account;
    private String currentCurrencyId;
    private String currentLanguageId;

    public UserSession() {
        this.account = null;
    }

    public boolean isLoggedIn() {
        return this.account != null;
    }

    public boolean isAdmin() {
        return isLoggedIn() && this.account.isAdmin();
    }

    public void invalidate() {
        this.account = null;
    }

    public AccountBean getAccount() {
        return account;
    }

    public void setAccount(AccountBean account) {
        this.account = account;
        if (account != null) {
            if (account.getCurrencyId() != null) {
                this.currentCurrencyId = account.getCurrencyId();
            }
            if (account.getLanguageId() != null) {
                this.currentLanguageId = account.getLanguageId();
            }
        }
    }

    public String getCurrentCurrencyId() {
        return currentCurrencyId;
    }

    public void setCurrentCurrencyId(String currentCurrencyId) {
        this.currentCurrencyId = currentCurrencyId;
    }

    public String getCurrentLanguageId() {
        return currentLanguageId;
    }

    public void setCurrentLanguageId(String currentLanguageId) {
        this.currentLanguageId = currentLanguageId;
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "isLoggedIn=" + isLoggedIn() +
                ", isAdmin=" + isAdmin() +
                ", currency='" + currentCurrencyId + '\'' +
                ", language='" + currentLanguageId + '\'' +
                '}';
    }
}
