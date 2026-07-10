package com.xyra.schemecraft.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Country implements Serializable {
    private static final long serialVersionUID = 1L;

    private String countryId;
    private String countryName;
    private boolean isActive;
    private BigDecimal tax;

    public Country() {
    }

    public Country(String countryId, String countryName, boolean isActive, BigDecimal tax) {
        this.countryId = countryId;
        this.countryName = countryName;
        this.isActive = isActive;
        this.tax = tax;
    }

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

    public void setActive(boolean active) {
        isActive = active;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    @Override
    public String toString() {
        return "Country{" +
                "countryId='" + countryId + '\'' +
                ", countryName='" + countryName + '\'' +
                ", isActive=" + isActive +
                ", tax=" + tax +
                '}';
    }
}
