package com.xyra.schemecraft.dto;

/**
 * DTO for updating or creating a single version within
 * a complete product update request.
 * <p>
 * - If {@code versionId} is null, a new version is created.
 * - If {@code versionId} is present, the existing version is updated.
 * - Existing versions not included in the list will be deleted.
 *
 * @param versionId        ID of the existing version (null for new version)
 * @param version          Version string (e.g., "1.0.0")
 * @param minecraftVersion Compatible Minecraft version (e.g., "1.20.4")
 * @param filePath         Path to the schematic file
 * @param changelog        Release notes
 */
public record VersionUpdateRequest(
        String versionId,
        String version,
        String minecraftVersion,
        String filePath,
        String changelog
) {
    /**
     * Compact constructor performing parameter sanitization and validation.
     * Note: versionId is intentionally NOT validated here because it can be null
     * for new versions. The service will handle the distinction.
     *
     * @throws IllegalArgumentException if version or filePath are null/empty
     */
    public VersionUpdateRequest {
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Version string cannot be null or empty");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        // Sanitize optional fields
        versionId = versionId != null ? versionId.trim() : null;
        version = version.trim();
        minecraftVersion = minecraftVersion != null ? minecraftVersion.trim() : null;
        filePath = filePath.trim();
        changelog = changelog != null ? changelog.trim() : null;
    }
}