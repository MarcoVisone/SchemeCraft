package com.xyra.schemecraft.dto;

import java.util.List;

import com.xyra.schemecraft.model.CategoryBean;
import com.xyra.schemecraft.model.ProductBean;
import com.xyra.schemecraft.model.ProductVersionBean;

/**
 * Data Transfer Object (DTO) aggregating complete product details for administrative operations,
 * including associated categories, media assets, and version histories.
 *
 * @param product    Base product domain model entity
 * @param categories List of category entities assigned to the product
 * @param imagePaths Ordered list of relative image paths associated with the product
 * @param versions   List of historical and active version entities for this product
 */
public record ProductFullDTO(

        ProductBean product,

        List<CategoryBean> categories,

        List<String> imagePaths,

        List<ProductVersionBean> versions
) {
    /**
     * Compact constructor performing parameter assertion and defensive copying for immutability.
     *
     * @throws IllegalArgumentException if the product is null
     */
    public ProductFullDTO {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        categories = (categories == null) ? List.of() : List.copyOf(categories);
        imagePaths = (imagePaths == null) ? List.of() : List.copyOf(imagePaths);
        versions = (versions == null) ? List.of() : List.copyOf(versions);
    }
}
