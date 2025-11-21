package com.warehouse.util;

import com.warehouse.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ActivityLogger {
    private static final Logger logger = LoggerFactory.getLogger(ActivityLogger.class);
    private static final String LOG_FILE = "activity_log.txt";
    private static final String SUSPICIOUS_LOG_FILE = "suspicious_activities.txt";
    private static final Map<String, Integer> userActivityCount = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> userActions = new ConcurrentHashMap<>();
    
    // Seuils pour détecter les activités suspectes
    private static final int MAX_ACTIONS_PER_HOUR = 50;
    private static final int MAX_DELETE_ACTIONS_PER_DAY = 10;
    private static final int MAX_FAILED_LOGINS = 5;
    private static final double MIN_SALE_PRICE_RATIO = 0.3; // Prix de vente minimum vs prix original
    
    public enum ActivityType {
        LOGIN_SUCCESS("Connexion réussie"),
        LOGIN_FAILED("Tentative de connexion échouée"),
        LOGOUT("Déconnexion"),
        ADD_MATTRESS("Ajout de matelas"),
        EDIT_MATTRESS("Modification de matelas"),
        DELETE_MATTRESS("Suppression de matelas"),
        ADD_TRANSACTION("Ajout de transaction"),
        EDIT_TRANSACTION("Modification de transaction"),
        DELETE_TRANSACTION("Suppression de transaction"),
        ADD_USER("Ajout d'utilisateur"),
        EDIT_USER("Modification d'utilisateur"),
        DELETE_USER("Suppression d'utilisateur"),
        GENERATE_REPORT("Génération de rapport"),
        VIEW_STATISTICS("Consultation des statistiques"),
        SUSPICIOUS_ACTIVITY("Activité suspecte détectée");
    
        private final String description;
        
        ActivityType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public static void logActivity(User user, ActivityType activityType, String details) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = String.format("[%s] %s (%s) - %s: %s", 
            timestamp, user.getUsername(), user.getRole(), activityType.getDescription(), details);
        
        // Écrire dans le fichier de log
        writeToFile(LOG_FILE, logEntry);
        
        // Mettre à jour les statistiques utilisateur
        updateUserStats(user.getUsername(), activityType);
        
        // Vérifier les activités suspectes
        checkForSuspiciousActivity(user, activityType, details);
    }
    
    public static void logSuspiciousActivity(User user, String reason, String details) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = String.format("[%s] ⚠️ ACTIVITÉ SUSPECTE - %s (%s) - Raison: %s - Détails: %s", 
            timestamp, user.getUsername(), user.getRole(), reason, details);
        
        // Écrire dans le fichier des activités suspectes
        writeToFile(SUSPICIOUS_LOG_FILE, logEntry);
        
        // Écrire aussi dans le log principal
        writeToFile(LOG_FILE, logEntry);
    }
    
    private static void updateUserStats(String username, ActivityType activityType) {
        String hourKey = username + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        
        // Compter les actions par heure
        userActivityCount.put(hourKey, userActivityCount.getOrDefault(hourKey, 0) + 1);
        
        // Garder un historique des actions
        userActions.computeIfAbsent(username, k -> new ArrayList<>()).add(activityType.name());
        
        // Limiter l'historique à 100 actions par utilisateur
        List<String> actions = userActions.get(username);
        if (actions.size() > 100) {
            actions.subList(0, actions.size() - 100);
        }
    }
    
    private static void checkForSuspiciousActivity(User user, ActivityType activityType, String details) {
        String username = user.getUsername();
        String hourKey = username + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));
        
        // Vérifier le nombre d'actions par heure
        int hourlyActions = userActivityCount.getOrDefault(hourKey, 0);
        if (hourlyActions > MAX_ACTIONS_PER_HOUR) {
            logSuspiciousActivity(user, "Trop d'actions par heure", 
                "Actions: " + hourlyActions + " (max: " + MAX_ACTIONS_PER_HOUR + ")");
        }
        
        // Vérifier les suppressions excessives
        if (activityType == ActivityType.DELETE_MATTRESS || activityType == ActivityType.DELETE_TRANSACTION) {
            String dayKey = username + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            int dailyDeletes = userActivityCount.getOrDefault(dayKey + "_DELETE", 0) + 1;
            userActivityCount.put(dayKey + "_DELETE", dailyDeletes);
            
            if (dailyDeletes > MAX_DELETE_ACTIONS_PER_DAY) {
                logSuspiciousActivity(user, "Trop de suppressions par jour", 
                    "Suppressions: " + dailyDeletes + " (max: " + MAX_DELETE_ACTIONS_PER_DAY + ")");
            }
        }
        
        // Vérifier les tentatives de connexion échouées
        if (activityType == ActivityType.LOGIN_FAILED) {
            String dayKey = username + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            int failedLogins = userActivityCount.getOrDefault(dayKey + "_FAILED_LOGIN", 0) + 1;
            userActivityCount.put(dayKey + "_FAILED_LOGIN", failedLogins);
            
            if (failedLogins > MAX_FAILED_LOGINS) {
                logSuspiciousActivity(user, "Trop de tentatives de connexion échouées", 
                    "Échecs: " + failedLogins + " (max: " + MAX_FAILED_LOGINS + ")");
            }
        }
        
        // Vérifier les ventes à prix très bas
        if (activityType == ActivityType.ADD_TRANSACTION && details.contains("Vente")) {
            checkSuspiciousSale(user, details);
        }
    }
    
    private static void checkSuspiciousSale(User user, String details) {
        // Extraire le prix de vente et le prix original des détails
        try {
            // Format attendu: "Vente - Prix: X.XX€ - Prix original: Y.YY€"
            if (details.contains("Prix:") && details.contains("Prix original:")) {
                String[] parts = details.split("Prix original:");
                if (parts.length > 1) {
                    String salePriceStr = parts[0].split("Prix:")[1].trim().replace("€", "");
                    String originalPriceStr = parts[1].trim().replace("€", "");
                    
                    double salePrice = Double.parseDouble(salePriceStr);
                    double originalPrice = Double.parseDouble(originalPriceStr);
                    
                    if (originalPrice > 0 && salePrice < originalPrice * MIN_SALE_PRICE_RATIO) {
                        logSuspiciousActivity(user, "Vente à prix très bas", 
                            "Prix de vente: " + salePrice + "€, Prix original: " + originalPrice + "€");
                    }
                }
            }
        } catch (Exception e) {
            // Ignorer les erreurs de parsing
        }
    }
    
    private static void writeToFile(String filename, String content) {
        try (FileWriter fw = new FileWriter(filename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(content);
        } catch (IOException e) {
            logger.error("Erreur lors de l'écriture du log {}: {}", filename, e.getMessage(), e);
        }
    }
    
    public static List<String> getSuspiciousActivities() {
        List<String> activities = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(SUSPICIOUS_LOG_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                activities.add(line);
            }
        } catch (IOException e) {
            logger.warn("Erreur lors de la lecture des activités suspectes: {}", e.getMessage());
        }
        return activities;
    }
    
    public static List<String> getRecentActivities(int hours) {
        List<String> activities = new ArrayList<>();
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        
        try (BufferedReader br = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("[")) {
                    try {
                        String timestampStr = line.substring(1, line.indexOf("]"));
                        LocalDateTime activityTime = LocalDateTime.parse(timestampStr, 
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        
                        if (activityTime.isAfter(cutoff)) {
                            activities.add(line);
                        }
                    } catch (Exception e) {
                        // Ignorer les lignes mal formatées
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("Erreur lors de la lecture des activités récentes: {}", e.getMessage());
        }
        
        return activities;
    }
    
    public static Map<String, Integer> getUserActivitySummary() {
        Map<String, Integer> summary = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("(") && line.contains(")")) {
                    String username = line.substring(line.indexOf("]") + 2, line.indexOf("(")).trim();
                    summary.put(username, summary.getOrDefault(username, 0) + 1);
                }
            }
        } catch (IOException e) {
            logger.warn("Erreur lors de la génération du résumé: {}", e.getMessage());
        }
        
        return summary;
    }
} 