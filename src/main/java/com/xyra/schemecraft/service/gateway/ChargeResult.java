package com.xyra.schemecraft.service.gateway;

public record ChargeResult(boolean success, String transactionId, String errorCode) {

    public static ChargeResult success(String transactionId) {
        return new ChargeResult(true, transactionId, null);
    }

    public static ChargeResult failure(String errorCode) {
        return new ChargeResult(false, null, errorCode);
    }
}
