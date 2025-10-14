package com.warehouse.controller;

import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
import com.warehouse.model.TransactionDAO;
import com.warehouse.model.User;
import com.warehouse.model.UserDAO;
import com.warehouse.util.ActivityLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class InventoryController {
    @FXML private TableView<Mattress> mattressTable;
    @FXML private TableColumn<Mattress, String> typeColumn;
    @FXML private TableColumn<Mattress, String> sizeColumn;
    @FXML private TableColumn<Mattress, String> brandColumn;
    @FXML private TableColumn<Mattress, Integer> quantityColumn;
    @FXML private TableColumn<Mattress, Double> prixColumn;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Label errorLabel;

    private ObservableList<Mattress> mattressList = FXCollections.observableArrayList();
    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        sizeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSize()));
        brandColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getBrand()));
        quantityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        prixColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrix()).asObject());
        
        // Format prix column to show 2 decimals with DT currency
        prixColumn.setCellFactory(column -> new TableCell<Mattress, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", item));
                }
            }
        });
        mattressTable.setItems(mattressList);
        loadMattresses();
    }

    @FXML
    public void loadMattresses() {
        mattressList.setAll(MattressDAO.getAllMattresses());
        errorLabel.setText("");
    }

    @FXML
    private void handleAdd() {
        try {
            // Log activity
            if (dashboardController != null) {
                User currentUser = UserDAO.findByUsername(dashboardController.getCurrentUser());
                if (currentUser != null) {
                    ActivityLogger.logActivity(currentUser, ActivityLogger.ActivityType.ADD_MATTRESS, 
                        "Ouverture de l'interface d'ajout de matelas");
                }
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MattressOverlay.fxml"));
            Parent overlayRoot = loader.load();
            MattressOverlayController controller = loader.getController();
            
            // Set up the controller
            controller.setDashboardController(dashboardController);
            controller.setInventoryController(this);
            controller.setMattress(null); // Add mode
            
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
        Mattress selected = mattressTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            errorLabel.setText("Aucun matelas sélectionné.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MattressOverlay.fxml"));
            Parent overlayRoot = loader.load();
            MattressOverlayController controller = loader.getController();
            
            // Set up the controller
            controller.setDashboardController(dashboardController);
            controller.setInventoryController(this);
            controller.setMattress(selected); // Edit mode
            
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
        Mattress selected = mattressTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            errorLabel.setText("❌ Aucun matelas sélectionné.");
            errorLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        // Check if mattress has transactions
        System.out.println("🔍 Vérification des transactions pour le matelas ID: " + selected.getId());
        int transactionCount = TransactionDAO.getTransactionCountForMattress(selected.getId());
        System.out.println("📊 Nombre de transactions trouvées: " + transactionCount);
        
        if (transactionCount > 0) {
            // Cannot delete - has transactions
            Alert warningAlert = new Alert(Alert.AlertType.WARNING);
            warningAlert.setTitle("Suppression impossible");
            warningAlert.setHeaderText("Ce matelas ne peut pas être supprimé");
            warningAlert.setContentText(
                "Ce matelas a " + transactionCount + " transaction(s) associée(s).\n\n" +
                "📋 Détails du matelas:\n" +
                "• Type: " + selected.getType() + "\n" +
                "• Taille: " + selected.getSize() + "\n" +
                "• Marque: " + selected.getBrand() + "\n\n" +
                "❌ Vous ne pouvez pas supprimer un matelas qui a des transactions.\n\n" +
                "💡 Solution:\n" +
                "Si vous voulez vraiment supprimer ce matelas, vous devez d'abord:\n" +
                "1. Supprimer toutes les transactions associées dans la vue Transactions\n" +
                "2. Ou modifier ces transactions pour utiliser un autre matelas\n\n" +
                "⚠️ Cette restriction protège l'intégrité de vos données."
            );
            warningAlert.showAndWait();
            
            errorLabel.setText("⚠️ Impossible de supprimer: " + transactionCount + " transaction(s) associée(s).");
            errorLabel.setStyle("-fx-text-fill: orange;");
            System.out.println("⚠️ Suppression refusée: Le matelas a " + transactionCount + " transactions");
            return;
        }
        
        // No transactions - safe to delete, ask for confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer ce matelas ?");
        confirmAlert.setContentText(
            "Type: " + selected.getType() + "\n" +
            "Taille: " + selected.getSize() + "\n" +
            "Marque: " + selected.getBrand() + "\n" +
            "Quantité: " + selected.getQuantity() + "\n\n" +
            "✅ Aucune transaction associée\n" +
            "⚠️ Cette action est irréversible !"
        );
        
        // Show confirmation and wait for response
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // User confirmed - proceed with deletion
                System.out.println("🗑️ Suppression du matelas ID: " + selected.getId() + " - " + selected.getType());
                
                if (MattressDAO.deleteMattress(selected.getId())) {
                    // Log activity
                    if (dashboardController != null) {
                        User currentUser = UserDAO.findByUsername(dashboardController.getCurrentUser());
                        if (currentUser != null) {
                            ActivityLogger.logActivity(currentUser, ActivityLogger.ActivityType.DELETE_MATTRESS, 
                                "Supprimé: " + selected.getType() + " " + selected.getSize() + " (" + selected.getBrand() + ")");
                        }
                    }
                    
                    loadMattresses();
                    errorLabel.setText("✅ Matelas supprimé avec succès !");
                    errorLabel.setStyle("-fx-text-fill: green;");
                    System.out.println("✅ Matelas supprimé avec succès");
                } else {
                    errorLabel.setText("❌ Échec de la suppression du matelas.");
                    errorLabel.setStyle("-fx-text-fill: red;");
                    System.err.println("❌ Échec de la suppression du matelas");
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
        loadMattresses();
    }
} 