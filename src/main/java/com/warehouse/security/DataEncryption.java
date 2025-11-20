package com.warehouse.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;

public class DataEncryption {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static SecretKey secretKey;
    
    static {
        try {
            // Générer une clé de chiffrement
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(256);
            secretKey = keyGen.generateKey();
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération de la clé: " + e.getMessage());
        }
    }
    
    /**
     * Chiffre une chaîne de caractères
     */
    public static String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            System.err.println("Erreur lors du chiffrement: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Déchiffre une chaîne de caractères
     */
    public static String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes);
        } catch (Exception e) {
            System.err.println("Erreur lors du déchiffrement: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Chiffre les données sensibles d'une transaction
     */
    public static String encryptTransactionData(String transactionData) {
        return encrypt(transactionData);
    }
    
    /**
     * Déchiffre les données sensibles d'une transaction
     */
    public static String decryptTransactionData(String encryptedTransactionData) {
        return decrypt(encryptedTransactionData);
    }
    
    /**
     * Chiffre les informations utilisateur sensibles
     */
    public static String encryptUserData(String userData) {
        return encrypt(userData);
    }
    
    /**
     * Déchiffre les informations utilisateur sensibles
     */
    public static String decryptUserData(String encryptedUserData) {
        return decrypt(encryptedUserData);
    }
    
    /**
     * Génère un hash sécurisé pour les mots de passe
     */
    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            System.err.println("Erreur lors du hashage: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Vérifie si un mot de passe correspond au hash
     */
    public static boolean verifyPassword(String password, String hash) {
        String passwordHash = hashPassword(password);
        return passwordHash != null && passwordHash.equals(hash);
    }
    
    /**
     * Génère un token de session sécurisé
     */
    public static String generateSessionToken() {
        try {
            SecureRandom random = new SecureRandom();
            byte[] tokenBytes = new byte[32];
            random.nextBytes(tokenBytes);
            return Base64.getEncoder().encodeToString(tokenBytes);
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du token: " + e.getMessage());
            return null;
        }
    }
} 