package com.example.recipe_android_project.core.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordHasher {

    private static final int BCRYPT_COST = 12;


    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        return BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
    }


    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
        return result.verified;
    }


    public static boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null) {
            return true;
        }

        try {
            int currentCost = extractCostFactor(hashedPassword);
            return currentCost < BCRYPT_COST;
        } catch (Exception e) {
            return true;
        }
    }


    private static int extractCostFactor(String hashedPassword) {
        String[] parts = hashedPassword.split("\\$");
        if (parts.length >= 3) {
            return Integer.parseInt(parts[2]);
        }
        return 0;
    }

    public static PasswordValidationResult validatePasswordStrength(String password) {
        PasswordValidationResult result = new PasswordValidationResult();

        if (password == null || password.isEmpty()) {
            result.setValid(false);
            result.setMessage("Password cannot be empty");
            return result;
        }

        if (password.length() < 8) {
            result.setValid(false);
            result.setMessage("Password must be at least 8 characters long");
            return result;
        }

        if (password.length() > 72) {
            result.setValid(false);
            result.setMessage("Password cannot exceed 72 characters");
            return result;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUppercase = true;
            else if (Character.isLowerCase(c)) hasLowercase = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        int strengthScore = 0;
        if (hasUppercase) strengthScore++;
        if (hasLowercase) strengthScore++;
        if (hasDigit) strengthScore++;
        if (hasSpecial) strengthScore++;
        if (password.length() >= 12) strengthScore++;

        result.setStrengthScore(strengthScore);

        if (strengthScore <= 2) {
            result.setStrengthLevel(PasswordStrength.WEAK);
        } else if (strengthScore <= 3) {
            result.setStrengthLevel(PasswordStrength.MEDIUM);
        } else {
            result.setStrengthLevel(PasswordStrength.STRONG);
        }

        if (!hasUppercase || !hasLowercase || !hasDigit) {
            result.setValid(false);
            result.setMessage("Password must contain at least one uppercase letter, one lowercase letter, and one digit");
            return result;
        }

        result.setValid(true);
        result.setMessage("Password is valid");
        return result;
    }


    public enum PasswordStrength {
        WEAK,
        MEDIUM,
        STRONG
    }

    public static class PasswordValidationResult {
        private boolean isValid;
        private String message;
        private int strengthScore;
        private PasswordStrength strengthLevel;

        public boolean isValid() {
            return isValid;
        }

        public void setValid(boolean valid) {
            isValid = valid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getStrengthScore() {
            return strengthScore;
        }

        public void setStrengthScore(int strengthScore) {
            this.strengthScore = strengthScore;
        }

        public PasswordStrength getStrengthLevel() {
            return strengthLevel;
        }

        public void setStrengthLevel(PasswordStrength strengthLevel) {
            this.strengthLevel = strengthLevel;
        }
    }
}
