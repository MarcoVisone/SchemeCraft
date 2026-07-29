package com.xyra.schemecraft.dto;

import com.xyra.schemecraft.model.AccountBean;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the contextual session state of an active user within the SchemeCraft platform.
 */
public class UserSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** The authenticated account details of the user. */
    private AccountBean account;

    /** The currently active currency code in the user's session. */
    private String currentCurrencyId;

    /** The currently active language code in the user's session. */
    private String currentLanguageId;

    /**
     * Default constructor initializing an anonymous guest session.
     */
    public UserSession() {
        this.account = null;
    }

    /**
     * Helper check to determine if the current session belongs to an authenticated user.
     *
     * @return true if an account is bound to this session; false otherwise
     */
    public boolean isLoggedIn() {
        return this.account != null;
    }

    /**
     * Helper check to determine if the active session user has administrative privileges.
     *
     * @return true if the user is logged in and possesses administrator privileges; false otherwise
     */
    public boolean isAdmin() {
        return isLoggedIn() && this.account.isAdmin();
    }

    /**
     * Invalidates the active session by completely purging the bound account
     * and resetting localization variables to a clean state.
     */
    public void invalidate() {
        this.account = null;
        this.currentCurrencyId = null;
        this.currentLanguageId = null;
    }

    // --- Getters and Setters ---

    public AccountBean getAccount() {
        return this.account;
    }

    /**
     * Binds an authenticated Account to the session.
     * Automatically inherits preferred localization preferences (language, currency)
     * defined on the account profile if present.
     *
     * @param account The authenticated Account to bind
     */
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
        return this.currentCurrencyId;
    }

    public void setCurrentCurrencyId(String currentCurrencyId) {
        this.currentCurrencyId = currentCurrencyId;
    }

    public String getCurrentLanguageId() {
        return this.currentLanguageId;
    }

    public void setCurrentLanguageId(String currentLanguageId) {
        this.currentLanguageId = currentLanguageId;
    }

    // --- Standard Object Override Methods ---

    /**
     * Compares this user session with another object for equality.
     * Equality is determined by the active account bound to the session.
     *
     * @param o The reference object to compare
     * @return true if both sessions share the exact same account; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSession that = (UserSession) o;
        return Objects.equals(account, that.account);
    }

    /**
     * Generates a hash code based on the bound account.
     *
     * @return A hash code value for this session
     */
    @Override
    public int hashCode() {
        return Objects.hash(account);
    }

    /**
     * Returns a string representation of the UserSession state.
     * Keeps track of security contexts (login status and administrative rights) without exposing
     * sensitive password hashes or user personal details.
     *
     * @return Formatted string representation of this instance
     */
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
