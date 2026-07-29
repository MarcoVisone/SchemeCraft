package com.xyra.schemecraft.dto;

import java.time.LocalDateTime;

/**
 * Read-only view of an account for administrative listing purposes.
 * Deliberately excludes sensitive fields (e.g. passwordHash) that must never leave the server.
 *
 * @param accountId Unique identifier of the account
 * @param username  Account's username
 * @param email     Account's email address
 * @param createdAt Timestamp of account creation
 * @param isActive  Whether the account is currently active
 * @param isAdmin   Whether the account has admin privileges
 */
public record AccountAdminView(
        String accountId,
        String username,
        String email,
        LocalDateTime createdAt,
        boolean isActive,
        boolean isAdmin
) {
}
