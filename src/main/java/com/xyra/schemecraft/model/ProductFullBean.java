package com.xyra.schemecraft.model;

import com.xyra.schemecraft.model.CategoryBean;
import com.xyra.schemecraft.model.ProductBean;
import com.xyra.schemecraft.model.ProductVersionBean;

import java.util.List;

/**
 * DTO aggregating complete product data for admin operations.
 * Used as response payload for GET /admin/products/get.
 *
 * @param product    Base product data
 * @param categories List of categories assigned to the product
 * @param imagePaths List of image paths in display order
 * @param versions   List of all versions for this product
 */
public record ProductFullBean(
        ProductBean product,
        List<CategoryBean> categories,
        List<String> imagePaths,
        List<ProductVersionBean> versions
) {
    public ProductFullBean {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (categories == null) {
            categories = List.of();
        } else {
            categories = List.copyOf(categories);
        }
        if (imagePaths == null) {
            imagePaths = List.of();
        } else {
            imagePaths = List.copyOf(imagePaths);
        }
        if (versions == null) {
            versions = List.of();
        } else {
            versions = List.copyOf(versions);
        }
    }
}