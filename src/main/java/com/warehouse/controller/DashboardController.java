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
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.effect.GaussianBlur;
import com.warehouse.model.User;
import com.warehouse.model.UserDAO;
import com.warehouse.util.ActivityLogger;
import com.warehouse.ui.IconFactory;
import com.warehouse.service.ThemeService;
import com.warehouse.service.QuickStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    @FXML private ImageView logoImage;
    @FXML private Label welcomeLabel;
    @FXML private Label userInfoLabel;
    @FXML private Text duaaHeadingText;
    @FXML private Text duaaText;
    @FXML private Button logoutButton;
    @FXML private Button darkModeButton;
    @FXML private Button powerButton;
    @FXML private Button inventoryButton;
    @FXML private Button transactionsButton;
    @FXML private Button ownersButton;
    @FXML private VBox navBox;
    @FXML private VBox contentPane;
    @FXML private StackPane contentBody;
    @FXML private StackPane overlayPane;
    @FXML private VBox notificationArea;
    @FXML private Label notificationLabel;
    @FXML private Button userManagementButton;
    @FXML private Button adminLogsButton;
    @FXML private Button advancedFeaturesButton;
    @FXML private Button statisticsButton;
    @FXML private Button reportsButton;
    @FXML private HBox quickActionsBar;
    @FXML private Label lowStockValueLabel;
    @FXML private Label lowStockSubLabel;
    @FXML private Label pendingReturnsValueLabel;
    @FXML private Label pendingReturnsSubLabel;
    @FXML private Label todayRevenueValueLabel;
    @FXML private Label todayNetProfitValueLabel;
    
    private String currentUser;
    private String currentRole;
    private boolean isDarkMode = false;
    private boolean highContrastMode = false;
    private final GaussianBlur navBlur = new GaussianBlur(0);
    private final GaussianBlur contentBlur = new GaussianBlur(0);
    private Timeline quickStatsTimeline;
    
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
            logger.warn("Logo not found: {}", e.getMessage());
            // Try white logo as fallback
            try {
                Image logo = new Image(getClass().getResource("/images/SuperMousse.jpg").toExternalForm());
                logoImage.setImage(logo);
            } catch (Exception e2) {
                logger.warn("White logo also not found: {}", e2.getMessage());
            }
        }
        
        // Configure duaa banner for RTL display
        configureDuaaBanner();

        // Set up notification area
        notificationArea.setVisible(false);
        notificationArea.setManaged(false);
        
        // Apply dark mode if needed
        ThemeService.applyDarkModeToChildren(contentPane, isDarkMode);
        
        // Load default content: statistics page
        showStatistics(null);
        
        // Start dynamic duaa rotation
        startDuaaRotation();
        
        // Load and apply saved theme preference
        ThemeService.applySavedThemeToNode(contentPane);
        
        // Load and apply saved dark mode preference
        isDarkMode = com.warehouse.util.ThemePreferences.loadDarkMode();
        if (isDarkMode) {
            darkModeButton.setText("☀️ Mode Clair");
            ThemeService.applyDarkModeToChildren(contentPane, true);
            ThemeService.applyDarkModeToChildren(navBox, true);
            Scene scene = contentPane.getScene();
            if (scene != null) {
                ThemeService.toggleDarkModeOnScene(scene, true);
            }
        }

        navBox.setEffect(navBlur);
        contentPane.setEffect(contentBlur);

        setupNavIcons();
        setupResponsiveObserver();
        refreshQuickStats();
        startQuickStatsRefresher();
    }
    
    private void setupNavIcons() {
        setButtonIcon(inventoryButton, "shippingbox");
        setButtonIcon(transactionsButton, "creditcard");
        setButtonIcon(ownersButton, "person");
        setButtonIcon(userManagementButton, "person2");
        setButtonIcon(statisticsButton, "chartbar");
        setButtonIcon(reportsButton, "doctext");
        setButtonIcon(adminLogsButton, "power");
        setButtonIcon(advancedFeaturesButton, "plus");
    }

    private void setButtonIcon(Button button, String iconName) {
        if (button == null) {
            return;
        }
        var icon = IconFactory.svg(iconName, 16);
        button.setGraphic(icon);
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setGraphicTextGap(10);
    }

    private void setupResponsiveObserver() {
        contentPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                registerResponsiveListeners(newScene);
                updateResponsiveMode(newScene.getWidth());
                if (highContrastMode) {
                    newScene.getRoot().getStyleClass().add("high-contrast");
                }
            }
        });
    }

    private void registerResponsiveListeners(Scene scene) {
        scene.widthProperty().addListener((o, oldVal, newVal) -> updateResponsiveMode(newVal.doubleValue()));
    }

    private void updateResponsiveMode(double width) {
        Scene scene = contentPane.getScene();
        if (scene == null) {
            return;
        }
        var root = scene.getRoot();
        if (width < 1280) {
            if (!root.getStyleClass().contains("compact")) {
                root.getStyleClass().add("compact");
            }
        } else {
            root.getStyleClass().remove("compact");
        }
    }

    private void startQuickStatsRefresher() {
        quickStatsTimeline = new Timeline(
            new KeyFrame(Duration.minutes(2), e -> refreshQuickStats())
        );
        quickStatsTimeline.setCycleCount(Animation.INDEFINITE);
        quickStatsTimeline.play();
    }

    private void refreshQuickStats() {
        QuickStatsService.refreshAndUpdate(
            quickActionsBar,
            lowStockValueLabel,
            lowStockSubLabel,
            pendingReturnsValueLabel,
            pendingReturnsSubLabel,
            todayRevenueValueLabel,
            todayNetProfitValueLabel
        );
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
            logger.warn("Impossible de charger le fichier des invocations: {}", e.getMessage());
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
            logger.error("Error during logout", e);
        }
    }

    @FXML
    private void toggleDarkMode(ActionEvent event) {
        isDarkMode = !isDarkMode;
        darkModeButton.setText(isDarkMode ? "☀️ Mode Clair" : "🌙 Mode Sombre");
        
        // Save dark mode preference
        com.warehouse.util.ThemePreferences.saveDarkMode(isDarkMode);
        
        // Apply dark mode to all main containers
        ThemeService.applyDarkModeToChildren(contentPane, isDarkMode);
        ThemeService.applyDarkModeToChildren(navBox, isDarkMode);
        ThemeService.applyDarkModeToChildren(notificationArea, isDarkMode);
        
        // Apply dark mode to current content if exists
        if (contentBody != null && !contentBody.getChildren().isEmpty()) {
            Parent currentContent = (Parent) contentBody.getChildren().get(0);
            ThemeService.applyDarkModeToChildren(currentContent, isDarkMode);
        }
        
        // Apply dark mode to the root scene
        Scene scene = contentPane.getScene();
        if (scene != null) {
            ThemeService.toggleDarkModeOnScene(scene, isDarkMode);
            
            // Force refresh by reapplying styles
            javafx.application.Platform.runLater(() -> {
                if (isDarkMode) {
                    scene.getRoot().getStyleClass().add("dark-mode");
                } else {
                    scene.getRoot().getStyleClass().remove("dark-mode");
                }
                // Trigger style refresh
                scene.getRoot().setStyle(scene.getRoot().getStyle() + " ");
            });
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
            logger.warn("Logo not found for dark mode: {}", e.getMessage());
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
        toggleBackgroundBlur(true);
        
        if (isDarkMode) {
            ThemeService.applyDarkModeToChildren(overlayContent, true);
        }
    }

    public void hideOverlay() {
        overlayPane.setVisible(false);
        overlayPane.setManaged(false);
        overlayPane.getChildren().clear();
        toggleBackgroundBlur(false);
    }

    // Navigation methods
    @FXML
    private void showInventory(ActionEvent event) {
        loadSection("/fxml/InventoryView.fxml", controller -> {
            if (controller instanceof InventoryController inventoryController) {
                inventoryController.setDashboardController(this);
            }
        });
    }
    
    /**
     * Refreshes the inventory view if it's currently loaded
     */
    public void refreshInventory() {
        // Find and refresh inventory controller if loaded
        if (contentBody != null && !contentBody.getChildren().isEmpty()) {
            Parent currentContent = (Parent) contentBody.getChildren().get(0);
            Object controller = currentContent.getUserData();
            if (controller instanceof InventoryController) {
                ((InventoryController) controller).loadMattresses();
            }
        }
    }

    @FXML
    private void showTransactions(ActionEvent event) {
        loadSection("/fxml/TransactionsView.fxml", controller -> {
            if (controller instanceof TransactionsController transactionsController) {
                transactionsController.setDashboardController(this);
            }
        });
    }

    @FXML
    private void showStoreOwners(ActionEvent event) {
        loadSection("/fxml/StoreOwnersView.fxml", controller -> {
            if (controller instanceof StoreOwnersController ownersController) {
                ownersController.setDashboardController(this);
            }
        });
    }

    @FXML
    private void showUserManagement(ActionEvent event) {
        loadSection("/fxml/UserManagementView.fxml", controller -> {
            if (controller instanceof UserManagementController userManagementController) {
                userManagementController.setDashboardController(this);
            }
        });
    }

    @FXML
    private void showStatistics(ActionEvent event) {
        loadSection("/fxml/StatisticsView.fxml", null);
    }

    @FXML
    private void showReports(ActionEvent event) {
        loadSection("/fxml/ReportsView.fxml", null);
    }
    
    @FXML
    private void showAdminLogs(ActionEvent event) {
        loadSection("/fxml/AdminLogsView.fxml", null);
    }
    
    @FXML
    private void showAdvancedFeatures(ActionEvent event) {
        loadSection("/fxml/AdvancedFeaturesView.fxml", null);
    }

    private void loadSection(String fxmlPath, Consumer<Object> controllerConfigurator) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            if (controllerConfigurator != null) {
                controllerConfigurator.accept(loader.getController());
            }
            ThemeService.applyThemeToNode(root, isDarkMode);
            displayContent(root);
            refreshQuickStats();
        } catch (Exception e) {
            logger.error("Error loading section: {}", fxmlPath, e);
        }
    }

    private void displayContent(Parent newContent) {
        if (contentBody == null) {
            return;
        }
        newContent.setOpacity(0);
        newContent.setTranslateY(18);
        contentBody.getChildren().setAll(newContent);
        VBox.setVgrow(contentBody, Priority.ALWAYS);

        FadeTransition fade = new FadeTransition(Duration.millis(240), newContent);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(320), newContent);
        slide.setFromY(24);
        slide.setToY(0);

        ParallelTransition parallelTransition = new ParallelTransition(fade, slide);
        parallelTransition.setInterpolator(Interpolator.EASE_BOTH);
        parallelTransition.play();
    }

    private void toggleBackgroundBlur(boolean enable) {
        double target = enable ? 18 : 0;
        Timeline blurTimeline = new Timeline(
            new KeyFrame(Duration.millis(220),
                new KeyValue(navBlur.radiusProperty(), target, Interpolator.EASE_BOTH),
                new KeyValue(contentBlur.radiusProperty(), target, Interpolator.EASE_BOTH)
            )
        );
        blurTimeline.play();
    }

} 