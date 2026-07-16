package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Represents the Country domain model and data transfer object within the application.
 */
public class CountryBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier for the country. */
    @NotBlank(message = "Country ID cannot be blank")
    private String countryId;

    /** Official display name of the country. */
    @NotBlank(message = "Country name cannot be blank")
    @Size(max = 100, message = "Country name cannot exceed {max} characters")
    private String countryName;

    /** Flag indicating if the country is active and supported for transactions. */
    private boolean isActive;

    /** Tax rate applied to purchases made from this country. */
    @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
    private BigDecimal tax;

    /**
     * Default no-argument constructor.
     */
    public CountryBean() {
    }

    /**
     * Constructs a fully-initialized CountryBean.
     *
     * @param countryId   Unique country identifier (e.g., "US", "IT")
     * @param countryName Display name of the country
     * @param isActive    Active state of the country
     * @param tax         Tax rate applied to transactions
     */
    public CountryBean(String countryId, String countryName, boolean isActive, BigDecimal tax) {
        this.countryId = countryId;
        this.countryName = countryName;
        this.isActive = isActive;
        this.tax = tax;
    }

    // --- Getters and Setters ---

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive; // Added explicit 'this' reference for stylistic consistency
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    // --- Standard Object Override Methods ---

    /**
     * Compares this country with another object for equality.
     * Equality is determined strictly by the unique, case-insensitive countryId.
     *
     * @param o The reference object to compare
     * @return true if the objects share the same countryId; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountryBean that = (CountryBean) o;
        return Objects.equals(
                countryId != null ? countryId.toUpperCase() : null,
                that.countryId != null ? that.countryId.toUpperCase() : null
        );
    }

    /**
     * Generates a hash code based on the unique countryId.
     *
     * @return A hash code value for this country bean
     */
    @Override
    public int hashCode() {
        return Objects.hash(countryId != null ? countryId.toUpperCase() : null);
    }

    /**
     * Returns a string representation of the CountryBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "CountryBean{" + // Fixed class name consistency
                "countryId='" + countryId + '\'' +
                ", countryName='" + countryName + '\'' +
                ", isActive=" + isActive +
                ", tax=" + tax +
                '}';
    }
}
