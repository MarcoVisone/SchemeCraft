package com.xyra.schemecraft.dto;

/**
 * Data Transfer Object (DTO) carrying the payload required to create or update a category.
 *
 * @param categoryId       Unique identifier of the target category to update; null when creating a new category
 * @param categoryName     Display name of the category
 * @param parentCategoryId Identifier of the parent category; null or blank if this is a root category
 * @param description      Optional textual description of the category
 */
public record CategoryRequest(

        String categoryId,

        String categoryName,

        String parentCategoryId,

        String description
) {
    /**
     * Compact constructor performing parameter sanitization and mandatory field validation.
     *
     * @throws IllegalArgumentException if categoryName is null or blank
     */
    public CategoryRequest {
        categoryId = categoryId != null ? categoryId.trim() : null;

        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        categoryName = categoryName.trim();

        parentCategoryId = (parentCategoryId != null && !parentCategoryId.trim().isEmpty())
                ? parentCategoryId.trim()
                : null;

        description = description != null ? description.trim() : null;
    }
}
