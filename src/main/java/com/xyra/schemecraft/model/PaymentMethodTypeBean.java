package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Represents the PaymentMethodType domain model and data transfer object within the application.
 */
public class PaymentMethodTypeBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique numerical identifier for the payment method type. */
    private int typeId;

    /** Flag indicating if this payment method type is currently active and selectable by users. */
    private boolean isActive;

    /** Descriptive display name of the payment method type. */
    @NotBlank(message = "Type name cannot be blank")
    @Size(max = 50, message = "Type name cannot exceed {max} characters")
    private String typeName;

    /**
     * Default no-argument constructor.
     */
    public PaymentMethodTypeBean() {
    }

    /**
     * Constructs a PaymentMethodTypeBean with only the type name.
     *
     * @param typeName Display name of the payment method type
     */
    public PaymentMethodTypeBean(String typeName) {
        this(0, true, typeName);
    }

    /**
     * Constructs a fully-initialized PaymentMethodTypeBean.
     *
     * @param typeId   Unique numerical identifier
     * @param isActive Status state of this payment type
     * @param typeName Display name of the payment type
     */
    public PaymentMethodTypeBean(int typeId, boolean isActive, String typeName) {
        this.typeId = typeId;
        this.isActive = isActive;
        this.typeName = typeName;
    }

    // --- Getters and Setters ---

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    // --- Standard Object Override Methods ---

    /**
     * Compares this payment method type with another object for equality.
     * Equality is determined strictly by the unique typeId.
     *
     * @param o The reference object to compare
     * @return true if the objects share the same typeId; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentMethodTypeBean that = (PaymentMethodTypeBean) o;
        return typeId == that.typeId;
    }

    /**
     * Generates a hash code based on the unique typeId.
     *
     * @return A hash code value for this payment method type bean
     */
    @Override
    public int hashCode() {
        return Objects.hash(typeId);
    }

    /**
     * Returns a string representation of the PaymentMethodTypeBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "PaymentMethodTypeBean{" + // Fixed class name consistency
                "typeId=" + typeId +
                ", isActive=" + isActive +
                ", typeName='" + typeName + '\'' +
                '}';
    }
}
