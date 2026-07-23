package com.xyra.schemecraft.service;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.constant.ValidationConstants;
import com.xyra.schemecraft.dao.AddressDAO;
import com.xyra.schemecraft.dao.PaymentMethodDAO;
import com.xyra.schemecraft.exception.*;
import com.xyra.schemecraft.model.*;

import com.xyra.schemecraft.dao.AccountDAO;

import com.xyra.schemecraft.service.gateway.FakeTokenizationService;
import com.xyra.schemecraft.service.gateway.TokenizationResult;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private static final int BCRYPT_WORKLOAD = 12;
    private static final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = validatorFactory.getValidator();
    private static final String DUMMY_HASH =  "$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO3t0Rejsq";

    private final AccountDAO accountDAO;
    private final AddressDAO addressDAO;
    private final PaymentMethodDAO paymentMethodDAO;
    private final EntityValidator entityValidator;
    private final FakeTokenizationService tokenizationGateway;

    public AccountService(){
        this.accountDAO = new AccountDAO();
        this.addressDAO = new AddressDAO();
        this.paymentMethodDAO = new PaymentMethodDAO();
        this.tokenizationGateway = new FakeTokenizationService();
        this.entityValidator = new EntityValidator();
    }

    public static boolean looksLikeEmail(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }

        return input.matches(ValidationConstants.EMAIL_REGEXP);
    }

    public UserSession login(String usernameOrEmail, String password)
            throws BadCredentialsException {

        if (usernameOrEmail == null || usernameOrEmail.isBlank() || password == null || password.isEmpty()) {
            throw new BadCredentialsException("Invalid credentials.");
        }

        String normalizedInput = usernameOrEmail.trim().toLowerCase();

        try (Connection conn = ConnectionPool.getConnection()) {
            AccountBean account = null;
            boolean isEmail = looksLikeEmail(normalizedInput);

            if (isEmail) {
                account = accountDAO.findByEmail(conn, normalizedInput).orElse(null);
            } else {
                account = accountDAO.findByUsername(conn, normalizedInput).orElse(null);
            }

            if (account == null) {
                BCrypt.checkpw(password, DUMMY_HASH);
                throw new BadCredentialsException("Invalid credentials.");
            }
            String storedHash = account.getPasswordHash();
            if (storedHash == null || storedHash.isEmpty()) {
                BCrypt.checkpw(password, DUMMY_HASH);
                throw new BadCredentialsException("Invalid credentials.");
            }

            if (!account.isActive()) {
                BCrypt.checkpw(password, DUMMY_HASH);
                throw new BadCredentialsException("Invalid credentials.");
            }

            if (!BCrypt.checkpw(password, storedHash)) {
                throw new BadCredentialsException("Invalid credentials.");
            }

            account.setPasswordHash(null);

            UserSession session = new UserSession();
            session.setAccount(account);

            return session;

        } catch (SQLException | DAOException e) {
            logger.error("Database connection error during login attempt", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public AccountRegistrationResponse registerAccount(AccountRegistrationRequest request)
            throws DuplicateEntityException, EntityNotFoundException, InactiveEntityException, ServiceException {

        if (request == null) {
            throw new IllegalArgumentException("Registration request cannot be null");
        }
        if (request.plainTextPassword() == null || request.plainTextPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (request.plainTextPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password exceeds maximum length of 72 bytes");
        }
        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveCountry(conn, request.countryId());
            entityValidator.validateActiveCurrency(conn, request.currencyId());
            entityValidator.validateLanguage(conn, request.languageId());

            String passwordHash = BCrypt.hashpw(request.plainTextPassword(), BCrypt.gensalt(BCRYPT_WORKLOAD));

            AccountBean account = new AccountBean();
            account.setAccountId(java.util.UUID.randomUUID().toString());
            account.setUsername(request.username().trim());
            account.setEmail(request.email().trim().toLowerCase());
            account.setPasswordHash(passwordHash);
            account.setCountryId(request.countryId());
            account.setLanguageId(request.languageId());
            account.setCurrencyId(request.currencyId());
            account.setBio(request.bio());
            account.setBannerPath(request.bannerPath());
            account.setProfileImagePath(request.profileImagePath());
            account.applyDefaultsIfMissing();

            account.setAdmin(false);
            account.setActive(true);

            accountDAO.insert(conn, account);

            logger.info("Account registered successfully with ID: {}", account.getAccountId());

            return new AccountRegistrationResponse(
                    account.getAccountId(),
                    account.getUsername(),
                    account.getEmail(),
                    account.getCountryId(),
                    account.getLanguageId(),
                    account.getCurrencyId(),
                    account.getBio(),
                    account.getBannerPath(),
                    account.getProfileImagePath(),
                    account.isAdmin(),
                    account.isActive()
            );

        } catch (DuplicateEntityException | EntityNotFoundException | InactiveEntityException e) {
            throw e;
        } catch (SQLException | DAOException e) {
            logger.error("Database error during registration for username: {}", request.username(), e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public void changePassword(String accountId, String oldPassword, String newPassword)
            throws BadCredentialsException, EntityNotFoundException, ServiceException {

        if (accountId == null || accountId.isBlank()) {
            throw new ServiceException("Invalid account ID.");
        }
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new BadCredentialsException("Invalid credentials.");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            throw new BadCredentialsException("Invalid credentials.");
        }
        if (newPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new ServiceException("New password exceeds maximum length of 72 bytes.");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            AccountBean account = entityValidator.validateActiveAccount(conn, accountId);

            String storedHash = account.getPasswordHash();
            if(storedHash == null || storedHash.isEmpty()) {
                throw new ServiceException("Account has no password hash.");
            }

            if(!BCrypt.checkpw(oldPassword, storedHash)) {
                throw new BadCredentialsException("Invalid credentials.");
            }

            String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(BCRYPT_WORKLOAD));

            boolean updated = accountDAO.updatePassword(conn, accountId, newHash);
            if(!updated) {
                logger.error("Password update failed unexpectedly for account {}", accountId);
                throw new ServiceException("Failed to update password.");
            }
            logger.info("Password changed for account {}", accountId);
        } catch (SQLException | DAOException e) {
            logger.error("Database error during password change", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public boolean checkEmailExists(String email) {
        if (email == null || email.isBlank()) {
            throw new ServiceException("Invalid email.");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            return accountDAO.findByEmail(conn, email).isPresent();
        } catch (SQLException | DAOException e) {
            logger.error("Database error while checking email existence", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public boolean checkUsernameExists(String username) {
        if(username == null || username.isBlank()) {
            throw new ServiceException("Invalid username.");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            return accountDAO.findByUsername(conn, username).isPresent();
        } catch (SQLException | DAOException e) {
            logger.error("Database error while checking username existence", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public void changeUsername(String accountId, String newUsername) throws EntityNotFoundException {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }
        if (newUsername == null || newUsername.isBlank()) {
            throw new IllegalArgumentException("New username cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveAccount(conn, accountId);

            boolean updated = accountDAO.updateUsername(conn, accountId, newUsername.trim());
            if (updated) {
                logger.info("Username changed successfully for account: {}", accountId);
            } else {
                logger.error("Username update failed for account {}", accountId);
                throw new ServiceException("Failed to update username.");
            }
        } catch (DuplicateEntityException | EntityNotFoundException e) {
            throw e;
        } catch (SQLException | DAOException e) {
            logger.error("Database connection error while changing username for account {}", accountId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public void changeEmail(String accountId, String newEmail) throws EntityNotFoundException {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }
        if (newEmail == null || newEmail.isBlank()) {
            throw new IllegalArgumentException("New email cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveAccount(conn, accountId);

            boolean updated = accountDAO.updateEmail(conn, accountId, newEmail.trim().toLowerCase());
            if (updated) {
                logger.info("Email changed successfully for account: {}", accountId);
            } else {
                logger.error("Email update failed for account {}", accountId);
                throw new ServiceException("Failed to update email.");
            }
        } catch (DuplicateEntityException | EntityNotFoundException e) {
            throw e;
        } catch (SQLException | DAOException e) {
            logger.error("Database connection error while changing email for account {}", accountId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }


    public AccountBean getAccountById(String accountId) throws EntityNotFoundException {
        if (accountId == null || accountId.isBlank()) {
            throw new ServiceException("Invalid account ID.");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            AccountBean account = accountDAO.findById(conn, accountId)
                    .orElseThrow(() -> new EntityNotFoundException("Account not found.",
                            EntityNotFoundException.EntityType.ACCOUNT));
            account.setPasswordHash(null);
            return account;
        } catch (SQLException | DAOException e) {
            logger.error("Database error while retrieving account by ID", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public void updateProfile(ProfileUpdateRequest updatedAccount) throws EntityNotFoundException, ServiceException {
        if (updatedAccount == null) {
            throw new IllegalArgumentException("ProfileUpdateRequest cannot be null");
        }

        if (updatedAccount.accountId() == null || updatedAccount.accountId().isBlank()) {
            throw new ServiceException("Invalid account ID.");
        }

        if (updatedAccount.countryId() == null && updatedAccount.currencyId() == null &&
                updatedAccount.languageId() == null && updatedAccount.bannerPath() == null &&
                updatedAccount.bio() == null && updatedAccount.profileImagePath() == null) {
            throw new ServiceException("At least one profile field must be provided for update.");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveAccount(conn, updatedAccount.accountId());

            if (updatedAccount.countryId() != null) {
                entityValidator.validateActiveCountry(conn, updatedAccount.countryId());
            }
            if (updatedAccount.currencyId() != null) {
                entityValidator.validateActiveCurrency(conn, updatedAccount.currencyId());
            }
            if (updatedAccount.languageId() != null) {
                entityValidator.validateLanguage(conn, updatedAccount.languageId());
            }

            boolean updated = accountDAO.softUpdate(conn, updatedAccount);
            if (updated) {
                logger.info("Profile updated successfully for account: {}", updatedAccount.accountId());
            } else {
                logger.error("Profile update failed for account {}", updatedAccount.accountId());
                throw new ServiceException("Failed to update profile.");
            }
        } catch (SQLException | DAOException e) {
            logger.error("Database connection error while updating profile for account {}",
                    updatedAccount.accountId(), e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public void deactivateAccount(String accountId) throws EntityNotFoundException {
        if (accountId == null || accountId.isBlank()) {
            throw new ServiceException("Invalid account ID.");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            if (accountDAO.deactivate(conn, accountId)) {
                logger.info("Account deactivated successfully for account: {}", accountId);
            } else {
                logger.error("Failed to deactivate account for account {}", accountId);
                throw new ServiceException("Failed to deactivate account.");
            }
        } catch (SQLException | DAOException e) {
            logger.error("Database connection error while deactivating account {}", accountId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public void reactivateAccount(String accountId) throws EntityNotFoundException {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            if (accountDAO.activate(conn, accountId)) {
                logger.info("Account reactivated successfully for account: {}", accountId);
            } else {
                logger.error("Failed to reactivate account for account {}", accountId);
                throw new ServiceException("Failed to reactivate account.");
            }
        } catch (SQLException | DAOException e) {
            logger.error("Database connection error while reactivating account {}", accountId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public List<AddressBean> listAddresses(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new ServiceException("Invalid account ID.");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            return addressDAO.findAllByAccountId(conn, accountId);
        } catch (SQLException | DAOException e) {
            logger.error("Database connection error while listing addresses for account {}", accountId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public void addAddress(AddressBean address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }

        if (address.getCountryId() == null || address.getCountryId().isBlank()) {
            throw new IllegalArgumentException("Country ID is required for an address");
        }

        if (address.getAddressId() == null || address.getAddressId().isBlank()) {
            address.setAddressId(UUID.randomUUID().toString());
        }

        String accountId = address.getAccountId();

        try (Connection conn = ConnectionPool.getConnection()) {
            conn.setAutoCommit(false);
            boolean success = false;

            try {
                entityValidator.validateActiveAccount(conn, accountId);
                entityValidator.validateActiveCountry(conn, address.getCountryId());

                boolean isFirstAddress = addressDAO.findAllActiveByAccountId(conn, accountId).isEmpty();
                if (isFirstAddress) {
                    address.setDefault(true);
                } else if (address.isDefault()) {
                    addressDAO.findDefaultByAccountId(conn, accountId)
                            .ifPresent(current -> addressDAO.unsetDefault(conn, current.getAddressId()));
                }

                addressDAO.insert(conn, address);
                conn.commit();
                success = true;

                logger.info("Address {} added successfully for account: {}", address.getAddressId(), accountId);

            } finally {
                if (!success) {
                    try {
                        conn.rollback();
                    } catch (SQLException rollbackEx) {
                        logger.error("Rollback failed while adding address for account {}", accountId, rollbackEx);
                    }
                }
                conn.setAutoCommit(true);
            }
        } catch (SQLException | DAOException e) {
            logger.error("Database connection error while adding address for account {}", accountId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public void removeAddress(String addressId) throws EntityNotFoundException {
        if (addressId == null || addressId.isBlank()) {
            throw new IllegalArgumentException("Address ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            conn.setAutoCommit(false);

            try {
                AddressBean address = addressDAO.findById(conn, addressId).orElseThrow(() -> {
                    logger.error("Address not found for ID: {}", addressId);
                    return new EntityNotFoundException("Address not found for ID: " + addressId);
                });

                boolean wasDefault = address.isDefault();
                String accountId = address.getAccountId();

                addressDAO.deactivate(conn, addressId);

                if (wasDefault) {
                    addressDAO.unsetDefault(conn, addressId);

                    Optional<AddressBean> candidate = addressDAO.findAnyActiveByAccountIdExcluding(conn, accountId,
                            addressId);
                    if (candidate.isPresent()) {
                        AddressBean newDefault = candidate.get();
                        newDefault.setDefault(true);
                        addressDAO.update(conn, newDefault);
                    }
                }

                conn.commit();
                logger.info("Address {} removed successfully (default promotion: {})", addressId, wasDefault);
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Rollback failed", rollbackEx);
                    e.addSuppressed(rollbackEx);
                }
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException | DAOException e) {
            logger.error("Database connection error while removing address {}", addressId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public void setDefaultAddress(String accountId, String addressId) throws EntityNotFoundException {
        if (accountId == null || accountId.isBlank() || addressId == null || addressId.isBlank()) {
            throw new IllegalArgumentException("Account ID and Address ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            conn.setAutoCommit(false);

            try {
                entityValidator.validateActiveAccount(conn, accountId);
                AddressBean address = addressDAO.findById(conn, addressId).orElseThrow(() -> {
                    logger.error("Address not found for ID: {}", addressId);
                    return new EntityNotFoundException("Address not found for ID: " + addressId);
                });

                if (!address.getAccountId().equals(accountId)) {
                    throw new ServiceException("Address does not belong to the specified account");
                }

                Optional<AddressBean> currentDefault = addressDAO.findDefaultByAccountId(conn, accountId);
                currentDefault.ifPresent(addr -> {
                    addressDAO.unsetDefault(conn, addr.getAddressId());
                });

                address.setDefault(true);
                addressDAO.update(conn, address);

                conn.commit();
                logger.info("Address {} set as default for account {}", addressId, accountId);
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Rollback failed", rollbackEx);
                    e.addSuppressed(rollbackEx);
                }
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException | DAOException e) {
            logger.error("Database connection error while setting default address {} for account {}", addressId, accountId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public List<PaymentMethodBean> listPaymentMethods(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            return paymentMethodDAO.findAllByAccountId(conn, accountId);
        } catch (SQLException | DAOException e) {
            logger.error("Database connection error while listing payment methods for account {}", accountId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public void addPaymentMethod(PaymentMethodRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("PaymentMethodRequest cannot be null");
        }

        TokenizationResult tokenizationResult;
        if (request.cardNumber() != null) {
            tokenizationResult = tokenizationGateway.tokenizeCC(
                    request.cardNumber(), request.cardExpiration(), request.cvv());
        } else if (request.paypalEmail() != null) {
            tokenizationResult = tokenizationGateway.tokenizePP(request.paypalEmail());
        } else {
            throw new IllegalArgumentException("Request must contain either card data or a PayPal email");
        }

        if (!tokenizationResult.success()) {
            logger.warn("Tokenization failed for account {}: {}", request.accountId(), tokenizationResult.errorCode());
            throw new PaymentTokenizationException("Payment data could not be tokenized: " +
                    tokenizationResult.errorCode(), tokenizationResult.errorCode());
        }

        PaymentMethodBean method = new PaymentMethodBean();
        method.setPaymentMethodId(UUID.randomUUID().toString());
        method.setMethodType(request.methodType());
        method.setPaymentToken(tokenizationResult.token());
        method.setDefault(request.isDefault());

        if (request.cardNumber() != null) {
            method.setCardBrand(tokenizationResult.brand());
            method.setCardExpiration(tokenizationResult.expiration());
            method.setCardLastFour(tokenizationResult.lastFour());
        } else {
            method.setPaymentEmail(tokenizationResult.email());
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            conn.setAutoCommit(false);

            try {
                entityValidator.validateActiveAccount(conn, request.accountId());
                method.setAccountId(request.accountId());

                boolean isFirstMethod = paymentMethodDAO.findAllByAccountId(conn, request.accountId()).isEmpty();
                if (isFirstMethod) {
                    method.setDefault(true);
                } else if (method.isDefault()) {
                    Optional<PaymentMethodBean> currentDefault =
                            paymentMethodDAO.findDefaultByAccountId(conn, request.accountId());
                    currentDefault
                            .ifPresent(pm -> paymentMethodDAO
                            .unsetDefault(conn, pm.getPaymentMethodId()));
                }

                paymentMethodDAO.insert(conn, method);
                conn.commit();

                logger.info("Payment method {} added successfully for account: {}",
                        method.getPaymentMethodId(), request.accountId());
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Rollback failed", rollbackEx);
                    e.addSuppressed(rollbackEx);
                }
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException | DAOException e) {
            logger.error("Database connection error while adding payment method for account {}",
                    request.accountId(), e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public void removePaymentMethod(String paymentMethodId) throws EntityNotFoundException {
        if (paymentMethodId == null || paymentMethodId.isBlank()) {
            throw new IllegalArgumentException("Payment Method ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            conn.setAutoCommit(false);

            try {
                PaymentMethodBean method = paymentMethodDAO.findById(conn, paymentMethodId).orElseThrow(() -> {
                    logger.error("Payment method not found for ID: {}", paymentMethodId);
                    return new EntityNotFoundException("Payment method not found for ID: " + paymentMethodId);
                });

                boolean wasDefault = method.isDefault();
                String accountId = method.getAccountId();

                paymentMethodDAO.forceDelete(conn, paymentMethodId);

                if (wasDefault) {
                    Optional<PaymentMethodBean> candidate =
                            paymentMethodDAO.findAnyByAccountIdExcluding(conn, accountId, paymentMethodId);
                    if (candidate.isPresent()) {
                        PaymentMethodBean newDefault = candidate.get();
                        newDefault.setDefault(true);
                        paymentMethodDAO.update(conn, newDefault);
                    }
                }

                conn.commit();
                logger.info("Payment method {} removed successfully (default promotion: {})",
                        paymentMethodId, wasDefault);
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Rollback failed", rollbackEx);
                    e.addSuppressed(rollbackEx);
                }
                throw e;
            }finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException | DAOException e) {
            logger.error("Database connection error while removing payment method {}", paymentMethodId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public void setDefaultPaymentMethod(String accountId, String paymentMethodId) throws EntityNotFoundException {
        if (accountId == null || accountId.isBlank() || paymentMethodId == null || paymentMethodId.isBlank()) {
            throw new IllegalArgumentException("Account ID and Payment Method ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            conn.setAutoCommit(false);

            try {
                entityValidator.validateActiveAccount(conn, accountId);
                PaymentMethodBean method = paymentMethodDAO.findById(conn, paymentMethodId).orElseThrow(() -> {
                    logger.error("Payment method not found for ID: {}", paymentMethodId);
                    return new EntityNotFoundException("Payment method not found for ID: " + paymentMethodId);
                });

                if (!method.getAccountId().equals(accountId)) {
                    throw new ServiceException("Payment method does not belong to the specified account");
                }

                Optional<PaymentMethodBean> currentDefault = paymentMethodDAO.findDefaultByAccountId(conn, accountId);
                currentDefault.ifPresent(pm -> paymentMethodDAO.unsetDefault(conn, pm.getPaymentMethodId()));

                method.setDefault(true);
                paymentMethodDAO.update(conn, method);

                conn.commit();
                logger.info("Payment method {} set as default for account {}", paymentMethodId, accountId);
            } catch (Exception e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Rollback failed", rollbackEx);
                    e.addSuppressed(rollbackEx);
                }
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException | DAOException e) {
            logger.error("Database connection error while setting default payment method {} for account {}",
                    paymentMethodId, accountId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }
}