package com.warehouse.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Properties;
import java.util.Set;

/**
 * Manages application license/activation key
 * Prevents unauthorized use of the application
 */
public class LicenseManager {
    private static final Logger logger = LoggerFactory.getLogger(LicenseManager.class);
    private static final String LICENSE_FILE = "license.dat";
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".matelaspro";
    private static final String LICENSE_PATH = CONFIG_DIR + File.separator + LICENSE_FILE;
    
    // Master license key (change this to your secret key)
    // Format: XXXX-XXXX-XXXX-XXXX-XXXX (5 groups of 4 alphanumeric chars)
    private static final String MASTER_KEY = "STE-HABIBA-MATELAS-PRO-2024";
    
    /**
     * Validates a license key format and checksum
     * @param licenseKey The license key to validate (format: XXXX-XXXX-XXXX-XXXX-XXXX)
     * @return true if valid, false otherwise
     */
    public static boolean validateLicenseKey(String licenseKey) {
        if (licenseKey == null || licenseKey.trim().isEmpty()) {
            return false;
        }
        
        // Remove spaces and convert to uppercase
        licenseKey = licenseKey.replaceAll("\\s+", "").toUpperCase();
        
        // Check format: 5 groups of 4 alphanumeric characters separated by hyphens
        if (!licenseKey.matches("^[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$")) {
            logger.warn("Invalid license key format: {}", licenseKey);
            return false;
        }
        
        // Validate checksum
        try {
            String keyWithoutDashes = licenseKey.replace("-", "");
            String checksumPart = keyWithoutDashes.substring(keyWithoutDashes.length() - 4);
            String keyPart = keyWithoutDashes.substring(0, keyWithoutDashes.length() - 4);
            
            // Calculate expected checksum
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String combined = keyPart + MASTER_KEY;
            byte[] hash = md.digest(combined.getBytes(StandardCharsets.UTF_8));
            
            // Get last 4 characters of hash as checksum
            String hashHex = bytesToHex(hash);
            String expectedChecksum = hashHex.substring(hashHex.length() - 4).toUpperCase();
            
            boolean isValid = checksumPart.equals(expectedChecksum);
            if (!isValid) {
                logger.warn("License key checksum validation failed. Expected: {}, Got: {}", expectedChecksum, checksumPart);
            }
            return isValid;
        } catch (Exception e) {
            logger.error("Error validating license key: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Saves a validated license key to disk (encrypted)
     * @param licenseKey The validated license key
     * @return true if saved successfully
     */
    public static boolean saveLicense(String licenseKey) {
        if (!validateLicenseKey(licenseKey)) {
            logger.error("Cannot save invalid license key");
            return false;
        }
        
        try {
            // Ensure config directory exists
            File configDir = new File(CONFIG_DIR);
            if (!configDir.exists()) {
                configDir.mkdirs();
                // Set permissions to owner-only (Unix-like systems)
                if (!System.getProperty("os.name").toLowerCase().startsWith("win")) {
                    try {
                        Path dirPath = Paths.get(CONFIG_DIR);
                        Set<PosixFilePermission> perms = Files.getPosixFilePermissions(dirPath);
                        perms.remove(PosixFilePermission.OTHERS_READ);
                        perms.remove(PosixFilePermission.OTHERS_WRITE);
                        perms.remove(PosixFilePermission.OTHERS_EXECUTE);
                        perms.remove(PosixFilePermission.GROUP_READ);
                        perms.remove(PosixFilePermission.GROUP_WRITE);
                        perms.remove(PosixFilePermission.GROUP_EXECUTE);
                        Files.setPosixFilePermissions(dirPath, perms);
                    } catch (Exception e) {
                        logger.debug("Could not set directory permissions: {}", e.getMessage());
                    }
                }
            }
            
            // Encrypt and save license
            String encrypted = encrypt(licenseKey);
            Properties props = new Properties();
            props.setProperty("license", encrypted);
            props.setProperty("timestamp", String.valueOf(System.currentTimeMillis()));
            
            try (FileOutputStream fos = new FileOutputStream(LICENSE_PATH)) {
                props.store(fos, "MatelasPro License - DO NOT MODIFY");
            }
            
            // Set file permissions to owner-only (Unix-like systems)
            if (!System.getProperty("os.name").toLowerCase().startsWith("win")) {
                try {
                    Path filePath = Paths.get(LICENSE_PATH);
                    Set<PosixFilePermission> perms = Files.getPosixFilePermissions(filePath);
                    perms.remove(PosixFilePermission.OTHERS_READ);
                    perms.remove(PosixFilePermission.OTHERS_WRITE);
                    perms.remove(PosixFilePermission.OTHERS_EXECUTE);
                    perms.remove(PosixFilePermission.GROUP_READ);
                    perms.remove(PosixFilePermission.GROUP_WRITE);
                    perms.remove(PosixFilePermission.GROUP_EXECUTE);
                    Files.setPosixFilePermissions(filePath, perms);
                } catch (Exception e) {
                    logger.debug("Could not set file permissions: {}", e.getMessage());
                }
            }
            
            logger.info("License key saved successfully");
            return true;
        } catch (Exception e) {
            logger.error("Failed to save license key: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Loads and validates the saved license key
     * @return true if valid license is found and validated
     */
    public static boolean checkLicense() {
        try {
            File licenseFile = new File(LICENSE_PATH);
            if (!licenseFile.exists()) {
                logger.info("No license file found at: {}", LICENSE_PATH);
                return false;
            }
            
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(licenseFile)) {
                props.load(fis);
            }
            
            String encrypted = props.getProperty("license");
            if (encrypted == null || encrypted.isEmpty()) {
                logger.warn("License file exists but contains no license key");
                return false;
            }
            
            // Decrypt and validate
            String licenseKey = decrypt(encrypted);
            boolean isValid = validateLicenseKey(licenseKey);
            
            if (isValid) {
                logger.info("License validated successfully");
            } else {
                logger.warn("Saved license key is invalid");
            }
            
            return isValid;
        } catch (Exception e) {
            logger.error("Error checking license: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Removes the saved license (for testing or re-activation)
     */
    public static void removeLicense() {
        try {
            File licenseFile = new File(LICENSE_PATH);
            if (licenseFile.exists()) {
                licenseFile.delete();
                logger.info("License file removed");
            }
        } catch (Exception e) {
            logger.error("Error removing license file: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Generates a valid license key for testing/development
     * In production, you would generate these server-side and send to customers
     * @return A valid license key
     */
    public static String generateLicenseKey() {
        try {
            // Generate random key part (16 chars = 4 groups)
            StringBuilder keyPart = new StringBuilder();
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            java.util.Random random = new java.util.Random();
            for (int i = 0; i < 16; i++) {
                keyPart.append(chars.charAt(random.nextInt(chars.length())));
            }
            
            // Calculate checksum
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String combined = keyPart.toString() + MASTER_KEY;
            byte[] hash = md.digest(combined.getBytes(StandardCharsets.UTF_8));
            String hashHex = bytesToHex(hash);
            String checksum = hashHex.substring(hashHex.length() - 4).toUpperCase();
            
            // Combine key part and checksum, format with hyphens
            String fullKey = keyPart.toString() + checksum;
            return String.format("%s-%s-%s-%s-%s",
                fullKey.substring(0, 4),
                fullKey.substring(4, 8),
                fullKey.substring(8, 12),
                fullKey.substring(12, 16),
                fullKey.substring(16, 20));
        } catch (Exception e) {
            logger.error("Error generating license key: {}", e.getMessage(), e);
            return null;
        }
    }
    
    // Encryption/Decryption methods
    private static String encrypt(String plainText) throws Exception {
        // Use a simple XOR cipher with master key hash for obfuscation
        // In production, use proper AES encryption
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = md.digest(MASTER_KEY.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    private static String decrypt(String encryptedText) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = md.digest(MASTER_KEY.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    /**
     * Gets the license file path (for display purposes)
     */
    public static String getLicensePath() {
        return LICENSE_PATH;
    }
}

