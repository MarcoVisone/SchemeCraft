package com.xyra.schemecraft.model;

import java.io.Serializable;

public class PaymentMethodType implements Serializable {
    private static final long serialVersionUID = 1L;

    private int typeId;
    private String typeName;

    public PaymentMethodType() {
    }

    public PaymentMethodType(int typeId, String typeName) {
        this.typeId = typeId;
        this.typeName = typeName;
    }

    public PaymentMethodType(String typeName) {
        this.typeName = typeName;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return "PaymentMethodType{" +
                "typeId=" + typeId +
                ", typeName='" + typeName + '\'' +
                '}';
    }
}
