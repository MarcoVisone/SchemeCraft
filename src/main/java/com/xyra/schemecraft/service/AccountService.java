package com.xyra.schemecraft.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.*;

import org.mindrot.jbcrypt.BCrypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.constant.ValidationConstants;
import com.xyra.schemecraft.dao.AccountDAO;
import com.xyra.schemecraft.dao.AddressDAO;
import com.xyra.schemecraft.dao.PaymentMethodDAO;
import com.xyra.schemecraft.dto.*;
import com.xyra.schemecraft.exception.*;
import com.xyra.schemecraft.model.*;
import com.xyra.schemecraft.service.gateway.FakeTokenizationService;
import com.xyra.schemecraft.service.gateway.TokenizationResult;
import com.xyra.schemecraft.util.Utils;

public class AccountService {

    /** Logger instance for tracking service events and errors. */
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    /** Work factor cost parameter for BCrypt password hashing algorithm. */
    private static final int BCRYPT_WORKLOAD = 12;

    /** Precomputed dummy BCrypt hash used for timing attack mitigation on failed lookups. */
    private static final String DUMMY_HASH =  "$2a$12$R9h/cIPz0gi.URNNX3kh2OPST9/PgBkqquzi.Ss7KIUgO3t0Rejsq";

    /** Data Access Object for account persistence operations. */
    private final AccountDAO accountDAO;

    /** Data Access Object for address management. */
    private final AddressDAO addressDAO;

    /** Data Access Object for payment method transactions. */
    private final PaymentMethodDAO paymentMethodDAO;

    /** Validator utility for inspecting entity constraints. */
    private final EntityValidator entityValidator;

    /** Gateway simulator for handling mock payment tokenization. */
    private final FakeTokenizationService tokenizationGateway;

    /**
     * Default constructor initializing dependencies with default implementations.
     */
    public AccountService(){
        this.accountDAO = new AccountDAO();
        this.addressDAO = new AddressDAO();
        this.paymentMethodDAO = new PaymentMethodDAO();
        this.entityValidator = new EntityValidator();
        this.tokenizationGateway = new FakeTokenizationService();
    }

    /**
     * Authenticates a user using either their username or email address along with their plain-text password.
     * Incorporates constant-time dummy password hashing checks to prevent timing side-channel attacks.
     *
     * @param usernameOrEmail The user's account username or registered email address
     * @param password        The plain-text authentication password
     * @return A newly established {@link UserSession} containing the authenticated account details
     * @throws BadCredentialsException if credentials are missing, account is inactive, or password verification fails
     * @throws ServiceException        if a database or infrastructure error occurs during authentication
     */
    public UserSession login(String usernameOrEmail, String password) throws BadCredentialsException {
        if (Utils.isNullOrBlank(usernameOrEmail) || Utils.isNullOrBlank(password)) {
            throw new BadCredentialsException("Invalid credentials.");
        }

        String normalizedInput = usernameOrEmail.trim().toLowerCase();

        try (Connection conn = ConnectionPool.getConnection()) {
            AccountBean account = null;
            boolean isEmail = Utils.looksLikeEmail(normalizedInput);

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
            if (Utils.isNullOrBlank(storedHash)) {
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

    /**
     * Registers a new user account on the platform, performing comprehensive validation on user inputs,
     * verifying related reference entities (country, currency, language), hashing the password securely with BCrypt,
     * and persisting the new record.
     *
     * @param request The {@link AccountRegistrationRequest} containing account registration data
     * @return An {@link AccountRegistrationResponse} carrying the newly created account summary details
     * @throws IllegalArgumentException   if the request payload is null or any field fails validation constraints
     * @throws DuplicateEntityException   if the username or email is already registered in the system
     * @throws EntityNotFoundException    if referenced entities like country, currency, or language do not exist
     * @throws ServiceException           if a database or infrastructure error occurs during execution
     */
    public AccountRegistrationResponse registerAccount(AccountRegistrationRequest request)
            throws DuplicateEntityException, EntityNotFoundException, ServiceException {
        if (request == null) {
            throw new IllegalArgumentException("Registration request cannot be null");
        }

        String username = request.username() != null ? request.username().trim() : "";
        if (Utils.isNullOrBlank(username) || !ValidationConstants.USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(String.format(
                    "Username must be between %d and %d characters (only letters, numbers, and underscores allowed)",
                    ValidationConstants.USERNAME_MIN_LENGTH,
                    ValidationConstants.USERNAME_MAX_LENGTH
            ));
        }

        String email = request.email() != null ? request.email().trim() : "";
        if (Utils.isNullOrBlank(email) || !ValidationConstants.EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format provided");
        }

        String rawPassword = request.plainTextPassword();
        if (Utils.isNullOrBlank(rawPassword)) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (!ValidationConstants.PASSWORD_PATTERN.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException(String.format(
                    "Password must be %d-%d characters long and contain at least one uppercase letter, " +
                            "one lowercase letter, and one digit",
                    ValidationConstants.PASSWORD_MIN_LENGTH,
                    ValidationConstants.PASSWORD_MAX_LENGTH
            ));
        }
        if (rawPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password exceeds maximum length of 72 bytes");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActiveCountry(conn, request.countryId());
            entityValidator.validateActiveCurrency(conn, request.currencyId());
            entityValidator.validateLanguage(conn, request.languageId());

            String passwordHash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_WORKLOAD));

            AccountBean account = new AccountBean();
            account.setAccountId(java.util.UUID.randomUUID().toString());
            account.setUsername(username);
            account.setEmail(email.toLowerCase());
            account.setPasswordHash(passwordHash);
            account.setCountryId(request.countryId());
            account.setLanguageId(request.languageId());
            account.setCurrencyId(request.currencyId());
            account.setBio(request.bio());
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

    /**
     * Changes the authentication password for an existing active user account.
     * Validates the old password via BCrypt verification, enforces length constraints,
     * hashes the new password securely, and updates the database record.
     *
     * @param accountId   Unique identifier of the target account changing the password
     * @param oldPassword The current plain-text password for verification
     * @param newPassword The desired new plain-text password
     * @throws EntityNotFoundException if the account does not exist or is inactive
     * @throws BadCredentialsException if the old password verification fails or inputs are blank
     * @throws ServiceException        if new password exceeds length limits, database operations fail, or update fails unexpectedly
     */
    public void changePassword(String accountId, String oldPassword, String newPassword)
            throws EntityNotFoundException, ServiceException {
        if (Utils.isNullOrBlank(accountId)) {
            throw new ServiceException("Invalid account ID.");
        }
        if (Utils.isNullOrBlank(oldPassword) || Utils.isNullOrBlank(newPassword)) {
            throw new BadCredentialsException("Invalid credentials.");
        }
        if (newPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new ServiceException("New password exceeds maximum length of 72 bytes.");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            AccountBean account = entityValidator.validateActiveAccount(conn, accountId);

            String storedHash = account.getPasswordHash();
            if (Utils.isNullOrBlank(storedHash)) {
                throw new ServiceException("Account has no password hash.");
            }

            if (!BCrypt.checkpw(oldPassword, storedHash)) {
                throw new BadCredentialsException("Invalid credentials.");
            }

            String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(BCRYPT_WORKLOAD));

            boolean updated = accountDAO.updatePassword(conn, accountId, newHash);
            if (!updated) {
                logger.error("Password update failed unexpectedly for account {}", accountId);
                throw new ServiceException("Failed to update password.");
            }
            logger.info("Password changed for account {}", accountId);
        } catch (SQLException | DAOException e) {
            logger.error("Database error during password change", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    /**
     * Changes the display username for an existing active user account.
     * Validates input parameters, checks account active status, updates the record in the database,
     * and handles persistence or unique constraint exceptions appropriately.
     *
     * @param accountId   Unique identifier of the target account changing its username
     * @param newUsername The desired new username string
     * @throws IllegalArgumentException if the account ID or new username is null or blank
     * @throws EntityNotFoundException  if the target account does not exist or is inactive
     * @throws DuplicateEntityException if the new username is already taken by another account
     * @throws ServiceException         if a database error occurs or the username update fails unexpectedly
     */
    public void changeUsername(String accountId, String newUsername) throws EntityNotFoundException {
        if (Utils.isNullOrBlank(accountId)) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }
        if (Utils.isNullOrBlank(newUsername)) {
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

    /**
     * Checks whether a specific username is already registered in the system.
     *
     * @param username The username string to verify
     * @return true if an account associated with the username exists, false otherwise
     * @throws ServiceException if the username is null or blank, or if a database or infrastructure error occurs
     */
    public boolean checkUsernameExists(String username) {
        if (Utils.isNullOrBlank(username)) {
            throw new ServiceException("Invalid username.");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            return accountDAO.findByUsername(conn, username).isPresent();
        } catch (SQLException | DAOException e) {
            logger.error("Database error while checking username existence", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    /**
     * Changes the registered email address for an existing active user account.
     * Validates input parameters, verifies account active status, updates the record in the database,
     * and handles persistence or unique constraint exceptions appropriately.
     *
     * @param accountId Unique identifier of the target account changing its email
     * @param newEmail  The desired new email address string
     * @throws IllegalArgumentException if the account ID or new email is null or blank
     * @throws EntityNotFoundException  if the target account does not exist or is inactive
     * @throws DuplicateEntityException if the new email address is already taken by another account
     * @throws ServiceException         if a database error occurs or the email update fails unexpectedly
     */
    public void changeEmail(String accountId, String newEmail) throws EntityNotFoundException {
        if (Utils.isNullOrBlank(accountId)) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }
        if (Utils.isNullOrBlank(newEmail)) {
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

    /**
     * Checks whether a specific email address is already registered in the system.
     *
     * @param email The email address string to verify
     * @return true if an account associated with the email exists, false otherwise
     * @throws ServiceException if the email is null or blank, or if a database or infrastructure error occurs
     */
    public boolean checkEmailExists(String email) {
        if (Utils.isNullOrBlank(email)) {
            throw new ServiceException("Invalid email.");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            return accountDAO.findByEmail(conn, email).isPresent();
        } catch (SQLException | DAOException e) {
            logger.error("Database error while checking email existence", e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    /**
     * Retrieves an account entity by its unique identifier.
     * Clears sensitive data such as the password hash before returning the account object.
     *
     * @param accountId Unique identifier of the target account
     * @return The retrieved {@link AccountBean} instance with its password hash redacted
     * @throws ServiceException        if the account ID is null or blank, or if a database error occurs
     * @throws EntityNotFoundException if no account exists with the specified ID
     */
    public AccountBean getAccountById(String accountId) throws EntityNotFoundException {
        if (Utils.isNullOrBlank(accountId)) {
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

    /**
     * Updates an existing user's profile details using the provided {@link ProfileUpdateRequest} payload.
     * Validates that the account is active and that any referenced relational entities (country, currency, language)
     * are valid and active before executing the update.
     *
     * @param updatedAccount The {@link ProfileUpdateRequest} containing the profile fields to modify
     * @throws IllegalArgumentException if the request payload is null
     * @throws ServiceException         if the account ID is invalid, no fields are provided, or database errors occur
     * @throws EntityNotFoundException  if the target account or referenced lookup entities do not exist
     */
    public void updateProfile(ProfileUpdateRequest updatedAccount) throws EntityNotFoundException, ServiceException {
        if (updatedAccount == null) {
            throw new IllegalArgumentException("ProfileUpdateRequest cannot be null");
        }

        if (Utils.isNullOrBlank(updatedAccount.accountId())) {
            throw new ServiceException("Invalid account ID.");
        }

        if (updatedAccount.countryId() == null && updatedAccount.currencyId() == null &&
                updatedAccount.languageId() == null && updatedAccount.bio() == null &&
                updatedAccount.profileImagePath() == null) {
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

    /**
     * Deactivates an existing user account by its unique identifier.
     * Updates the account status in the database to inactive.
     *
     * @param accountId Unique identifier of the target account to deactivate
     * @throws IllegalArgumentException or {@link ServiceException} if the account ID is null or blank
     * @throws EntityNotFoundException  if no account exists with the specified ID
     * @throws ServiceException         if a database error occurs or account deactivation fails unexpectedly
     */
    public void deactivateAccount(String accountId) throws EntityNotFoundException {
        if (Utils.isNullOrBlank(accountId)) {
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

    /**
     * Reactivates an existing suspended or deactivated user account by its unique identifier.
     * Updates the account status in the database back to active.
     *
     * @param accountId Unique identifier of the target account to reactivate
     * @throws IllegalArgumentException if the account ID is null or blank
     * @throws EntityNotFoundException  if no account exists with the specified ID
     * @throws ServiceException         if a database error occurs or account reactivation fails unexpectedly
     */
    public void reactivateAccount(String accountId) throws EntityNotFoundException {
        if (Utils.isNullOrBlank(accountId)) {
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

    /**
     * Retrieves a list of all saved delivery or billing addresses associated with a specific user account.
     *
     * @param accountId Unique identifier of the target account
     * @return A {@link List} of {@link AddressBean} objects belonging to the account
     * @throws IllegalArgumentException if the account ID is null or blank
     * @throws ServiceException         if a database or infrastructure error occurs during retrieval
     */
    public List<AddressBean> listAddresses(String accountId) {
        if (Utils.isNullOrBlank(accountId)) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            return addressDAO.findAllByAccountId(conn, accountId);
        } catch (SQLException | DAOException e) {
            logger.error("Database connection error while listing addresses for account {}", accountId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    /**
     * Adds a new address entry for a specific user account within a transactional block.
     * Validates account and country status, assigns a unique identifier if missing, handles
     * default address precedence rules, and persists the record.
     *
     * @param address The {@link AddressBean} object containing address details to add
     * @throws IllegalArgumentException if the address object is null or lacks a required country ID
     * @throws EntityNotFoundException  if the target account or country does not exist or is inactive
     * @throws ServiceException         if a database or transaction error occurs during execution
     */
    public void addAddress(AddressBean address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }

        if (Utils.isNullOrBlank(address.getCountryId())) {
            throw new IllegalArgumentException("Country ID is required for an address");
        }

        if (Utils.isNullOrBlank(address.getAddressId())) {
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

                if (isFirstAddress || address.isDefault()) {
                    addressDAO.findDefaultByAccountId(conn, accountId)
                            .ifPresent(current -> addressDAO.unsetDefault(conn, current.getAddressId()));
                    address.setDefault(true);
                }

                address.setActive(true);

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

    public void removeOwnAddress(String accountId, String addressId) throws EntityNotFoundException {
        if (Utils.isNullOrBlank(accountId)) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }
        if (Utils.isNullOrBlank(addressId)) {
            throw new IllegalArgumentException("Address ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            conn.setAutoCommit(false);

            try {
                AddressBean address = entityValidator.validateActiveAddress(conn, addressId);

                if (!address.getAccountId().equals(accountId)) {
                    logger.warn("Account {} attempted to remove address {} owned by a different account",
                            accountId, addressId);
                    throw new UnauthorizedActionException("Address does not belong to the specified account");
                }

                boolean wasDefault = address.isDefault();

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
                logger.info("Address {} removed successfully by owner {} (default promotion: {})",
                        addressId, accountId, wasDefault);
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
            logger.error("Database connection error while removing address {} for account {}", addressId, accountId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }

    public void removeAddress(String addressId) throws EntityNotFoundException {
        if (Utils.isNullOrBlank(addressId)) {
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
        if (Utils.isNullOrBlank(accountId) || Utils.isNullOrBlank(addressId)) {
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
        if (Utils.isNullOrBlank(accountId)) {
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

        try (Connection conn = ConnectionPool.getConnection()) {
            entityValidator.validateActivePaymentMethodType(conn, request.methodType());
        } catch (SQLException e) {
            logger.error("Database connection error while validating payment method type for account {}",
                    request.accountId(), e);
            throw new ServiceException("Internal database error occurred", e);
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
            } catch (SQLException | DAOException e) {
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

    public void removeOwnPaymentMethod(String accountId, String paymentMethodId) throws EntityNotFoundException {
        if (Utils.isNullOrBlank(accountId)) {
            throw new IllegalArgumentException("Account ID cannot be null or blank");
        }
        if (Utils.isNullOrBlank(paymentMethodId)) {
            throw new IllegalArgumentException("Payment Method ID cannot be null or blank");
        }

        try (Connection conn = ConnectionPool.getConnection()) {
            conn.setAutoCommit(false);

            try {
                PaymentMethodBean method = paymentMethodDAO.findById(conn, paymentMethodId).orElseThrow(() -> {
                    logger.error("Payment method not found for ID: {}", paymentMethodId);
                    return new EntityNotFoundException("Payment method not found for ID: " + paymentMethodId);
                });

                // Ownership check happens within the same transaction as the removal itself,
                // so there is no gap between verifying ownership and performing to delete.
                if (!method.getAccountId().equals(accountId)) {
                    logger.warn("Account {} attempted to remove payment method {} owned by a different account",
                            accountId, paymentMethodId);
                    throw new UnauthorizedActionException("Payment method does not belong to the specified account");
                }

                boolean wasDefault = method.isDefault();

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
                logger.info("Payment method {} removed successfully by owner {} (default promotion: {})",
                        paymentMethodId, accountId, wasDefault);
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
            logger.error("Database connection error while removing payment method {} for account {}",
                    paymentMethodId, accountId, e);
            throw new ServiceException("Internal database error occurred", e);
        }
    }


    public void removePaymentMethod(String paymentMethodId) throws EntityNotFoundException {
        if (Utils.isNullOrBlank(paymentMethodId)) {
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
            } catch (SQLException | DAOException e) {
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
        if (Utils.isNullOrBlank(accountId) || Utils.isNullOrBlank(paymentMethodId)) {
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
            } catch (SQLException | DAOException e) {
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
