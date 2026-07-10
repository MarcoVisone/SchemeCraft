package com.xyra.schemecraft.model;

import java.io.Serializable;

public class OrderStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     *
     * CREATE TABLE IF NOT EXISTS order_status (
     *     status_id INT AUTO_INCREMENT PRIMARY KEY,
     *     status_name VARCHAR(50) NOT NULL UNIQUE
     * );
     */

    private int statusId;
    private String statusName;

    public OrderStatus() {
    }

    public OrderStatus(int statusId, String statusName) {
        this.statusId = statusId;
        this.statusName = statusName;
    }

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

    @Override
    public String toString() {
        return "OrderStatus{" +
                "statusId=" + statusId +
                ", statusName=" + statusName +
                '}';
    }
}
