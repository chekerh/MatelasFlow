package com.warehouse.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.warehouse.util.ActivityLogger;
import com.warehouse.model.User;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AdminLogsController {
    @FXML private ComboBox<String> periodComboBox;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    @FXML private Spinner<Integer> hoursSpinner;
    @FXML private TextArea recentActivitiesArea;
    @FXML private TextArea suspiciousActivitiesArea;
    @FXML private Button clearSuspiciousButton;
    @FXML private TableView<UserActivityStats> statsTable;
    @FXML private TableColumn<UserActivityStats, String> userColumn;
    @FXML private TableColumn<UserActivityStats, String> roleColumn;
    @FXML private TableColumn<UserActivityStats, Integer> activityCountColumn;
    @FXML private TableColumn<UserActivityStats, String> lastActivityColumn;
    @FXML private Spinner<Integer> maxActionsSpinner;
    @FXML private Spinner<Integer> maxDeletesSpinner;
    @FXML private Spinner<Integer> maxFailedLoginsSpinner;
    @FXML private Spinner<Double> minPriceRatioSpinner;
    @FXML private Button saveConfigButton;
    @FXML private Button resetConfigButton;
    @FXML private Label statusLabel;
    
    private ObservableList<UserActivityStats> statsList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        setupComboBoxes();
        setupSpinners();
        setupTable();
        loadRecentActivities();
        loadSuspiciousActivities();
        loadUserStats();
        updateStatus("Interface de surveillance chargée");
    }
    
    private void setupComboBoxes() {
        // Périodes
        periodComboBox.setItems(FXCollections.observableArrayList(
            "Dernière heure", "Dernières 6 heures", "Dernières 12 heures", 
            "Dernières 24 heures", "Dernière semaine", "Tout"
        ));
        periodComboBox.getSelectionModel().select(3); // 24 heures par défaut
        
        // Types d'activité
        typeComboBox.setItems(FXCollections.observableArrayList(
            "Tous les types", "Connexions", "Transactions", "Modifications", "Suppressions", "Rapports"
        ));
        typeComboBox.getSelectionModel().select(0);
    }
    
    private void setupSpinners() {
        // Configuration des spinners
        hoursSpinner.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(1, 168, 24));
        
        maxActionsSpinner.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(10, 200, 50));
        maxDeletesSpinner.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 10));
        maxFailedLoginsSpinner.setValueFactory(new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 5));
        
        minPriceRatioSpinner.setValueFactory(new javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.3, 0.1));
    }
    
    private void setupTable() {
        userColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        roleColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole()));
        activityCountColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getActivityCount()).asObject());
        lastActivityColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLastActivity()));
        
        statsTable.setItems(statsList);
    }
    
    @FXML
    private void handleRefresh() {
        loadRecentActivities();
        loadSuspiciousActivities();
        loadUserStats();
        updateStatus("Données actualisées");
    }
    
    @FXML
    private void handleExport() {
        try {
            String filename = "rapport_surveillance_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".txt";
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                writer.println("=== RAPPORT DE SURVEILLANCE ===");
                writer.println("Généré le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.println();
                
                writer.println("--- ACTIVITÉS RÉCENTES ---");
                List<String> recentActivities = ActivityLogger.getRecentActivities(hoursSpinner.getValue());
                for (String activity : recentActivities) {
                    writer.println(activity);
                }
                writer.println();
                
                writer.println("--- ACTIVITÉS SUSPECTES ---");
                List<String> suspiciousActivities = ActivityLogger.getSuspiciousActivities();
                for (String activity : suspiciousActivities) {
                    writer.println(activity);
                }
                writer.println();
                
                writer.println("--- STATISTIQUES UTILISATEURS ---");
                Map<String, Integer> userStats = ActivityLogger.getUserActivitySummary();
                for (Map.Entry<String, Integer> entry : userStats.entrySet()) {
                    writer.println(entry.getKey() + ": " + entry.getValue() + " actions");
                }
            }
            
            updateStatus("Rapport exporté: " + filename);
        } catch (IOException e) {
            updateStatus("Erreur lors de l'export: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClearSuspicious() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Effacer les activités suspectes");
        alert.setContentText("Êtes-vous sûr de vouloir effacer toutes les activités suspectes ?");
        
        if (alert.showAndWait().orElse(null) == ButtonType.OK) {
            try {
                new File("suspicious_activities.txt").delete();
                loadSuspiciousActivities();
                updateStatus("Activités suspectes effacées");
            } catch (Exception e) {
                updateStatus("Erreur lors de l'effacement: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleSaveConfig() {
        // Ici on pourrait sauvegarder les configurations dans un fichier
        updateStatus("Configuration sauvegardée");
    }
    
    @FXML
    private void handleResetConfig() {
        maxActionsSpinner.getValueFactory().setValue(50);
        maxDeletesSpinner.getValueFactory().setValue(10);
        maxFailedLoginsSpinner.getValueFactory().setValue(5);
        minPriceRatioSpinner.getValueFactory().setValue(0.3);
        updateStatus("Configuration réinitialisée");
    }
    
    private void loadRecentActivities() {
        List<String> activities = ActivityLogger.getRecentActivities(hoursSpinner.getValue());
        StringBuilder sb = new StringBuilder();
        for (String activity : activities) {
            sb.append(activity).append("\n");
        }
        recentActivitiesArea.setText(sb.toString());
    }
    
    private void loadSuspiciousActivities() {
        List<String> activities = ActivityLogger.getSuspiciousActivities();
        StringBuilder sb = new StringBuilder();
        for (String activity : activities) {
            sb.append(activity).append("\n");
        }
        suspiciousActivitiesArea.setText(sb.toString());
    }
    
    private void loadUserStats() {
        statsList.clear();
        Map<String, Integer> userStats = ActivityLogger.getUserActivitySummary();
        
        for (Map.Entry<String, Integer> entry : userStats.entrySet()) {
            // Simuler des données pour la démo
            statsList.add(new UserActivityStats(
                entry.getKey(),
                "admin".equals(entry.getKey()) ? "admin" : "employé",
                entry.getValue(),
                LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ));
        }
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    // Classe interne pour les statistiques utilisateur
    public static class UserActivityStats {
        private String username;
        private String role;
        private int activityCount;
        private String lastActivity;
        
        public UserActivityStats(String username, String role, int activityCount, String lastActivity) {
            this.username = username;
            this.role = role;
            this.activityCount = activityCount;
            this.lastActivity = lastActivity;
        }
        
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public int getActivityCount() { return activityCount; }
        public String getLastActivity() { return lastActivity; }
    }
} 