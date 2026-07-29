package com.xyra.schemecraft.dto;

import java.io.Serial;
import java.io.Serializable;

import com.xyra.schemecraft.model.ProductBean;

/**
 * Data Transfer Object (DTO) representing a product in relation to a specific user account,
 * encapsulating ownership status, acquisition flags, and visual metadata.
 */
public class OwnedProductItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Product entity associated with this item. */
    private ProductBean product;

    /** Unique identifier of the target user account evaluating ownership. */
    private String ownerAccountId;

    /** Flag indicating whether the product was explicitly purchased via order transaction. */
    private boolean purchased;

    /** Flag indicating whether the user currently possesses or holds entitlement to the product. */
    private boolean owned;

    /** Relative path or URL to the product's cover display image. */
    private String coverImagePath;

    /**
     * Default no-argument constructor.
     */
    public OwnedProductItem() {
    }

    /**
     * Constructs a fully-initialized OwnedProductItem.
     *
     * @param product        Product domain model entity
     * @param ownerAccountId Account identifier of the potential owner
     * @param purchased      Transaction acquisition flag
     * @param owned          General ownership or entitlement flag
     * @param coverImagePath Relative path or URL to the cover image
     */
    public OwnedProductItem(ProductBean product, String ownerAccountId, boolean purchased, boolean owned, String coverImagePath) {
        this.product = product;
        this.ownerAccountId = ownerAccountId;
        this.purchased = purchased;
        this.owned = owned;
        this.coverImagePath = coverImagePath;
    }

    // --- Getters and Setters ---

    public ProductBean getProduct() {
        return product;
    }

    public void setProduct(ProductBean product) {
        this.product = product;
    }

    public String getOwnerAccountId() {
        return ownerAccountId;
    }

    public void setOwnerAccountId(String ownerAccountId) {
        this.ownerAccountId = (ownerAccountId != null) ? ownerAccountId.trim() : null;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    public boolean isOwned() {
        return owned;
    }

    public void setOwned(boolean owned) {
        this.owned = owned;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = (coverImagePath != null) ? coverImagePath.trim() : null;
    }

    // --- Standard Object Override Methods ---

    /**
     * Returns a string representation of the OwnedProductItem object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "OwnedProductItem{" +
                "product=" + product +
                ", ownerAccountId='" + ownerAccountId + '\'' +
                ", purchased=" + purchased +
                ", owned=" + owned +
                ", coverImagePath='" + coverImagePath + '\'' +
                '}';
    }
}
