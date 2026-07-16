package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Represents the Currency domain model and data transfer object within the application.
 */
public class CurrencyBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier for the currency. */
    @NotBlank(message = "Currency ID cannot be blank")
    private String currencyId;

    /** Official display name of the currency. */
    @NotBlank(message = "Currency name cannot be blank")
    @Size(max = 100, message = "Currency name cannot exceed {max} characters")
    private String currencyName;

    /** Flag indicating if this currency is currently active and supported for transactions. */
    @NotBlank(message = "Active status cannot be blank")
    private boolean isActive;

    /** Graphical representation symbol of the currency. */
    @NotBlank(message = "Currency symbol cannot be blank")
    @Size(max = 10, message = "Currency symbol cannot exceed {max} characters")
    private String symbol;

    /**
     * Default no-argument constructor.
     */
    public CurrencyBean() {
    }

    /**
     * Constructs a fully-initialized CurrencyBean.
     *
     * @param currencyId   Unique currency identifier (e.g., "USD")
     * @param currencyName Display name of the currency (e.g., "US Dollar")
     * @param isActive    Active state of the currency
     * @param symbol       Graphical symbol (e.g., "$")
     */
    public CurrencyBean(String currencyId, String currencyName, boolean isActive, String symbol) {
        this.currencyId = currencyId;
        this.currencyName = currencyName;
        this.isActive = isActive;
        this.symbol = symbol;
    }

    // --- Getters and Setters ---

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
        this.isActive = active; // Added explicit 'this' reference for stylistic consistency
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    // --- Standard Object Override Methods ---

    /**
     * Compares this currency with another object for equality.
     * Equality is determined strictly by the unique, case-insensitive currencyId.
     *
     * @param o The reference object to compare
     * @return true if the objects share the same currencyId; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrencyBean that = (CurrencyBean) o;
        return Objects.equals(
                currencyId != null ? currencyId.toUpperCase() : null,
                that.currencyId != null ? that.currencyId.toUpperCase() : null
        );
    }

    /**
     * Generates a hash code based on the unique currencyId.
     *
     * @return A hash code value for this currency bean
     */
    @Override
    public int hashCode() {
        return Objects.hash(currencyId != null ? currencyId.toUpperCase() : null);
    }

    /**
     * Returns a string representation of the CurrencyBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "CurrencyBean{" + // Fixed class name consistency
                "currencyId='" + currencyId + '\'' +
                ", currencyName='" + currencyName + '\'' +
                ", isActive=" + isActive +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}
