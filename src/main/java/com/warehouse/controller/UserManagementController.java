package com.warehouse.controller;

import com.warehouse.model.User;
import com.warehouse.model.UserDAO;
import com.warehouse.model.TransactionDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UserManagementController {
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Label errorLabel;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        // Set up table columns
        usernameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        roleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRole()));
        userTable.setItems(userList);
        loadUsers();
    }

    @FXML
    public void loadUsers() {
        userList.setAll(UserDAO.getAllUsers());
        errorLabel.setText("");
    }

    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserOverlay.fxml"));
            Parent overlayRoot = loader.load();
            UserOverlayController controller = loader.getController();
            
            // Set up the controller
            controller.setDashboardController(dashboardController);
            controller.setUserManagementController(this);
            controller.setUser(null); // Add mode
            
            // Show the overlay
            if (dashboardController != null) {
                dashboardController.showOverlay(overlayRoot);
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de l'ouverture du dialogue d'ajout.");
        }
    }

    @FXML
    private void handleEdit() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            errorLabel.setText("Aucun utilisateur sélectionné.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserOverlay.fxml"));
            Parent overlayRoot = loader.load();
            UserOverlayController controller = loader.getController();
            
            // Set up the controller
            controller.setDashboardController(dashboardController);
            controller.setUserManagementController(this);
            controller.setUser(selected); // Edit mode
            
            // Show the overlay
            if (dashboardController != null) {
                dashboardController.showOverlay(overlayRoot);
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Erreur lors de l'ouverture du dialogue de modification.");
        }
    }

    @FXML
    private void handleDelete() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            errorLabel.setText("❌ Aucun utilisateur sélectionné.");
            errorLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        // Check if user has created transactions
        System.out.println("🔍 Vérification des transactions pour l'utilisateur ID: " + selected.getId());
        int transactionCount = TransactionDAO.getTransactionCountForUser(selected.getId());
        System.out.println("📊 Nombre de transactions trouvées: " + transactionCount);
        
        if (transactionCount > 0) {
            // Cannot delete - has transactions
            Alert warningAlert = new Alert(Alert.AlertType.WARNING);
            warningAlert.setTitle("Suppression impossible");
            warningAlert.setHeaderText("Cet utilisateur ne peut pas être supprimé");
            warningAlert.setContentText(
                "Cet utilisateur a créé " + transactionCount + " transaction(s).\\n\\n" +
                "📋 Détails de l'utilisateur:\\n" +
                "• Nom d'utilisateur: " + selected.getUsername() + "\\n" +
                "• Rôle: " + selected.getRole() + "\\n\\n" +
                "❌ Vous ne pouvez pas supprimer un utilisateur qui a créé des transactions.\\n\\n" +
                "💡 Solution:\\n" +
                "Si vous voulez vraiment supprimer cet utilisateur, vous devez d'abord:\\n" +
                "1. Supprimer toutes les transactions associées dans la vue Transactions\\n" +
                "2. Ou réassigner ces transactions à un autre utilisateur (modification manuelle de la base de données)\\n\\n" +
                "⚠️ Cette restriction protège l'intégrité de vos données et l'historique des transactions."
            );
            warningAlert.showAndWait();
            
            errorLabel.setText("⚠️ Impossible de supprimer: " + transactionCount + " transaction(s) créée(s) par cet utilisateur.");
            errorLabel.setStyle("-fx-text-fill: orange;");
            System.out.println("⚠️ Suppression refusée: L'utilisateur a " + transactionCount + " transactions");
            return;
        }
        
        // No transactions - safe to delete, ask for confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer cet utilisateur ?");
        confirmAlert.setContentText(
            "Nom d'utilisateur: " + selected.getUsername() + "\\n" +
            "Rôle: " + selected.getRole() + "\\n\\n" +
            "✅ Aucune transaction créée par cet utilisateur\\n" +
            "⚠️ Cette action est irréversible !"
        );
        
        // Show confirmation and wait for response
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // User confirmed - proceed with deletion
                System.out.println("🗑️ Suppression de l'utilisateur ID: " + selected.getId() + " - " + selected.getUsername());
                
                if (UserDAO.deleteUser(selected.getId())) {
                    loadUsers();
                    errorLabel.setText("✅ Utilisateur supprimé avec succès !");
                    errorLabel.setStyle("-fx-text-fill: green;");
                    System.out.println("✅ Utilisateur supprimé avec succès");
                } else {
                    errorLabel.setText("❌ Échec de la suppression de l'utilisateur.");
                    errorLabel.setStyle("-fx-text-fill: red;");
                    System.err.println("❌ Échec de la suppression de l'utilisateur");
                }
            } else {
                // User cancelled
                errorLabel.setText("ℹ️ Suppression annulée.");
                errorLabel.setStyle("-fx-text-fill: blue;");
                System.out.println("ℹ️ Suppression annulée par l'utilisateur");
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }
} 