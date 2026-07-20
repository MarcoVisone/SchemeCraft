package com.xyra.schemecraft.exception;

public class InactiveEntityException extends ServiceException {
    public enum EntityType {
        ACCOUNT,
        COUNTRY,
        CURRENCY,
        LANGUAGE,
        ADDRESS,
        PAYMENT_METHOD,
        PAYMENT_METHOD_TYPE,
        PRODUCT,
        UNKNOWN
    }

    private final EntityType entityType;

    public InactiveEntityException(String message) {
        super(message);
        this.entityType = EntityType.UNKNOWN;
    }

    public InactiveEntityException(String message, Throwable cause) {
        super(message, cause);
        this.entityType = EntityType.UNKNOWN;
    }

    public InactiveEntityException(String message, EntityType entityType) {
        super(message);
        this.entityType = entityType;
    }

    public EntityType getEntityType() {
        return entityType;
    }
}