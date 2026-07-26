package com.xyra.schemecraft.dao;

import com.xyra.schemecraft.exception.DAOException;
import com.xyra.schemecraft.model.RememberTokenBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RememberTokenDAO extends BaseDAO {

    private static final String INSERT_SQL =
            "INSERT INTO remember_token (token_id, account_id, token_hash) VALUES (?, ?, ?)";

    private static final String FIND_BY_HASH_SQL =
            "SELECT token_id, account_id, token_hash, created_at FROM remember_token WHERE token_hash = ?";

    private static final String FIND_ALL_BY_ACCOUNT_SQL =
            "SELECT token_id, account_id, token_hash, created_at FROM remember_token WHERE account_id = ?";

    private static final String DELETE_BY_HASH_SQL =
            "DELETE FROM remember_token WHERE token_hash = ?";

    private static final String DELETE_ALL_BY_ACCOUNT_SQL =
            "DELETE FROM remember_token WHERE account_id = ?";

    public void insert(Connection conn, RememberTokenBean token) throws DAOException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            ps.setString(1, token.getTokenId());
            ps.setString(2, token.getAccountId());
            ps.setString(3, token.getTokenHash());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to insert remember token for account: {}", token.getAccountId(), e);
            throw new DAOException("Failed to insert remember token", e);
        }
    }

    public Optional<RememberTokenBean> findByTokenHash(Connection conn, String tokenHash) throws DAOException {
        try (PreparedStatement ps = conn.prepareStatement(FIND_BY_HASH_SQL)) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.error("Failed to find remember token by hash", e);
            throw new DAOException("Failed to find remember token by hash", e);
        }
    }

    public List<RememberTokenBean> findAllByAccountId(Connection conn, String accountId) throws DAOException {
        List<RememberTokenBean> tokens = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(FIND_ALL_BY_ACCOUNT_SQL)) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tokens.add(mapRow(rs));
                }
            }
            return tokens;
        } catch (SQLException e) {
            logger.error("Failed to find remember tokens for account: {}", accountId, e);
            throw new DAOException("Failed to find remember tokens by account id", e);
        }
    }

    public boolean deleteByTokenHash(Connection conn, String tokenHash) throws DAOException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_BY_HASH_SQL)) {
            ps.setString(1, tokenHash);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete remember token by hash", e);
            throw new DAOException("Failed to delete remember token by hash", e);
        }
    }

    public boolean deleteAllByAccountId(Connection conn, String accountId) throws DAOException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_ALL_BY_ACCOUNT_SQL)) {
            ps.setString(1, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete remember tokens for account: {}", accountId, e);
            throw new DAOException("Failed to delete remember tokens by account id", e);
        }
    }

    private RememberTokenBean mapRow(ResultSet rs) throws SQLException {
        RememberTokenBean token = new RememberTokenBean();
        token.setTokenId(rs.getString("token_id"));
        token.setAccountId(rs.getString("account_id"));
        token.setTokenHash(rs.getString("token_hash"));
        token.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return token;
    }
}