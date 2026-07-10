package com.xyra.schemecraft.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ProductVersion implements Serializable {
    private static final long serialVersionUID = 1L;

    /*
    CREATE TABLE IF NOT EXISTS product_version (
    version_id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    changelog TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    download_count INT DEFAULT 0,
    file_path VARCHAR(255) NOT NULL,
    minecraft_version VARCHAR(20) NOT NULL,
    version VARCHAR(20) NOT NULL,
    CONSTRAINT fk_product_version_product FOREIGN KEY (product_id) REFERENCES product(product_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
     */

    private String versionId;
    private String productId;
    private String changelog;
    private LocalDateTime createdAt;
    private int downloadCount;
    private String filePath;
    private String minecraftVersion;
    private String version;

    public ProductVersion() {
    }

    public ProductVersion(String versionId, String productId, LocalDateTime createdAt, String changelog,
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

    @Override
    public String toString() {
        return "ProductVersion{" +
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
