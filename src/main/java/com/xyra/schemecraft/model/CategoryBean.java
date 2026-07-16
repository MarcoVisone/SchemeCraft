package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Represents the Category domain model and data transfer object within the application.
 */
public class CategoryBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier for the category. */
    @NotBlank(message = "Category ID cannot be blank")
    private String categoryId;

    /** Display name of the category. */
    @NotBlank(message = "Category name cannot be blank")
    @Size(min = 2, max = 100, message = "Category name must be between {min} and {max} characters")
    private String categoryName;

    /** ID of the parent category, if applicable. */
    private String parentCategoryId;

    /** Detailed description explaining what products fall under this category. */
    private String description;

    /**
     * Default no-argument constructor.
     */
    public CategoryBean() {
    }

    /**
     * Constructs a fully-initialized CategoryBean.
     *
     * @param categoryId         Unique identifier of the category
     * @param categoryName       Display name of the category
     * @param parentCategoryId   ID of the parent category (optional)
     * @param description        Description of the category
     */
    public CategoryBean(String categoryId, String categoryName, String parentCategoryId, String description) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.parentCategoryId = parentCategoryId;
        this.description = description;
    }

    // --- Getters and Setters ---

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(String parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // --- Standard Object Override Methods ---

    /**
     * Compares this category with another object for equality.
     * Equality is determined strictly by the unique categoryId.
     *
     * @param o The reference object to compare
     * @return true if the objects share the same categoryId; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryBean that = (CategoryBean) o;
        return Objects.equals(categoryId, that.categoryId);
    }

    /**
     * Generates a hash code based on the unique categoryId.
     *
     * @return A hash code value for this category bean
     */
    @Override
    public int hashCode() {
        return Objects.hash(categoryId);
    }

    /**
     * Returns a string representation of the CategoryBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "CategoryBean{" + // Fixed class name consistency
                "categoryId='" + categoryId + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", parentCategoryId='" + parentCategoryId + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
