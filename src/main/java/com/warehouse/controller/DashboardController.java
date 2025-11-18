package com.warehouse.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import com.warehouse.model.User;
import com.warehouse.model.UserDAO;
import com.warehouse.util.ActivityLogger;
import com.warehouse.util.ThemePreferences;
import com.warehouse.controller.AdvancedFeaturesController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DashboardController {
    @FXML private ImageView logoImage;
    @FXML private Label welcomeLabel;
    @FXML private Label userInfoLabel;
    @FXML private Text duaaHeadingText;
    @FXML private Text duaaText;
    @FXML private Button logoutButton;
    @FXML private Button darkModeButton;
    @FXML private Button powerButton;
    @FXML private VBox navBox;
    @FXML private VBox contentPane;
    @FXML private StackPane overlayPane;
    @FXML private VBox notificationArea;
    @FXML private Label notificationLabel;
    @FXML private Button userManagementButton;
    @FXML private Button adminLogsButton;
    @FXML private Button advancedFeaturesButton;
    
    private String currentUser;
    private String currentRole;
    private boolean isDarkMode = false;
    
    // Dynamic Duaa rotation
    private static final String DUAA_RESOURCE = "/text/duaas.txt";
    private static final String DEFAULT_DUAA_HEADING = "\u0628\u0650\u0633\u0652\u0645\u0650 \u0627\u0644\u0644\u0647\u0650 \u0627\u0644\u0631\u0651\u064e\u062d\u0652\u0645\u064e\u0646\u0650 \u0627\u0644\u0631\u0651\u064e\u062d\u0650\u064a\u0645\u0650";
    private static final List<String> DEFAULT_DUAAS = Arrays.asList(
        "\u0627\u0644\u0644\u0647\u064f\u0645\u064e \u0628\u0627\u0631\u0650\u0643\u0652 \u0644\u064e\u0646\u0627 \u0641\u0650\u064a \u064a\u064e\u0648\u0652\u0645\u0650\u0646\u064e\u0627 \u0647\u064e\u0630\u0627",
        "\u0627\u0644\u0644\u0647\u064f\u0645\u064e \u0648\u064e\u0641\u0651\u0650\u0642\u0652\u0646\u064e\u0627 \u0644\u0650\u0645\u064e\u0627 \u062a\u064f\u062d\u0650\u0628\u0651\u064f \u0648\u064e\u062a\u064e\u0631\u0636\u0649\u0649",
        "\u0631\u064e\u0628\u0651\u064e\u0646\u064e\u0627 \u0622\u062a\u0650\u0646\u064e\u0627 \u0641\u0650\u064a \u0627\u0644\u062f\u0651\u064f\u0646\u0652\u064a\u064e\u0627 \u062d\u064e\u0633\u064e\u0646\u064e\u0629\u064b \u0648\u064e\u0641\u0650\u064a \u0627\u0644\u0622\u062e\u0650\u0631\u064e\u0629\u0650 \u062d\u064e\u0633\u064e\u0646\u064e\u0629\u064b",
        "\u0627\u0644\u0644\u0647\u064f\u0645\u064e \u0627\u062c\u0652\u0639\u064e\u0644\u0652 \u0639\u064e\u0645\u064e\u0644\u064e\u0646\u064e\u0627 \u062e\u0627\u0644\u0650\u0635\u064b\u0627 \u0644\u0650\u0648\u064e\u062c\u0652\u0647\u0650\u0643\u064e \u0627\u0644\u0643\u064e\u0631\u0650\u064a\u0645",
        "\u0627\u0644\u0644\u0647\u064f\u0645\u064e \u0625\u0650\u0646\u0651\u064e\u0627 \u0646\u064e\u0633\u0652\u0623\u064e\u0644\u064f\u0643\u064e \u0627\u0644\u0639\u064e\u0641\u0652\u0648\u064e \u0648\u064e\u0627\u0644\u0639\u064e\u0627\u0641\u0650\u064a\u064e\u0629",
        "\u0627\u0644\u0644\u0647\u064f\u0645\u064e \u0627\u0647\u0652\u062f\u0650\u0646\u064e\u0627 \u0641\u0650\u064a\u0645\u064e\u0646\u0652 \u0647\u064e\u062f\u064e\u064a\u0652\u062a",
        "\u0633\u064f\u0628\u0652\u062d\u064e\u0627\u0646\u064e \u0627\u0644\u0644\u0647\u0650 \u0648\u064e\u0628\u0650\u062d\u064e\u0645\u0652\u062f\u0650\u0647\u0650 \u0633\u064f\u0628\u0652\u062d\u064e\u0627\u0646\u064e \u0627\u0644\u0644\u0647\u0650 \u0627\u0644\u0639\u064e\u0638\u0650\u064a\u0645",
        "\u0627\u0644\u062d\u064e\u0645\u0652\u062f\u064f \u0644\u0650\u0644\u0647\u0650 \u0631\u064e\u0628\u0651\u0650 \u0627\u0644\u0639\u064e\u0627\u0644\u064e\u0645\u0650\u064a\u0646"
    );
    private String duaaHeading = DEFAULT_DUAA_HEADING;
    private List<String> duaas = new ArrayList<>(DEFAULT_DUAAS);
    private int currentDuaaIndex = 0;
    private Timer duaaRotationTimer;

    @FXML
    public void initialize() {
        loadDuaasFromResource();
        // Set logo
        try {
            Image logo = new Image(getClass().getResource("/images/SuperMousse.jpg").toExternalForm());
            logoImage.setImage(logo);
        } catch (Exception e) {
            System.out.println("Logo not found: " + e.getMessage());
            // Try white logo as fallback
            try {
                Image logo = new Image(getClass().getResource("/images/SuperMousse.jpg").toExternalForm());
                logoImage.setImage(logo);
            } catch (Exception e2) {
                System.out.println("White logo also not found: " + e2.getMessage());
            }
        }
        
        // Configure duaa banner for RTL display
        configureDuaaBanner();

        // Set up notification area
        notificationArea.setVisible(false);
        notificationArea.setManaged(false);
        
        // Apply dark mode if needed
        applyDarkModeToChildren(contentPane, isDarkMode);
        
        // Load default content: statistics page
        showStatistics(null);
        
        // Start dynamic duaa rotation
        startDuaaRotation();
        
        // Load and apply saved theme preference
        applySavedTheme();
    }
    
    /**
     * Loads and applies the saved theme preference
     */
    private void applySavedTheme() {
        try {
            String savedTheme = ThemePreferences.loadTheme();
            if (savedTheme != null && !savedTheme.isEmpty()) {
                // Get the scene from any node
                Scene scene = contentPane.getScene();
                if (scene == null) {
                    // If scene not ready yet, try to get it from root
                    scene = contentPane.getScene();
                    if (scene == null) {
                        // Schedule to apply theme after scene is ready
                        javafx.application.Platform.runLater(() -> {
                            Scene laterScene = contentPane.getScene();
                            if (laterScene != null) {
                                AdvancedFeaturesController.applyThemeToScene(laterScene, savedTheme);
                            }
                        });
                        return;
                    }
                }
                AdvancedFeaturesController.applyThemeToScene(scene, savedTheme);
            }
        } catch (Exception e) {
            System.err.println("Error applying saved theme: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Start the dynamic duaa rotation with fade animation
     * Changes duaa every 10 seconds
     */
    private void startDuaaRotation() {
        if (duaaText != null && !duaas.isEmpty()) {
            // Set initial duaa
            duaaText.setText(duaas.get(0));
            
            // Schedule periodic rotation
            duaaRotationTimer = new Timer(true);
            duaaRotationTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> rotateDuaa());
                }
            }, 10000, 10000); // Every 10 seconds
        }
    }
    
    /**
     * Rotate to next duaa with smooth fade transition
     */
    private void rotateDuaa() {
        if (duaaText == null) return;
        
        // Fade out
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), duaaText);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        fadeOut.setOnFinished(e -> {
            // Change text
            currentDuaaIndex = (currentDuaaIndex + 1) % duaas.size();
            duaaText.setText(duaas.get(currentDuaaIndex));
            
            // Fade in
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), duaaText);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        
        fadeOut.play();
    }

    private void loadDuaasFromResource() {
        List<String> lines = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream(DUAA_RESOURCE)) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty()) {
                            lines.add(line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Impossible de charger le fichier des invocations: " + e.getMessage());
        }

        if (lines.size() > 1) {
            duaaHeading = lines.get(0);
            duaas = new ArrayList<>(lines.subList(1, lines.size()));
        } else {
            duaaHeading = DEFAULT_DUAA_HEADING;
            duaas = new ArrayList<>(DEFAULT_DUAAS);
        }
    }

    private void configureDuaaBanner() {
        Font headingFont = resolveArabicFont(18);
        Font textFont = resolveArabicFont(16);

        if (duaaHeadingText != null) {
            duaaHeadingText.setText(duaaHeading);
            duaaHeadingText.setTextAlignment(TextAlignment.RIGHT);
            duaaHeadingText.setFill(Color.web("#27ae60"));
            duaaHeadingText.setFont(headingFont);
            duaaHeadingText.setWrappingWidth(440);
        }

        if (duaaText != null) {
            duaaText.setText(duaas.get(0));
            duaaText.setTextAlignment(TextAlignment.RIGHT);
            duaaText.setFill(Color.web("#2980b9"));
            duaaText.setFont(textFont);
            duaaText.setWrappingWidth(440);
        }
    }

    private Font resolveArabicFont(int size) {
        // Prefer Arabic-capable system fonts without forcing BOLD (some fonts lack bold glyphs)
        String[] candidates = {
            "Noto Naskh Arabic",
            "Noto Sans Arabic",
            "Geeza Pro",
            "Al Bayan",
            "Tahoma",
            "Arial Unicode MS",
            "Arial"
        };
        for (String family : candidates) {
            try {
                Font font = Font.font(family, FontWeight.NORMAL, size);
                if (font != null && font.getName() != null && font.getName().toLowerCase().contains(family.toLowerCase())) {
                    return font;
                }
            } catch (Exception ignored) {
            }
        }
        return Font.font("Arial", FontWeight.NORMAL, size);
    }

    @FXML
    private void initializeNavbarLayout() {
        // Prevent welcome and user info labels from being compressed to 0 width
        if (welcomeLabel != null) {
            welcomeLabel.setMinWidth(Region.USE_PREF_SIZE);
        }
        // Ensure action buttons keep readable size (CSS also enforces min-width)
        if (darkModeButton != null) darkModeButton.setMinWidth(140);
        if (powerButton != null) powerButton.setMinWidth(140);
        if (logoutButton != null) logoutButton.setMinWidth(140);
    }

    public void setUser(String username, String role) {
        this.currentUser = username;
        this.currentRole = role;
        
        // Update welcome message
        String roleDisplay = "admin".equals(role) ? "Administrateur" : "Employé";
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue, " + username);
        }
        if (userInfoLabel != null) {
            userInfoLabel.setText("Utilisateur: " + username + " (" + roleDisplay + ")");
        }
        
        // Show/hide admin buttons based on role
        boolean isAdmin = "admin".equals(role);
        userManagementButton.setVisible(isAdmin);
        userManagementButton.setManaged(isAdmin);
        adminLogsButton.setVisible(isAdmin);
        adminLogsButton.setManaged(isAdmin);
        advancedFeaturesButton.setVisible(isAdmin);
        advancedFeaturesButton.setManaged(isAdmin);
        
        // Leave window sizing to user to avoid macOS fullscreen crashes
    }
    
    public String getCurrentUser() {
        return currentUser;
    }
    
    public String getCurrentRole() {
        return currentRole;
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Log logout activity
            User currentUserObj = UserDAO.findByUsername(currentUser);
            if (currentUserObj != null) {
                ActivityLogger.logActivity(currentUserObj, ActivityLogger.ActivityType.LOGOUT, 
                    "Déconnexion depuis le dashboard");
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent loginRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleDarkMode(ActionEvent event) {
        isDarkMode = !isDarkMode;
        darkModeButton.setText(isDarkMode ? "☀️ Mode Clair" : "🌙 Mode Sombre");
        
        // Apply dark mode to all main containers
        applyDarkModeToChildren(contentPane, isDarkMode);
        applyDarkModeToChildren(navBox, isDarkMode);
        applyDarkModeToChildren(notificationArea, isDarkMode);
        
        // Apply dark mode to the root scene
        Scene scene = contentPane.getScene();
        if (scene != null) {
            scene.getRoot().getStyleClass().removeAll("dark-mode");
            if (isDarkMode) {
                scene.getRoot().getStyleClass().add("dark-mode");
            }
        }
        
        // Update logo for dark mode
        try {
            if (isDarkMode) {
                Image logo = new Image(getClass().getResource("/images/SuperMousse.jpg").toExternalForm());
                logoImage.setImage(logo);
            } else {
                Image logo = new Image(getClass().getResource("/images/SuperMousse.jpg").toExternalForm());
                logoImage.setImage(logo);
            }
        } catch (Exception e) {
            System.out.println("Logo not found for dark mode: " + e.getMessage());
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        // Optional: confirm before quitting
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Quitter l'application");
        alert.setHeaderText("Voulez-vous vraiment quitter MatelasPro ?");
        alert.setContentText("Toutes les fenêtres seront fermées.");
        alert.initOwner(((Node) event.getSource()).getScene().getWindow());
        alert.setGraphic(null);
        if (alert.getDialogPane() != null) {
            alert.getDialogPane().getStyleClass().add("modern-alert");
        }

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                Platform.exit();
            }
        });
    }

    private void applyDarkModeToChildren(javafx.scene.Node node, boolean darkMode) {
        if (darkMode) {
            node.getStyleClass().add("dark-mode");
        } else {
            node.getStyleClass().remove("dark-mode");
        }
        if (node instanceof javafx.scene.Parent) {
            for (javafx.scene.Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                applyDarkModeToChildren(child, darkMode);
            }
        }
    }

    // Notification system
    public void showNotification(String message, boolean isError) {
        notificationLabel.setText(message);
        notificationArea.getStyleClass().clear();
        notificationArea.getStyleClass().add("notification-area");
        if (isError) {
            notificationArea.getStyleClass().add("error");
        }
        
        notificationArea.setVisible(true);
        notificationArea.setManaged(true);
        
        // Auto-hide after 3 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            notificationArea.setVisible(false);
            notificationArea.setManaged(false);
        });
        pause.play();
    }

    // Overlay management
    public void showOverlay(Parent overlayContent) {
        overlayPane.getChildren().clear();
        overlayPane.getChildren().add(overlayContent);
        overlayPane.setVisible(true);
        overlayPane.setManaged(true);
        
        if (isDarkMode) {
            applyDarkModeToChildren(overlayContent, true);
        }
    }

    public void hideOverlay() {
        overlayPane.setVisible(false);
        overlayPane.setManaged(false);
        overlayPane.getChildren().clear();
    }

    // Navigation methods
    @FXML
    private void showInventory(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/InventoryView.fxml"));
            Parent inventoryRoot = loader.load();
            InventoryController inventoryController = loader.getController();
            inventoryController.setDashboardController(this);
            contentPane.getChildren().setAll(inventoryRoot);
            
            // Apply dark mode to new content if needed
            if (isDarkMode) {
                applyDarkModeToChildren(inventoryRoot, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showTransactions(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TransactionsView.fxml"));
            Parent transactionsRoot = loader.load();
            TransactionsController transactionsController = loader.getController();
            transactionsController.setDashboardController(this);
            contentPane.getChildren().setAll(transactionsRoot);
            
            // Apply dark mode to new content if needed
            if (isDarkMode) {
                applyDarkModeToChildren(transactionsRoot, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showStoreOwners(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StoreOwnersView.fxml"));
            Parent ownersRoot = loader.load();
            StoreOwnersController storeOwnersController = loader.getController();
            storeOwnersController.setDashboardController(this);
            contentPane.getChildren().setAll(ownersRoot);
            
            // Apply dark mode to new content if needed
            if (isDarkMode) {
                applyDarkModeToChildren(ownersRoot, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showUserManagement(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserManagementView.fxml"));
            Parent userRoot = loader.load();
            UserManagementController userManagementController = loader.getController();
            userManagementController.setDashboardController(this);
            contentPane.getChildren().setAll(userRoot);
            
            // Apply dark mode to new content if needed
            if (isDarkMode) {
                applyDarkModeToChildren(userRoot, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showStatistics(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StatisticsView.fxml"));
            Parent statsRoot = loader.load();
            contentPane.getChildren().setAll(statsRoot);
            VBox.setVgrow(statsRoot, Priority.ALWAYS);
            
            // Apply dark mode to new content if needed
            if (isDarkMode) {
                applyDarkModeToChildren(statsRoot, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showReports(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReportsView.fxml"));
            Parent reportsRoot = loader.load();
            contentPane.getChildren().setAll(reportsRoot);
            
            // Apply dark mode to new content if needed
            if (isDarkMode) {
                applyDarkModeToChildren(reportsRoot, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void showAdminLogs(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminLogsView.fxml"));
            Parent adminLogsRoot = loader.load();
            contentPane.getChildren().setAll(adminLogsRoot);
            
            // Apply dark mode to new content if needed
            if (isDarkMode) {
                applyDarkModeToChildren(adminLogsRoot, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void showAdvancedFeatures(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdvancedFeaturesView.fxml"));
            Parent advancedFeaturesRoot = loader.load();
            contentPane.getChildren().setAll(advancedFeaturesRoot);
            
            if (isDarkMode) {
                applyDarkModeToChildren(advancedFeaturesRoot, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 