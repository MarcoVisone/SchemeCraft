package com.xyra.schemecraft.dto;

/**
 * Data Transfer Object (DTO) carrying the payload required to create or register a new product version release.
 *
 * @param productId        Unique identifier of the associated product. May be null/empty when this request
 *                          is built before the parent product exists yet (e.g. as part of a ProductFullRequest);
 *                          in that case the consuming service is responsible for resolving and validating it.
 * @param changelog        Detailed summary of changes, fixes, and features included in this release
 * @param filePath         Storage file path or key referencing the binary artifact
 * @param minecraftVersion Target Minecraft game version compatibility string
 * @param version          Semantic release version number
 */
public record ProductVersionRequest(
        String productId,
        String changelog,
        String filePath,
        String minecraftVersion,
        String version
) {
    /**
     * Compact constructor performing parameter sanitization and validation.
     * Note: productId is intentionally NOT validated here, since some callers (e.g. full product
     * creation flows) build this request before the parent product exists. Any service consuming
     * this DTO for a standalone operation (e.g. publishVersion) must validate productId itself.
     *
     * @throws IllegalArgumentException if version or filePath are null or empty
     */
    public ProductVersionRequest {
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Version string cannot be null or empty");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        productId = productId != null ? productId.trim() : null;
        changelog = changelog != null ? changelog.trim() : null;
        filePath = filePath.trim();
        minecraftVersion = minecraftVersion != null ? minecraftVersion.trim() : null;
        version = version.trim();
    }
}
