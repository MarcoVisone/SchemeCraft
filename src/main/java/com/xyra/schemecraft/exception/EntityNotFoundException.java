package com.xyra.schemecraft.exception;

public class EntityNotFoundException extends DAOException {

    public enum EntityType {
        ACCOUNT,
        COUNTRY,
        CURRENCY,
        LANGUAGE,
        ADDRESS,
        PAYMENT_METHOD,
        PAYMENT_METHOD_TYPE,
        PRODUCT,
        ORDER,
        UNKNOWN
    }

    private final EntityType entityType;

    public EntityNotFoundException(String message) {
        super(message);
        this.entityType = EntityType.UNKNOWN;
    }

    public EntityNotFoundException(String message, EntityType entityType) {
        super(message);
        this.entityType = entityType;
    }

    public EntityType getEntityType() {
        return entityType;
    }
}