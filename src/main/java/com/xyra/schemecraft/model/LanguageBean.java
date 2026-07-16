package com.xyra.schemecraft.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Represents the Language domain model and data transfer object within the application.
 */
public class LanguageBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Unique identifier for the language. */
    @NotBlank(message = "Language ID cannot be blank")
    private String languageId;

    /** Official display name of the language. */
    @NotBlank(message = "Language name cannot be blank")
    @Size(max = 100, message = "Language name cannot exceed {max} characters")
    private String languageName;

    /**
     * Default no-argument constructor.
     */
    public LanguageBean() {
    }

    /**
     * Constructs a fully-initialized LanguageBean.
     *
     * @param languageId   Unique language identifier
     * @param languageName Display name of the language
     */
    public LanguageBean(String languageId, String languageName) {
        this.languageId = languageId;
        this.languageName = languageName;
    }

    // --- Getters and Setters ---

    public String getLanguageId() {
        return languageId;
    }

    public void setLanguageId(String languageId) {
        this.languageId = languageId;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    // --- Standard Object Override Methods ---

    /**
     * Compares this language with another object for equality.
     * Equality is determined strictly by the unique, case-insensitive languageId.
     *
     * @param o The reference object to compare
     * @return true if the objects share the same languageId; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LanguageBean that = (LanguageBean) o;
        return Objects.equals(
                languageId != null ? languageId.toLowerCase() : null,
                that.languageId != null ? that.languageId.toLowerCase() : null
        );
    }

    /**
     * Generates a hash code based on the unique languageId.
     *
     * @return A hash code value for this language bean
     */
    @Override
    public int hashCode() {
        return Objects.hash(languageId != null ? languageId.toLowerCase() : null);
    }

    /**
     * Returns a string representation of the LanguageBean object.
     *
     * @return Formatted string representation of this instance
     */
    @Override
    public String toString() {
        return "LanguageBean{" + // Fixed class name consistency
                "languageId='" + languageId + '\'' +
                ", languageName='" + languageName + '\'' +
                '}';
    }
}
