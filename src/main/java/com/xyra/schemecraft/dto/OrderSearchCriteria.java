package com.xyra.schemecraft.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) encapsulating dynamic query search parameters, pagination,
 * and sorting criteria for the SchemeCraft admin order listing.
 * Includes defensive validations to prevent invalid values and potential performance issues.
 */
public class OrderSearchCriteria {

    /** Default number of records per page if not specified or invalid. */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /** Minimum allowed page index for pagination. */
    private static final int MIN_PAGE_NUMBER = 1;

    /** Maximum allowed page size boundary to prevent excessive memory consumption. */
    private static final int MAX_PAGE_SIZE = 100;

    /** Filter orders created on or after this timestamp. */
    private LocalDateTime dateFrom;

    /** Filter orders created on or before this timestamp. */
    private LocalDateTime dateTo;

    /** Unique identifier of the customer to filter by. */
    private String customerId;

    /** Customer display username filter keyword. */
    private String customerUsername;

    /** Customer email address filter keyword. */
    private String customerEmail;

    /** Numerical order status code filter. */
    private Integer status;

    /** Column or field name used for sorting query results. */
    private String orderByColumn;

    /** Flag indicating sorting direction; true for ascending, false for descending. */
    private Boolean ascending = true;

    /** Current requested page number for paginated results (1-based index). */
    private int pageNumber = MIN_PAGE_NUMBER;

    /** Maximum quantity of records to retrieve per page. */
    private int pageSize = DEFAULT_PAGE_SIZE;

    /**
     * Default constructor initializing search criteria with default pagination settings.
     */
    public OrderSearchCriteria() {
    }

    // --- Getters and Setters ---

    public LocalDateTime getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDateTime dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDateTime getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDateTime dateTo) {
        this.dateTo = dateTo;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = (customerId != null) ? customerId.trim() : null;
    }

    public String getCustomerUsername() {
        return customerUsername;
    }

    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = (customerUsername != null) ? customerUsername.trim() : null;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = (customerEmail != null) ? customerEmail.trim() : null;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getOrderByColumn() {
        return orderByColumn;
    }

    public void setOrderByColumn(String orderByColumn) {
        this.orderByColumn = (orderByColumn != null) ? orderByColumn.trim() : null;
    }

    public Boolean getAscending() {
        return ascending;
    }

    public void setAscending(Boolean ascending) {
        this.ascending = ascending == null || ascending;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = Math.max(pageNumber, MIN_PAGE_NUMBER);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize <= 0) {
            this.pageSize = DEFAULT_PAGE_SIZE;
        } else {
            this.pageSize = Math.min(pageSize, MAX_PAGE_SIZE);
        }
    }

    // --- Standard Object Override Methods ---

    /**
     * Returns a string representation of the OrderSearchCriteria object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "OrderSearchCriteria{" +
                "dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", customerId='" + customerId + '\'' +
                ", customerUsername='" + customerUsername + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", status=" + status +
                ", orderByColumn='" + orderByColumn + '\'' +
                ", ascending=" + ascending +
                ", pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                '}';
    }
}
