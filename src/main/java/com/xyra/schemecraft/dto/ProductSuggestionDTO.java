package com.xyra.schemecraft.dto;

/**
 * Data Transfer Object (DTO) carrying a single search-bar autocomplete suggestion.
 * Populated internally by the service layer from a lightweight product lookup, decoupled
 * from the full {@link com.xyra.schemecraft.model.ProductBean}.
 *
 * @param productId      Unique identifier of the suggested product
 * @param productName    Display name of the product
 * @param description    Short description, already truncated for preview purposes
 * @param coverImagePath Relative path to the product's first gallery image, or null if none is set
 */
public record ProductSuggestionDTO(

        String productId,

        String productName,

        String description,

        String coverImagePath
) {
    /**
     * Compact constructor performing defensive trimming of textual fields.
     */
    public ProductSuggestionDTO {
        productId = productId != null ? productId.trim() : null;
        productName = productName != null ? productName.trim() : null;
        description = description != null ? description.trim() : null;
        coverImagePath = coverImagePath != null ? coverImagePath.trim() : null;
    }
}
