package com.example.recipe_android_project.features.auth.domain.model;


import java.io.Serializable;

public class ProfileData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fullName;
    private String email;

    public ProfileData() {
    }

    public ProfileData(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }


    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }



    // Setters
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
