package com.xyra.schemecraft.dto;

import com.xyra.schemecraft.model.ProductBean;

import java.io.Serial;
import java.io.Serializable;

/**
 * Data Transfer Object che rappresenta un prodotto nella lista
 * dei prodotti posseduti da un account.
 */
public class OwnedProductItem implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private ProductBean product;

    private String ownerAccountId;

    private boolean purchased;

    private boolean owned;

    public OwnedProductItem() {
    }

    public OwnedProductItem(ProductBean product, String ownerAccountId, boolean purchased, boolean owned) {
        this.product = product;
        this.ownerAccountId = ownerAccountId;
        this.purchased = purchased;
        this.owned = owned;
    }

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
        this.ownerAccountId = ownerAccountId;
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

    @Override
    public String toString() {
        return "OwnedProductItem{" +
                "product=" + product +
                ", ownerAccountId='" + ownerAccountId + '\'' +
                ", purchased=" + purchased +
                ", owned=" + owned +
                '}';
    }
}