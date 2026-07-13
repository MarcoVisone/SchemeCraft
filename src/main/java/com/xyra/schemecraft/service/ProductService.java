package com.xyra.schemecraft.service;

import com.xyra.schemecraft.model.ProductBean;
import com.xyra.schemecraft.model.ProductImageBean;
import com.xyra.schemecraft.model.ProductVersionBean;
import com.xyra.schemecraft.dao.ProductSearchCriteria;
import com.xyra.schemecraft.exception.EntityNotFoundException;

import java.util.List;

public class ProductService {

    public ProductBean getProductById(String productId) throws EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: getProductById");
    }

    public List<ProductBean> searchProducts(ProductSearchCriteria criteria) {
        throw new UnsupportedOperationException("TODO: searchProducts");
    }

    public ProductBean createProduct(ProductBean product, String accountId) {
        throw new UnsupportedOperationException("TODO: createProduct");
    }

    public void updateProduct(ProductBean product) throws EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: updateProduct");
    }

    public void deactivateProduct(String productId) throws EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: deactivateProduct");
    }

    public List<ProductImageBean> listImages(String productId) {
        throw new UnsupportedOperationException("TODO: listImages");
    }

    public ProductImageBean addImage(String productId, String imagePath) {
        throw new UnsupportedOperationException("TODO: addImage");
    }

    public void removeImage(String imageId) throws EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: removeImage");
    }

    public ProductVersionBean getVersionById(String versionId) throws EntityNotFoundException {
        throw new UnsupportedOperationException("TODO: getVersionById");
    }

    public List<ProductVersionBean> listVersions(String productId) {
        throw new UnsupportedOperationException("TODO: listVersions");
    }

    public ProductVersionBean publishVersion(ProductVersionBean version) {
        throw new UnsupportedOperationException("TODO: publishVersion");
    }

    public void registerDownload(String versionId) {
        throw new UnsupportedOperationException("TODO: registerDownload");
    }

    public void assignCategory(String productId, String categoryId) {
        throw new UnsupportedOperationException("TODO: assignCategory");
    }

    public void removeCategory(String productId, String categoryId) {
        throw new UnsupportedOperationException("TODO: removeCategory");
    }

    public boolean ownsProduct(String accountId, String productId) {
        throw new UnsupportedOperationException("TODO: ownsProduct");
    }

    public List<ProductBean> listOwnedProducts(String accountId) {
        throw new UnsupportedOperationException("TODO: listOwnedProducts");
    }
}