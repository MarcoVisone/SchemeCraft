package com.xyra.schemecraft.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ProductVersionBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String versionId;
    private String productId;
    private String changelog;
    private LocalDateTime createdAt;
    private int downloadCount;
    private String filePath;
    private String minecraftVersion;
    private String version;

    public ProductVersionBean() {
    }

    public ProductVersionBean(String versionId, String productId, String changelog, String filePath,
                              String minecraftVersion, String version) {
        this.versionId = versionId;
        this.productId = productId;
        this.changelog = changelog;
        this.filePath = filePath;
        this.minecraftVersion = minecraftVersion;
        this.version = version;
    }

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
