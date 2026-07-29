package com.xyra.schemecraft.dto;

import com.xyra.schemecraft.model.AddressBean;
import com.xyra.schemecraft.model.CurrencyBean;
import com.xyra.schemecraft.model.OrderBean;
import com.xyra.schemecraft.model.OrderStatusBean;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailDTO {

    private OrderBean order;
    private OrderStatusBean statusInfo;
    private AddressBean address;
    private CurrencyBean currency;
    private List<OrderItemDetailDTO> items = new ArrayList<>();

    public OrderDetailDTO() {
    }

    public OrderDetailDTO(OrderBean order, OrderStatusBean statusInfo, AddressBean address,
                          CurrencyBean currency, List<OrderItemDetailDTO> items) {
        this.order = order;
        this.statusInfo = statusInfo;
        this.address = address;
        this.currency = currency;
        this.items = (items != null) ? items : new ArrayList<>();
    }

    public OrderBean getOrder() {
        return order;
    }

    public void setOrder(OrderBean order) {
        this.order = order;
    }

    public OrderStatusBean getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(OrderStatusBean statusInfo) {
        this.statusInfo = statusInfo;
    }

    public AddressBean getAddress() {
        return address;
    }

    public void setAddress(AddressBean address) {
        this.address = address;
    }

    public CurrencyBean getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyBean currency) {
        this.currency = currency;
    }

    public List<OrderItemDetailDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDetailDTO> items) {
        this.items = items;
    }
}
