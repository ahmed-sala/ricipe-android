package com.example.recipe_android_project.features.auth.domain.model;

public class PasswordChangeResult {

    private final boolean success;
    private final String message;
    private final boolean pendingSync;

    private PasswordChangeResult(boolean success, String message, boolean pendingSync) {
        this.success = success;
        this.message = message;
        this.pendingSync = pendingSync;
    }

    public static PasswordChangeResult success() {
        return new PasswordChangeResult(true, "Password changed successfully", false);
    }

    public static PasswordChangeResult successWithPendingSync() {
        return new PasswordChangeResult(true, "Password changed. Will sync when online.", true);
    }

    public static PasswordChangeResult failure(String message) {
        return new PasswordChangeResult(false, message, false);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public boolean isPendingSync() {
        return pendingSync;
    }
}
