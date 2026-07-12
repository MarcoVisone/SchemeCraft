package com.xyra.schemecraft.model;

import java.io.Serializable;

public class OrderStatusBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private int statusId;
    private String statusName;

    public OrderStatusBean() {
    }

    public OrderStatusBean(int statusId, String statusName) {
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
