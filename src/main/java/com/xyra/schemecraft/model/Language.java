package com.xyra.schemecraft.model;

import java.io.Serializable;

public class Language implements Serializable {
    private static final long serialVersionUID = 1L;

    private String languageId;
    private String languageName;

    public Language() {
    }

    public Language(String languageId, String languageName) {
        this.languageId = languageId;
        this.languageName = languageName;
    }

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

    @Override
    public String toString() {
        return "Language{" +
                "languageId='" + languageId + '\'' +
                ", languageName='" + languageName + '\'' +
                '}';
    }
}
