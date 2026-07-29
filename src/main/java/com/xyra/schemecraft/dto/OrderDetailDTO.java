package com.xyra.schemecraft.dto;

import java.util.ArrayList;
import java.util.List;

import com.xyra.schemecraft.model.AddressBean;
import com.xyra.schemecraft.model.CurrencyBean;
import com.xyra.schemecraft.model.OrderBean;
import com.xyra.schemecraft.model.OrderStatusBean;

/**
 * Data Transfer Object (DTO) aggregating all granular details of an order,
 * including its domain entity, current status, shipping address, currency context, and line items.
 */
public class OrderDetailDTO {

    /** Primary order domain entity containing core transaction metadata. */
    private OrderBean order;

    /** Detailed status descriptor associated with the current order state. */
    private OrderStatusBean statusInfo;

    /** Shipping or billing address linked to this order. */
    private AddressBean address;

    /** Currency metadata applied to the monetary amounts in this order. */
    private CurrencyBean currency;

    /** List of individual item breakdown entries included in the order. */
    private List<OrderItemDetailDTO> items = new ArrayList<>();

    /**
     * Default no-argument constructor initializing an empty item list.
     */
    public OrderDetailDTO() {
    }

    /**
     * Constructs a fully-initialized OrderDetailDTO.
     *
     * @param order      Core order domain entity
     * @param statusInfo Detailed order status information
     * @param address    Associated address model
     * @param currency   Currency specification
     * @param items      List of granular item detail DTOs
     */
    public OrderDetailDTO(OrderBean order, OrderStatusBean statusInfo, AddressBean address,
                          CurrencyBean currency, List<OrderItemDetailDTO> items) {
        this.order = order;
        this.statusInfo = statusInfo;
        this.address = address;
        this.currency = currency;
        this.items = (items != null) ? items : new ArrayList<>();
    }

    // --- Getters and Setters ---

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
        this.items = (items != null) ? items : new ArrayList<>();
    }
}
