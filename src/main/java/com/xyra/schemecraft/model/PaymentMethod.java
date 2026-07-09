package com.xyra.schemecraft.model;

import java.io.Serializable;

public class PaymentMethod implements Serializable {
    private static final long serialVersionUID = 1L;

    private String paymentMethodId;
    private String accountId;
    private boolean flagDefault;
    private int methodType;
    private String paymentToken;
    private String cardBrand;
    private String cardLastFour;
    private String cardExpiration;
    private String paymentEmail;

    public PaymentMethod() {
    }

    public PaymentMethod(String paymentMethodId, String accountId, boolean flagDefault, int methodType,
                         String paymentToken, String cardBrand, String cardLastFour, String cardExpiration,
                         String paymentEmail) {
        this.paymentMethodId = paymentMethodId;
        this.accountId = accountId;
        this.flagDefault = flagDefault;
        this.methodType = methodType;
        this.paymentToken = paymentToken;
        this.cardBrand = cardBrand;
        this.cardLastFour = cardLastFour;
        this.cardExpiration = cardExpiration;
        this.paymentEmail = paymentEmail;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public boolean isFlagDefault() {
        return flagDefault;
    }

    public void setFlagDefault(boolean flagDefault) {
        this.flagDefault = flagDefault;
    }

    public int getMethodType() {
        return methodType;
    }

    public void setMethodType(int methodType) {
        this.methodType = methodType;
    }

    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public String getCardExpiration() {
        return cardExpiration;
    }

    public void setCardExpiration(String cardExpiration) {
        this.cardExpiration = cardExpiration;
    }

    public String getPaymentEmail() {
        return paymentEmail;
    }

    public void setPaymentEmail(String paymentEmail) {
        this.paymentEmail = paymentEmail;
    }

    @Override
    public String toString() {
        return "PaymentMethod{" +
                "paymentMethodId='" + paymentMethodId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", flagDefault=" + flagDefault +
                ", methodType=" + methodType +
                ", paymentToken='" + paymentToken + '\'' +
                ", cardBrand='" + cardBrand + '\'' +
                ", cardLastFour='" + cardLastFour + '\'' +
                ", cardExpiration='" + cardExpiration + '\'' +
                ", paymentEmail='" + paymentEmail + '\'' +
                '}';
    }
}
