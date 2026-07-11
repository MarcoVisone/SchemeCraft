package com.xyra.schemecraft.model;

import java.io.Serializable;

public class CategoryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String categoryId;
    private String categoryName;
    private String parentCategoryName;
    private String description;

    public CategoryBean() {
    }

    public CategoryBean(String categoryId, String categoryName, String parentCategoryName, String description) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.parentCategoryName = parentCategoryName;
        this.description = description;
    }

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

    public String getParentCategoryName() {
        return parentCategoryName;
    }

    public void setParentCategoryName(String parentCategoryName) {
        this.parentCategoryName = parentCategoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId='" + categoryId + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", parentCategoryName='" + parentCategoryName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
