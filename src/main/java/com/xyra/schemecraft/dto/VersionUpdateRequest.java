package com.xyra.schemecraft.dto;

/**
 * Data Transfer Object (DTO) for updating or creating a single version within
 * a complete product update request.
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
