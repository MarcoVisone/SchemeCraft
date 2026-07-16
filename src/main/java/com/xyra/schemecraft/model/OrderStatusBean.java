package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Represents the OrderStatus domain model and data transfer object within the application.
 */
public class OrderStatusBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique numerical identifier representing the specific order state. */
    private int statusId;

    /** Descriptive display name of the status. */
    @NotBlank(message = "Status name cannot be blank")
    @Size(max = 50, message = "Status name cannot exceed {max} characters")
    private String statusName;

    /**
     * Default no-argument constructor.
     */
    public OrderStatusBean() {
    }

    /**
     * Constructs a fully-initialized OrderStatusBean.
     *
     * @param statusId   Unique numerical identifier of the status
     * @param statusName Descriptive name of the status
     */
    public OrderStatusBean(int statusId, String statusName) {
        this.statusId = statusId;
        this.statusName = statusName;
    }

    // --- Getters and Setters ---

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    // --- Standard Object Override Methods ---

    /**
     * Compares this order status with another object for equality.
     * Equality is determined strictly by the unique statusId.
     *
     * @param o The reference object to compare
     * @return true if the objects share the same statusId; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderStatusBean that = (OrderStatusBean) o;
        return statusId == that.statusId;
    }

    /**
     * Generates a hash code based on the unique statusId.
     *
     * @return A hash code value for this order status bean
     */
    @Override
    public int hashCode() {
        return Objects.hash(statusId);
    }

    /**
     * Returns a string representation of the OrderStatusBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "OrderStatusBean{" + // Fixed class name consistency
                "statusId=" + statusId +
                ", statusName='" + statusName + '\'' +
                '}';
    }
}
