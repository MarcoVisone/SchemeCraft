package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Represents the PaymentMethod domain model and data transfer object within the application.
 */
public class PaymentMethodBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier for the payment method. */
    @NotBlank(message = "Payment method ID cannot be blank")
    private String paymentMethodId;

    /** Reference identifier of the associated Account owning this payment method. */
    @NotBlank(message = "Account ID cannot be blank")
    private String accountId;

    /** Numeric code representing the payment type. */
    private int methodType;

    /** Brand of the credit card. */
    @Size(max = 50, message = "Card brand cannot exceed {max} characters")
    private String cardBrand;

    /** Expiration date of the card formatted as MM/YY or MM/YYYY. */
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2}|[0-9]{4})$",
            message = "Card expiration must match MM/YY or MM/YYYY format")
    private String cardExpiration;

    /** Last four digits of the credit card number for safe UI visualization. */
    @Size(min = 4, max = 4, message = "Card last four digits must be exactly 4 characters")
    private String cardLastFour;

    /** Flag indicating if this is the default payment method for the associated account. */
    private boolean isDefault;

    /** Email associated with digital payment services (e.g., PayPal email). */
    @Size(max = 100, message = "Payment email cannot exceed {max} characters")
    private String paymentEmail;

    /** Secure billing token issued by the payment gateway (e.g., Stripe, Braintree). */
    @NotBlank(message = "Payment token is required for transactions")
    private String paymentToken;

    /**
     * Default no-argument constructor.
     */
    public PaymentMethodBean() {
    }

    /**
     * Constructs a fully-initialized PaymentMethodBean.
     *
     * @param paymentMethodId Unique payment method identifier
     * @param accountId       Associated account owner identifier
     * @param methodType      Payment method type code
     * @param cardBrand       Credit card brand name
     * @param cardExpiration  Credit card expiration date
     * @param cardLastFour    Credit card last four digits
     * @param isDefault       True if this is the default payment option
     * @param paymentEmail    Digital payment account email
     * @param paymentToken    Secure transaction gateway token
     */
    public PaymentMethodBean(String paymentMethodId, String accountId, int methodType, String cardBrand,
                             String cardExpiration, String cardLastFour, boolean isDefault, String paymentEmail,
                             String paymentToken) {
        this.paymentMethodId = paymentMethodId;
        this.accountId = accountId;
        this.methodType = methodType;
        this.cardBrand = cardBrand;
        this.cardExpiration = cardExpiration;
        this.cardLastFour = cardLastFour;
        this.isDefault = isDefault;
        this.paymentEmail = paymentEmail;
        this.paymentToken = paymentToken;
    }

    // --- Getters and Setters ---

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
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
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

    // --- Standard Object Override Methods ---

    /**
     * Compares this payment method with another object for equality.
     * Equality is determined strictly by the unique paymentMethodId.
     *
     * @param o The reference object to compare
     * @return true if the objects share the same paymentMethodId; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentMethodBean that = (PaymentMethodBean) o;
        return Objects.equals(paymentMethodId, that.paymentMethodId);
    }

    /**
     * Generates a hash code based on the unique paymentMethodId.
     *
     * @return A hash code value for this payment method bean
     */
    @Override
    public int hashCode() {
        return Objects.hash(paymentMethodId);
    }

    /**
     * Returns a string representation of the PaymentMethodBean object.
     * Sensitive fields like 'paymentToken' are masked to ensure PCI-DSS compliance.
     *
     * @return Formatted string representation of this instance with masked tokens
     */
    @Override
    public String toString() {
        return "PaymentMethodBean{" + // Fixed class name consistency
                "paymentMethodId='" + paymentMethodId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", methodType=" + methodType +
                ", cardBrand='" + cardBrand + '\'' +
                ", cardExpiration='" + cardExpiration + '\'' +
                ", cardLastFour='" + cardLastFour + '\'' +
                ", isDefault=" + isDefault +
                ", paymentEmail='" + paymentEmail + '\'' +
                ", paymentToken='[MASKED]'" + // Token masked for security compliance
                '}';
    }
}
