package com.xyra.schemecraft.model;

import java.math.BigDecimal;

public record ProductRequest(
        String accountId,
        String currencyId,
        String productName,
        String description,
        BigDecimal discount,
        BigDecimal price,
        Integer stockQuantity
) {}
