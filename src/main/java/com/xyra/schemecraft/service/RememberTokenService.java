package com.xyra.schemecraft.service;

import com.xyra.schemecraft.connection.ConnectionPool;
import com.xyra.schemecraft.dao.RememberTokenDAO;
import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.exception.InvalidTokenException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.model.AccountBean;
import com.xyra.schemecraft.model.RememberTokenBean;
import com.xyra.schemecraft.model.UserSession;
import com.xyra.schemecraft.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages persistent "remember me" login tokens, allowing a user to stay
 * logged in across browser restarts without re-entering credentials.
 */
public class RememberTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RememberTokenService.class);

    private final RememberTokenDAO rememberTokenDAO;
    private final EntityValidator entityValidator;

    public RememberTokenService() {
        this.rememberTokenDAO = new RememberTokenDAO();
        this.entityValidator = new EntityValidator();
    }

    /**
     * Creates a new persistent remember-me token for the given account.
     * The raw token is returned so the caller (servlet) can store it in a cookie;
     * only its hash is persisted in the database.
     *
     * @param accountId the account to create the token for
     * @return the raw token value to be stored client-side
     */
    public String createRememberToken(String accountId) throws ServiceException {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = Utils.hashToken(rawToken);

        RememberTokenBean token = new RememberTokenBean();
        token.setTokenId(UUID.randomUUID().toString());
        token.setAccountId(accountId);
        token.setTokenHash(tokenHash);

        // Single INSERT: no transaction/rollback needed
        try (Connection conn = ConnectionPool.getConnection()) {
            rememberTokenDAO.insert(conn, token);
        } catch (SQLException | DAOException e) {
            logger.error("Failed to create remember token for account: {}", accountId, e);
            throw new ServiceException("Failed to create remember token", e);
        }

        return rawToken;
    }

    /**
     * Validates a raw remember-me token (typically read from a cookie) and,
     * if valid, rebuilds a UserSession for the associated account.
     * Rejects tokens belonging to inactive accounts.
     *
     * @param rawToken the raw token value read from the client
     * @return a UserSession for the account associated with the token
     * @throws InvalidTokenException if the token is missing, malformed, unknown, or the account is inactive
     */
    public UserSession validateRememberToken(String rawToken) throws InvalidTokenException, ServiceException {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidTokenException("Remember token is missing or blank");
        }

        String tokenHash = Utils.hashToken(rawToken);

        try (Connection conn = ConnectionPool.getConnection()) {
            Optional<RememberTokenBean> tokenOpt = rememberTokenDAO.findByTokenHash(conn, tokenHash);
            if (tokenOpt.isEmpty()) {
                throw new InvalidTokenException("Remember token not recognized");
            }

            String accountId = tokenOpt.get().getAccountId();
            AccountBean account = entityValidator.validateActiveAccount(conn, accountId);

            UserSession session = new UserSession();
            session.setAccount(account);
            return session;

        } catch (SQLException | DAOException e) {
            logger.error("Failed to validate remember token", e);
            throw new ServiceException("Failed to validate remember token", e);
        }
    }

    /**
     * Invalidates a single remember-me token, e.g. on logout from one device.
     *
     * @param rawToken the raw token value to invalidate
     */
    public void invalidateRememberToken(String rawToken) throws ServiceException {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        String tokenHash = Utils.hashToken(rawToken);

        try (Connection conn = ConnectionPool.getConnection()) {
            rememberTokenDAO.deleteByTokenHash(conn, tokenHash);
        } catch (SQLException | DAOException e) {
            logger.error("Failed to invalidate remember token", e);
            throw new ServiceException("Failed to invalidate remember token", e);
        }
    }

    /**
     * Invalidates all remember-me tokens for an account, e.g. "log out from all devices"
     * or as a security measure after a password change.
     *
     * @param accountId the account whose tokens should all be invalidated
     */
    public void invalidateAllTokensForAccount(String accountId) throws ServiceException {
        try (Connection conn = ConnectionPool.getConnection()) {
            rememberTokenDAO.deleteAllByAccountId(conn, accountId);
        } catch (SQLException | DAOException e) {
            logger.error("Failed to invalidate all remember tokens for account: {}", accountId, e);
            throw new ServiceException("Failed to invalidate all remember tokens", e);
        }
    }
}