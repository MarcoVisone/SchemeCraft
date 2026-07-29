package com.xyra.schemecraft.service;

import com.xyra.schemecraft.dao.*;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.exception.EntityNotFoundException;
import com.xyra.schemecraft.exception.InactiveEntityException;
import com.xyra.schemecraft.exception.InsufficientStockException;
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
    private final ProductDAO productDAO;
    private final PaymentMethodDAO paymentMethodDAO;
    private final PaymentMethodTypeDAO paymentMethodTypeDAO;
    private final AccountDAO accountDAO;
    private final CategoryDAO categoryDAO;
    private final AccountProductDAO accountProductDAO;
    private final CartDAO cartDAO;

    public EntityValidator() {
        this.countryDAO = new CountryDAO();
        this.currencyDAO = new CurrencyDAO();
        this.languageDAO = new LanguageDAO();
        this.addressDAO = new AddressDAO();
        this.paymentMethodDAO = new PaymentMethodDAO();
        this.paymentMethodTypeDAO = new PaymentMethodTypeDAO();
        this.accountDAO = new AccountDAO();
        this.productDAO = new ProductDAO();
        this.accountProductDAO = new AccountProductDAO();
        this.categoryDAO = new CategoryDAO();
        this.cartDAO = new CartDAO();
    }

    public void validateProductNotInCart(Connection connection, String accountId, String productId)
            throws SQLException {
        boolean isInCart = cartDAO.findById(connection, accountId, productId).isPresent();
        if (isInCart) {
            logger.error("Account: {} already has Product: {} in cart", accountId, productId);
            throw new DuplicateEntityException("Product is already in the cart");
        }
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

    public ProductBean validateActiveProduct(Connection conn, String productId)
            throws EntityNotFoundException, InactiveEntityException {
        ProductBean product = productDAO.findById(conn, productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found",
                        EntityNotFoundException.EntityType.PRODUCT));

        if (!product.isActive()) {
            logger.error("Product is not active for ID: {}", productId);
            throw new InactiveEntityException("Product with id " + productId + " is not active",
                    InactiveEntityException.EntityType.PRODUCT);
        }
        return product;
    }

    public ProductBean validateProduct(Connection connection, String productId) throws SQLException {
        ProductBean product = productDAO.findById(connection, productId).orElseThrow(() -> {
            logger.error("Product not found for ID: {}", productId);
            return new EntityNotFoundException("Product not found for ID: " + productId,
                    EntityNotFoundException.EntityType.PRODUCT);
        });

        if (!product.isActive()) {
            logger.error("Product is not active for ID: {}", productId);
            throw new InactiveEntityException("Product with id " + productId + " is not active",
                    InactiveEntityException.EntityType.PRODUCT);
        }

        if (product.getStockQuantity() != null && product.getStockQuantity() <= 0) {
            logger.error("Product is out of stock for ID: {}", productId);
            throw new InsufficientStockException("Product with id " + productId + " is out of stock");
        }
        return product;
    }

    public ProductBean rawValidateProduct(Connection connection, String productId) throws SQLException {
        ProductBean product = productDAO.findById(connection, productId).orElseThrow(() -> {
            logger.error("Product not found for ID: {}", productId);
            return new EntityNotFoundException("Product not found for ID: " + productId,
                    EntityNotFoundException.EntityType.PRODUCT);
        });
        return product;
    }

    public CategoryBean validateActiveCategory(Connection connection, String categoryId) throws SQLException {
        return categoryDAO.findById(connection, categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found for ID: " + categoryId
                        , EntityNotFoundException.EntityType.CATEGORY));
    }

    public void validateProductNotAlreadyOwned(Connection connection, String accountId, String productId)
            throws SQLException {
        boolean isOwned = accountProductDAO.findById(connection, accountId, productId).isPresent();
        if (isOwned) {
            logger.error("Account: {} already owns Product: {}", accountId, productId);
            throw new DuplicateEntityException("Account already owns this product");
        }
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

    public AddressBean validateActiveAddress(Connection connection, String addressId) throws SQLException {
        AddressBean address = addressDAO.findById(connection, addressId).orElseThrow(() -> {
            logger.error("Address not found for ID: {}", addressId);
            return new EntityNotFoundException("Address not found for ID: " + addressId,
                    EntityNotFoundException.EntityType.ADDRESS);
        });

        if (!address.isActive()) {
            logger.error("Address is not active for Address ID: {}", addressId);
            throw new InactiveEntityException("Address with id " + addressId + " is not active",
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

    public PaymentMethodTypeBean validateActivePaymentMethodType(Connection connection, int typeId) throws SQLException {
        PaymentMethodTypeBean type = paymentMethodTypeDAO.findById(connection, typeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment method type not found: " + typeId
                        , EntityNotFoundException.EntityType.PAYMENT_METHOD_TYPE
                ));

        if (!type.isActive()) {
            throw new InactiveEntityException(
                    "Payment method type is not active: " + typeId
                    , InactiveEntityException.EntityType.PAYMENT_METHOD_TYPE
            );
        }

        return type;
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
