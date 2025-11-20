package com.warehouse.util;

import java.util.regex.Pattern;

/**
 * Utility class for input validation
 */
public class InputValidator {
    // Username: 3-20 characters, alphanumeric and underscore only
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    
    // Minimum password requirements: at least 6 characters
    private static final int MIN_PASSWORD_LENGTH = 6;
    
    // Maximum field lengths
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_REFERENCE_LENGTH = 50;
    public static final int MAX_NOTES_LENGTH = 500;
    public static final int MAX_CONTACT_LENGTH = 100;
    
    /**
     * Validates username format
     * @param username The username to validate
     * @return Validation result with message
     */
    public static ValidationResult validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.error("Le nom d'utilisateur est obligatoire.");
        }
        
        String trimmed = username.trim();
        
        if (trimmed.length() < 3) {
            return ValidationResult.error("Le nom d'utilisateur doit contenir au moins 3 caractères.");
        }
        
        if (trimmed.length() > MAX_USERNAME_LENGTH) {
            return ValidationResult.error("Le nom d'utilisateur ne peut pas dépasser " + MAX_USERNAME_LENGTH + " caractères.");
        }
        
        if (!USERNAME_PATTERN.matcher(trimmed).matches()) {
            return ValidationResult.error("Le nom d'utilisateur ne peut contenir que des lettres, chiffres et underscores.");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validates password strength
     * @param password The password to validate
     * @return Validation result with message
     */
    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return ValidationResult.error("Le mot de passe est obligatoire.");
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return ValidationResult.error("Le mot de passe doit contenir au moins " + MIN_PASSWORD_LENGTH + " caractères.");
        }
        
        // Check for at least one letter and one number (optional enhancement)
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        
        if (!hasLetter || !hasDigit) {
            return ValidationResult.warning("Pour plus de sécurité, utilisez un mot de passe contenant des lettres et des chiffres.");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validates field length
     * @param value The value to validate
     * @param fieldName The name of the field (for error message)
     * @param maxLength Maximum allowed length
     * @return Validation result
     */
    public static ValidationResult validateLength(String value, String fieldName, int maxLength) {
        if (value == null) {
            return ValidationResult.success(); // Null is allowed for optional fields
        }
        
        if (value.length() > maxLength) {
            return ValidationResult.error(fieldName + " ne peut pas dépasser " + maxLength + " caractères.");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validates that a value is not empty
     * @param value The value to validate
     * @param fieldName The name of the field (for error message)
     * @return Validation result
     */
    public static ValidationResult validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.error(fieldName + " est obligatoire.");
        }
        return ValidationResult.success();
    }
    
    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final boolean isWarning;
        
        private ValidationResult(boolean valid, String message, boolean isWarning) {
            this.valid = valid;
            this.message = message;
            this.isWarning = isWarning;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null, false);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message, false);
        }
        
        public static ValidationResult warning(String message) {
            return new ValidationResult(true, message, true);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public boolean isWarning() {
            return isWarning;
        }
    }
}

