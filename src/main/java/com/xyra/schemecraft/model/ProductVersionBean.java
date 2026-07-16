package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Represents the ProductVersion domain model and data transfer object within the application.
 */
public class ProductVersionBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier for this product version release. */
    @NotBlank(message = "Version ID cannot be blank")
    private String versionId;

    /** Reference identifier of the associated parent Product. */
    @NotBlank(message = "Product ID cannot be blank")
    private String productId;

    /** Textual description of features, bug fixes, or changes in this release. */
    private String changelog;

    /** Timestamp indicating exactly when this version was published. */
    private LocalDateTime createdAt;

    /** Total count of times this specific version has been downloaded. */
    @Min(value = 0, message = "Download count cannot be negative")
    private int downloadCount;

    /** Safe storage path pointing to the downloadable resource file. */
    @NotBlank(message = "File path cannot be blank")
    private String filePath;

    /** Target Minecraft version compatibility. */
    @NotBlank(message = "Minecraft version compatibility cannot be blank")
    @Size(max = 20, message = "Minecraft version string cannot exceed {max} characters")
    private String minecraftVersion;

    /** Semantic version name/number of this release. */
    @NotBlank(message = "Release version identifier cannot be blank")
    @Size(max = 20, message = "Version string cannot exceed {max} characters")
    private String version;

    /**
     * Default no-argument constructor.
     */
    public ProductVersionBean() {
    }

    /**
     * Constructs a ProductVersionBean with primary release fields.
     *
     * @param versionId        Unique version identifier
     * @param productId        Associated product identifier
     * @param changelog        Release changelog details
     * @param filePath         Storage path of the file
     * @param minecraftVersion Target game version compatibility
     * @param version          Semantic release version (e.g., "1.0.0")
     */
    public ProductVersionBean(String versionId, String productId, String changelog, String filePath,
                              String minecraftVersion, String version) {
        this.versionId = versionId;
        this.productId = productId;
        this.changelog = changelog;
        this.filePath = filePath;
        this.minecraftVersion = minecraftVersion;
        this.version = version;
        this.createdAt = LocalDateTime.now(); // Safe default fallback
    }

    /**
     * Constructs a fully-initialized ProductVersionBean.
     *
     * @param versionId        Unique version identifier
     * @param productId        Associated product identifier
     * @param createdAt        Publication timestamp
     * @param changelog        Release changelog details
     * @param downloadCount    Total starting downloads
     * @param filePath         Storage path of the file
     * @param minecraftVersion Target game version compatibility
     * @param version          Semantic release version (e.g., "1.0.0")
     */
    public ProductVersionBean(String versionId, String productId, LocalDateTime createdAt, String changelog,
                              int downloadCount, String filePath, String minecraftVersion, String version) {
        this.versionId = versionId;
        this.productId = productId;
        this.createdAt = createdAt;
        this.changelog = changelog;
        this.downloadCount = downloadCount;
        this.filePath = filePath;
        this.minecraftVersion = minecraftVersion;
        this.version = version;
    }

    // --- Getters and Setters ---

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    // --- Standard Object Override Methods ---

    /**
     * Compares this product version with another object for equality.
     * Equality is determined strictly by the unique versionId.
     *
     * @param o The reference object to compare
     * @return true if the objects share the same versionId; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductVersionBean that = (ProductVersionBean) o;
        return Objects.equals(versionId, that.versionId);
    }

    /**
     * Generates a hash code based on the unique versionId.
     *
     * @return A hash code value for this product version bean
     */
    @Override
    public int hashCode() {
        return Objects.hash(versionId);
    }

    /**
     * Returns a string representation of the ProductVersionBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "ProductVersionBean{" + // Fixed class name consistency
                "versionId='" + versionId + '\'' +
                ", productId='" + productId + '\'' +
                ", changelog='" + changelog + '\'' +
                ", createdAt=" + createdAt +
                ", downloadCount=" + downloadCount +
                ", filePath='" + filePath + '\'' +
                ", minecraftVersion='" + minecraftVersion + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
