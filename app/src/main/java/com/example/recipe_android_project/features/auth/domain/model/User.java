package com.example.recipe_android_project.features.auth.domain.model;

import java.io.Serializable;

public class User implements Serializable {


    private String id;
    private String fullName;
    private String email;
    private String password;
    private boolean isLoggedIn;

    private boolean pendingSync;
    private String pendingSyncAction;
    private long lastSyncedAt;

    private boolean pendingPasswordSync;

    private boolean pendingRegistrationSync;

    private long createdAt;
    private long updatedAt;


    public User() {
        this.pendingSync = false;
        this.pendingPasswordSync = false;
        this.pendingRegistrationSync = false;
        this.lastSyncedAt = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public User(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.pendingSync = false;
        this.pendingPasswordSync = false;
        this.pendingRegistrationSync = false;
        this.lastSyncedAt = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }


    public boolean isValid() {
        return isValidFullName() && isValidEmail() && isValidPassword();
    }

    public boolean isValidFullName() {
        return fullName != null && fullName.trim().length() >= 2;
    }

    public boolean isValidEmail() {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    public boolean isValidPassword() {
        return password != null && password.length() >= 6;
    }


    public String getFirstName() {
        if (fullName == null || fullName.isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    public String getLastName() {
        if (fullName == null || fullName.isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length > 1) {
            return parts[parts.length - 1];
        }
        return "";
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isPendingSync() {
        return pendingSync;
    }

    public void setPendingSync(boolean pendingSync) {
        this.pendingSync = pendingSync;
    }

    public String getPendingSyncAction() {
        return pendingSyncAction;
    }

    public void setPendingSyncAction(String pendingSyncAction) {
        this.pendingSyncAction = pendingSyncAction;
    }

    public long getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(long lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public boolean isPendingPasswordSync() {
        return pendingPasswordSync;
    }

    public void setPendingPasswordSync(boolean pendingPasswordSync) {
        this.pendingPasswordSync = pendingPasswordSync;
    }

    public boolean isPendingRegistrationSync() {
        return pendingRegistrationSync;
    }

    public void setPendingRegistrationSync(boolean pendingRegistrationSync) {
        this.pendingRegistrationSync = pendingRegistrationSync;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        if (id != null) {
            return id.equals(user.id);
        }
        return email != null && email.equals(user.email);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return email != null ? email.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", isLoggedIn=" + isLoggedIn +
                ", pendingSync=" + pendingSync +
                ", pendingPasswordSync=" + pendingPasswordSync +
                ", pendingRegistrationSync=" + pendingRegistrationSync +
                '}';
    }
}
