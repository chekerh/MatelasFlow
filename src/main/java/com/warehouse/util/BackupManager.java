package com.warehouse.util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class BackupManager {
    private static final String BACKUP_DIR = "backups/";
    private static final String CONFIG_FILE = "backup_config.properties";
    private static final int MAX_BACKUPS = 10; // Garder seulement les 10 dernières sauvegardes
    
    public static boolean createBackup() {
        try {
            // Créer le dossier de sauvegarde s'il n'existe pas
            File backupDir = new File(BACKUP_DIR);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            // Nom du fichier de sauvegarde
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String backupFile = BACKUP_DIR + "warehouse_backup_" + timestamp + ".sql";
            
            // Commande mysqldump (Windows)
            String command = String.format(
                "mysqldump -u root -p warehouse_mattress > \"%s\"",
                backupFile
            );
            
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", command);
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // Sauvegarder la configuration
                saveBackupConfig(backupFile);
                
                // Nettoyer les anciennes sauvegardes
                cleanupOldBackups();
                
                return true;
            } else {
                System.err.println("Erreur lors de la sauvegarde. Code de sortie: " + exitCode);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la sauvegarde: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean restoreBackup(String backupFile) {
        try {
            // Commande mysql pour restaurer
            String command = String.format(
                "mysql -u root -p warehouse_mattress < \"%s\"",
                backupFile
            );
            
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", command);
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            return exitCode == 0;
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la restauration: " + e.getMessage());
            return false;
        }
    }
    
    public static String[] listBackups() {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            return new String[0];
        }
        
        File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".sql"));
        if (files == null) {
            return new String[0];
        }
        
        String[] backupFiles = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            backupFiles[i] = files[i].getName();
        }
        
        return backupFiles;
    }
    
    private static void saveBackupConfig(String backupFile) {
        try {
            Properties props = new Properties();
            props.setProperty("last_backup", backupFile);
            props.setProperty("backup_date", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            
            try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
                props.store(out, "Configuration de sauvegarde");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde de la configuration: " + e.getMessage());
        }
    }
    
    private static void cleanupOldBackups() {
        try {
            File backupDir = new File(BACKUP_DIR);
            File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".sql"));
            
            if (files != null && files.length > MAX_BACKUPS) {
                // Trier par date de modification (plus ancien en premier)
                java.util.Arrays.sort(files, (f1, f2) -> 
                    Long.compare(f1.lastModified(), f2.lastModified()));
                
                // Supprimer les plus anciens
                for (int i = 0; i < files.length - MAX_BACKUPS; i++) {
                    files[i].delete();
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du nettoyage des sauvegardes: " + e.getMessage());
        }
    }
    
    public static String getLastBackupDate() {
        try {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
                props.load(in);
                return props.getProperty("backup_date", "Jamais");
            }
        } catch (IOException e) {
            return "Jamais";
        }
    }
    
    public static boolean isBackupNeeded() {
        String lastBackup = getLastBackupDate();
        if ("Jamais".equals(lastBackup)) {
            return true;
        }
        
        try {
            LocalDateTime lastBackupTime = LocalDateTime.parse(lastBackup, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime now = LocalDateTime.now();
            
            // Sauvegarde recommandée si plus de 7 jours
            return now.isAfter(lastBackupTime.plusDays(7));
        } catch (Exception e) {
            return true;
        }
    }
    
    public static void scheduleAutomaticBackup() {
        // Cette méthode pourrait être appelée au démarrage de l'application
        if (isBackupNeeded()) {
            NotificationSystem.showBackupReminder();
        }
    }
} 