package com.warehouse.security;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TwoFactorAuth {
    private static final Map<String, String> userCodes = new HashMap<>();
    private static final Map<String, LocalDateTime> codeExpiry = new HashMap<>();
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 5;
    
    /**
     * Génère un code 2FA pour un utilisateur
     */
    public static String generateCode(String username) {
        String code = generateRandomCode();
        userCodes.put(username, code);
        codeExpiry.put(username, LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));
        return code;
    }
    
    /**
     * Vérifie le code 2FA
     */
    public static boolean verifyCode(String username, String code) {
        String storedCode = userCodes.get(username);
        LocalDateTime expiry = codeExpiry.get(username);
        
        if (storedCode == null || expiry == null) {
            return false;
        }
        
        // Vérifier si le code a expiré
        if (LocalDateTime.now().isAfter(expiry)) {
            userCodes.remove(username);
            codeExpiry.remove(username);
            return false;
        }
        
        // Vérifier le code
        if (storedCode.equals(code)) {
            // Supprimer le code après utilisation
            userCodes.remove(username);
            codeExpiry.remove(username);
            return true;
        }
        
        return false;
    }
    
    /**
     * Vérifie si un utilisateur a un code 2FA actif
     */
    public static boolean hasActiveCode(String username) {
        LocalDateTime expiry = codeExpiry.get(username);
        if (expiry == null) {
            return false;
        }
        
        if (LocalDateTime.now().isAfter(expiry)) {
            userCodes.remove(username);
            codeExpiry.remove(username);
            return false;
        }
        
        return true;
    }
    
    /**
     * Supprime le code 2FA d'un utilisateur
     */
    public static void clearCode(String username) {
        userCodes.remove(username);
        codeExpiry.remove(username);
    }
    
    private static String generateRandomCode() {
        Random random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        
        return code.toString();
    }
    
    /**
     * Obtient le temps restant avant expiration du code
     */
    public static long getRemainingTime(String username) {
        LocalDateTime expiry = codeExpiry.get(username);
        if (expiry == null) {
            return 0;
        }
        
        return Math.max(0, java.time.Duration.between(LocalDateTime.now(), expiry).toSeconds());
    }
} 