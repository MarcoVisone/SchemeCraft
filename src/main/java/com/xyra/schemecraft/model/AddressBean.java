package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotBlank;

/**
 * Represents the Address domain model and data transfer object within the application.
 */
public class AddressBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier for the address. */
    @NotBlank(message = "Address ID cannot be blank")
    private String addressId;

    /** Reference identifier of the associated Account. */
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;

    /** Reference identifier of the country. */
    @NotBlank(message = "Country ID cannot be blank")
    private String countryId;

    /** Name of the city. */
    @NotBlank(message = "City cannot be blank")
    private String city;

    /** Flag indicating if this is the default address for the associated account. */
    private boolean isDefault;

    /** Flag indicating if the address record is currently active. */
    private boolean isActive;

    /** Postal code for the address. */
    @NotBlank(message = "Postal code cannot be blank")
    private String postalCode;

    /** State, province, or region. */
    @NotBlank(message = "State/Province cannot be blank")
    private String stateProvince;

    /** Street name, building number, and apartment/suite details. */
    @NotBlank(message = "Street address cannot be blank")
    private String streetAddress;

    /**
     * Default no-argument constructor.
     */
    public AddressBean() {
    }

    /**
     * Constructs a fully-initialized AddressBean.
     *
     * @param addressId     Unique identifier for the address
     * @param accountId     Associated account identifier
     * @param countryId     Associated country identifier
     * @param city          City name
     * @param isDefault     True if this is the primary address
     * @param isActive      True if the address record is active
     * @param postalCode    Postal or ZIP code
     * @param stateProvince State or province name
     * @param streetAddress Detailed street address
     */
    public AddressBean(String addressId, String accountId, String countryId, String city, boolean isDefault,
                       boolean isActive, String postalCode, String stateProvince, String streetAddress) {
        this.addressId = addressId;
        this.accountId = accountId;
        this.countryId = countryId;
        this.city = city;
        this.isDefault = isDefault;
        this.isActive = isActive;
        this.postalCode = postalCode;
        this.stateProvince = stateProvince;
        this.streetAddress = streetAddress;
    }

    // --- Getters and Setters ---

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
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
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

    // --- Standard Object Override Methods ---

    /**
     * Compares this address with another object for equality.
     * Equality is determined by the unique addressId and its associated accountId.
     *
     * @param o The reference object to compare
     * @return true if the objects are equivalent; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressBean that = (AddressBean) o;
        return Objects.equals(addressId, that.addressId) &&
                Objects.equals(accountId, that.accountId);
    }

    /**
     * Generates a hash code value for this address.
     *
     * @return A hash code based on addressId and accountId
     */
    @Override
    public int hashCode() {
        return Objects.hash(addressId, accountId);
    }

    /**
     * Returns a string representation of the AddressBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "AddressBean{" + // Fixed class name consistency
                "addressId='" + addressId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", countryId='" + countryId + '\'' +
                ", city='" + city + '\'' +
                ", isDefault=" + isDefault +
                ", isActive=" + isActive +
                ", postalCode='" + postalCode + '\'' +
                ", stateProvince='" + stateProvince + '\'' +
                ", streetAddress='" + streetAddress + '\'' +
                '}';
    }
}
