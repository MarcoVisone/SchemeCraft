package com.xyra.schemecraft.model;

import java.io.Serializable;

public class PaymentMethodBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * CREATE TABLE IF NOT EXISTS payment_method (
     *     payment_method_id VARCHAR(36) PRIMARY KEY,
     *     account_id VARCHAR(36) NOT NULL,
     *     method_type INT NOT NULL,
     *     card_brand VARCHAR(30) NULL,
     *     card_expiration VARCHAR(7) NULL,
     *     card_last_four VARCHAR(4) NULL,
     *     flag_default BOOLEAN NULL DEFAULT NULL CHECK (flag_default = TRUE),
     *     payment_email VARCHAR(100) NULL,
     *     payment_token VARCHAR(255) NOT NULL,
     *     CONSTRAINT fk_payment_account FOREIGN KEY (account_id) REFERENCES account(account_id)
     *         ON DELETE CASCADE ON UPDATE CASCADE,
     *     CONSTRAINT fk_payment_method_type FOREIGN KEY (method_type) REFERENCES payment_method_type(type_id)
     *         ON DELETE RESTRICT ON UPDATE CASCADE,
     *     CONSTRAINT uq_default_payment_account UNIQUE (flag_default, account_id)
     * );
     */

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
