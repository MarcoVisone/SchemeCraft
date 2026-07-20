package com.xyra.schemecraft.service;

import com.xyra.schemecraft.dao.*;
import com.xyra.schemecraft.exception.EntityNotFoundException;
import com.xyra.schemecraft.exception.InactiveEntityException;
import com.xyra.schemecraft.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class EntityValidator {
    private static final Logger logger = LoggerFactory.getLogger(EntityValidator.class);

    private final CountryDAO countryDAO;
    private final CurrencyDAO currencyDAO;
    private final LanguageDAO languageDAO;
    private final AddressDAO addressDAO;
    private final PaymentMethodDAO paymentMethodDAO;
    private final PaymentMethodTypeDAO paymentMethodTypeDAO;
    private final AccountDAO accountDAO;

    public EntityValidator() {
        this.countryDAO = new CountryDAO();
        this.currencyDAO = new CurrencyDAO();
        this.languageDAO = new LanguageDAO();
        this.addressDAO = new AddressDAO();
        this.paymentMethodDAO = new PaymentMethodDAO();
        this.paymentMethodTypeDAO = new PaymentMethodTypeDAO();
        this.accountDAO = new AccountDAO();
    }

    public CountryBean validateActiveCountry(Connection connection, String countryId) throws SQLException {
        CountryBean country = countryDAO.findById(connection, countryId).orElseThrow(() -> {
            logger.error("Country not found for ID: {}", countryId);
            return new EntityNotFoundException("Country not found for ID: " + countryId,
                    EntityNotFoundException.EntityType.COUNTRY);
        });

        if (!country.isActive()) {
            logger.error("Country is not active for ID: {}", countryId);
            throw new InactiveEntityException("Country with id " + countryId + " is not active",
                    InactiveEntityException.EntityType.COUNTRY);
        }

        return country;
    }

    public CurrencyBean validateActiveCurrency(Connection connection, String currencyId) throws SQLException {
        CurrencyBean currency = currencyDAO.findById(connection, currencyId).orElseThrow(() -> {
            logger.error("Currency not found for ID: {}", currencyId);
            return new EntityNotFoundException("Currency not found for ID: " + currencyId,
                    EntityNotFoundException.EntityType.CURRENCY);
        });

        if (!currency.isActive()) {
            logger.error("Currency is not active for ID: {}", currencyId);
            throw new InactiveEntityException("Currency with id " + currencyId + " is not active",
                    InactiveEntityException.EntityType.CURRENCY);
        }

        return currency;
    }

    public LanguageBean validateLanguage(Connection connection, String languageId) throws SQLException {
        return languageDAO.findById(connection, languageId).orElseThrow(() -> {
            logger.error("Language not found for ID: {}", languageId);
            return new EntityNotFoundException("Language not found for ID: " + languageId,
                    EntityNotFoundException.EntityType.LANGUAGE);
        });
    }

    public AddressBean validateActiveDefaultAddress(Connection connection, String accountId) throws SQLException {
        AddressBean address = addressDAO.findDefaultByAccountId(connection, accountId).orElseThrow(() -> {
            logger.error("Default address not found for Account ID: {}", accountId);
            return new EntityNotFoundException("Default address not found for Account ID: " + accountId,
                    EntityNotFoundException.EntityType.ADDRESS);
        });

        if (!address.isActive()) {
            logger.error("Default address is not active for Account ID: {} (Address ID: {})", accountId,
                    address.getAddressId());
            throw new InactiveEntityException("Address with id " + address.getAddressId() + " is not active",
                    InactiveEntityException.EntityType.ADDRESS);
        }

        return address;
    }

    public AccountBean validateActiveAccount(Connection connection, String accountId) throws SQLException {
        AccountBean account = accountDAO.findById(connection, accountId).orElseThrow(() -> {
            logger.error("Account not found for ID: {}", accountId);
            return new EntityNotFoundException("Account not found for ID: " + accountId,
                    EntityNotFoundException.EntityType.ACCOUNT);
        });

        if (!account.isActive()) {
            logger.error("Account is not active for ID: {}", accountId);
            throw new InactiveEntityException("Account with id " + accountId + " is not active",
                    InactiveEntityException.EntityType.ACCOUNT);
        }

        return account;
    }

    public PaymentMethodBean validateActiveDefaultPaymentMethod(Connection connection, String accountId)
            throws SQLException {
        PaymentMethodBean paymentMethod = paymentMethodDAO.findDefaultByAccountId(connection, accountId)
                .orElseThrow(() -> {
                    logger.error("No default payment method found for Account: {}", accountId);
                    return new EntityNotFoundException("No default payment method found for account " + accountId,
                            EntityNotFoundException.EntityType.PAYMENT_METHOD);
                });

        int paymentMethodTypeId = paymentMethod.getMethodType();

        PaymentMethodTypeBean paymentMethodType = paymentMethodTypeDAO.findById(connection, paymentMethodTypeId)
                .orElseThrow(() -> {
                    logger.error("Payment method type not found for ID: {}", paymentMethodTypeId);
                    return new EntityNotFoundException("Invalid payment method type",
                            EntityNotFoundException.EntityType.PAYMENT_METHOD_TYPE);
                });

        if (!paymentMethodType.isActive()) {
            logger.error("Payment method type is not active for ID: {}", paymentMethodTypeId);
            throw new InactiveEntityException("PaymentMethodType with id " + paymentMethodTypeId + " is not active",
                    InactiveEntityException.EntityType.PAYMENT_METHOD_TYPE);
        }

        return paymentMethod;
    }
}