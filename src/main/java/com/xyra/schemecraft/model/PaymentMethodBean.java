package com.xyra.schemecraft.model;

import java.io.Serializable;

public class PaymentMethodBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String paymentMethodId;
    private String accountId;
    private int methodType;
    private String cardBrand;
    private String cardExpiration;
    private String cardLastFour;
    private boolean flagDefault;
    private String paymentEmail;
    private String paymentToken;

    public PaymentMethodBean() {
    }

    public PaymentMethodBean(String paymentMethodId, String accountId, int methodType, String cardBrand,
                             String cardExpiration, String cardLastFour, boolean flagDefault, String paymentEmail,
                             String paymentToken) {
        this.paymentMethodId = paymentMethodId;
        this.accountId = accountId;
        this.methodType = methodType;
        this.cardBrand = cardBrand;
        this.cardExpiration = cardExpiration;
        this.cardLastFour = cardLastFour;
        this.flagDefault = flagDefault;
        this.paymentEmail = paymentEmail;
        this.paymentToken = paymentToken;
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

    public int getMethodType() {
        return methodType;
    }

    public void setMethodType(int methodType) {
        this.methodType = methodType;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }

    public String getCardExpiration() {
        return cardExpiration;
    }

    public void setCardExpiration(String cardExpiration) {
        this.cardExpiration = cardExpiration;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public boolean isDefault() {
        return flagDefault;
    }

    public void setDefault(boolean flagDefault) {
        this.flagDefault = flagDefault;
    }

    public String getPaymentEmail() {
        return paymentEmail;
    }

    public void setPaymentEmail(String paymentEmail) {
        this.paymentEmail = paymentEmail;
    }

    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }

    @Override
    public String toString() {
        return "PaymentMethod{" +
                "paymentMethodId='" + paymentMethodId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", methodType=" + methodType +
                ", cardBrand='" + cardBrand + '\'' +
                ", cardExpiration='" + cardExpiration + '\'' +
                ", cardLastFour='" + cardLastFour + '\'' +
                ", flagDefault=" + flagDefault +
                ", paymentEmail='" + paymentEmail + '\'' +
                ", paymentToken='" + paymentToken + '\'' +
                '}';
    }
}
