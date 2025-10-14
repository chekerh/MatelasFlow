package com.warehouse.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Firebase Realtime Database Backup Service
 * Handles automatic backups and restore functionality
 */
public class FirebaseBackupService {
    
    private static final String DATABASE_URL = "https://matelaspro-default-rtdb.europe-west1.firebasedatabase.app/";
    private static final String FIREBASE_KEY_PATH = "/firebase-key.json";
    private static DatabaseReference databaseRef;
    private static boolean initialized = false;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * Initialize Firebase Admin SDK
     */
    public static synchronized boolean initialize() {
        if (initialized) {
            return true;
        }
        
        try {
            InputStream serviceAccount = FirebaseBackupService.class.getResourceAsStream(FIREBASE_KEY_PATH);
            
            if (serviceAccount == null) {
                logError("Firebase key file not found at: " + FIREBASE_KEY_PATH);
                logError("Backup service will operate in offline mode.");
                return false;
            }
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(DATABASE_URL)
                    .build();
            
            FirebaseApp.initializeApp(options);
            databaseRef = FirebaseDatabase.getInstance().getReference();
            initialized = true;
            
            log("✅ Firebase initialized successfully");
            log("📡 Database URL: " + DATABASE_URL);
            
            return true;
        } catch (Exception e) {
            logError("Failed to initialize Firebase: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Backup database to Firebase
     * @param data Map containing all database tables
     * @return true if successful
     */
    public static CompletableFuture<Boolean> backupToFirebase(Map<String, Object> data) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (!initialized && !initialize()) {
            future.complete(false);
            return future;
        }
        
        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String backupPath = "backups/" + timestamp;
            
            log("📤 Starting backup to Firebase...");
            log("📂 Backup path: " + backupPath);
            
            DatabaseReference backupRef = databaseRef.child(backupPath);
            
            backupRef.setValueAsync(data).addListener(() -> {
                log("✅ Backup completed successfully!");
                log("🗓️  Timestamp: " + timestamp);
                future.complete(true);
            }, Runnable::run);
            
            // Timeout after 30 seconds
            CompletableFuture.delayedExecutor(30, TimeUnit.SECONDS).execute(() -> {
                if (!future.isDone()) {
                    logError("Backup timeout - operation took too long");
                    future.complete(false);
                }
            });
            
        } catch (Exception e) {
            logError("Backup failed: " + e.getMessage());
            e.printStackTrace();
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * Restore database from Firebase
     * @return Map containing restored data
     */
    public static CompletableFuture<Map<String, Object>> restoreFromFirebase() {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        if (!initialized && !initialize()) {
            future.complete(null);
            return future;
        }
        
        try {
            log("📥 Searching for latest backup...");
            
            DatabaseReference backupsRef = databaseRef.child("backups");
            
            backupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        logError("No backups found in Firebase");
                        future.complete(null);
                        return;
                    }
                    
                    // Get latest backup
                    String latestBackup = null;
                    for (DataSnapshot child : snapshot.getChildren()) {
                        if (latestBackup == null || child.getKey().compareTo(latestBackup) > 0) {
                            latestBackup = child.getKey();
                        }
                    }
                    
                    if (latestBackup == null) {
                        logError("No valid backup found");
                        future.complete(null);
                        return;
                    }
                    
                    log("📦 Found backup: " + latestBackup);
                    log("📥 Restoring data...");
                    
                    DataSnapshot backupData = snapshot.child(latestBackup);
                    Map<String, Object> data = new HashMap<>();
                    
                    for (DataSnapshot table : backupData.getChildren()) {
                        data.put(table.getKey(), table.getValue());
                    }
                    
                    log("✅ Restore completed successfully!");
                    log("📊 Tables restored: " + data.keySet());
                    
                    future.complete(data);
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    logError("Restore failed: " + error.getMessage());
                    future.complete(null);
                }
            });
            
            // Timeout after 30 seconds
            CompletableFuture.delayedExecutor(30, TimeUnit.SECONDS).execute(() -> {
                if (!future.isDone()) {
                    logError("Restore timeout - operation took too long");
                    future.complete(null);
                }
            });
            
        } catch (Exception e) {
            logError("Restore failed: " + e.getMessage());
            e.printStackTrace();
            future.complete(null);
        }
        
        return future;
    }
    
    /**
     * Schedule automatic daily backups
     */
    public static void scheduleAutomaticBackups() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                log("⏰ Automatic backup triggered");
                performAutomaticBackup();
            }
        }, getNextBackupDelay(), 24 * 60 * 60 * 1000); // Daily
        
        log("⏰ Automatic backups scheduled (daily)");
    }
    
    /**
     * Calculate delay until next backup (at 2 AM)
     */
    private static long getNextBackupDelay() {
        Calendar now = Calendar.getInstance();
        Calendar nextBackup = Calendar.getInstance();
        nextBackup.set(Calendar.HOUR_OF_DAY, 2);
        nextBackup.set(Calendar.MINUTE, 0);
        nextBackup.set(Calendar.SECOND, 0);
        
        if (nextBackup.before(now)) {
            nextBackup.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return nextBackup.getTimeInMillis() - now.getTimeInMillis();
    }
    
    /**
     * Perform automatic backup (called by scheduler)
     */
    private static void performAutomaticBackup() {
        try {
            Map<String, Object> data = BackupManager.collectDatabaseData();
            backupToFirebase(data).thenAccept(success -> {
                if (success) {
                    log("✅ Automatic backup completed");
                } else {
                    logError("❌ Automatic backup failed");
                }
            });
        } catch (Exception e) {
            logError("Automatic backup error: " + e.getMessage());
        }
    }
    
    /**
     * Check Firebase connection status
     */
    public static CompletableFuture<Boolean> testConnection() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (!initialized && !initialize()) {
            future.complete(false);
            return future;
        }
        
        try {
            databaseRef.child(".info/connected").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean connected = snapshot.exists() && snapshot.getValue(Boolean.class);
                    future.complete(connected);
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    future.complete(false);
                }
            });
            
            // Timeout after 5 seconds
            CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
                if (!future.isDone()) {
                    future.complete(false);
                }
            });
            
        } catch (Exception e) {
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * Log message to console and activity log
     */
    private static void log(String message) {
        String logMessage = "[Firebase] " + message;
        System.out.println(logMessage);
        try {
            Files.write(Paths.get("activity_log.txt"), 
                (new Date() + " - " + logMessage + "\n").getBytes(), 
                java.nio.file.StandardOpenOption.CREATE, 
                java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            // Ignore
        }
    }
    
    /**
     * Log error message
     */
    private static void logError(String message) {
        String logMessage = "[Firebase ERROR] " + message;
        System.err.println(logMessage);
        try {
            Files.write(Paths.get("activity_log.txt"), 
                (new Date() + " - " + logMessage + "\n").getBytes(), 
                java.nio.file.StandardOpenOption.CREATE, 
                java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            // Ignore
        }
    }
    
    /**
     * Check if Firebase is initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
