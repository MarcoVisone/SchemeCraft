package com.xyra.schemecraft.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

public record ReviewRequest(
        String accountId,

        String productId,

        @Min(value = 1, message = "Rating must be at least {value}")
        @Max(value = 5, message = "Rating cannot exceed {value}")
        int rating,

        String comment
) {}
