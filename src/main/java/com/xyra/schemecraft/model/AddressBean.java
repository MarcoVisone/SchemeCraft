package com.xyra.schemecraft.model;

import java.io.Serializable;

public class AddressBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String addressId;
    private String accountId;
    private String countryId;
    private String city;
    private boolean flagDefault;
    private boolean isActive;
    private String postalCode;
    private String stateProvince;
    private String streetAddress;

    public AddressBean() {
    }

    public AddressBean(String addressId, String accountId, String countryId, String city, boolean flagDefault,
                       boolean isActive, String postalCode, String stateProvince, String streetAddress) {
        this.addressId = addressId;
        this.accountId = accountId;
        this.countryId = countryId;
        this.city = city;
        this.flagDefault = flagDefault;
        this.isActive = isActive;
        this.postalCode = postalCode;
        this.stateProvince = stateProvince;
        this.streetAddress = streetAddress;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isDefault() {
        return flagDefault;
    }

    public void setDefault(boolean flagDefault) {
        this.flagDefault = flagDefault;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    @Override
    public String toString() {
        return "Address{" +
                "addressId='" + addressId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", countryId='" + countryId + '\'' +
                ", city='" + city + '\'' +
                ", flagDefault=" + flagDefault +
                ", isActive=" + isActive +
                ", postalCode='" + postalCode + '\'' +
                ", stateProvince='" + stateProvince + '\'' +
                ", streetAddress='" + streetAddress + '\'' +
                '}';
    }
}
