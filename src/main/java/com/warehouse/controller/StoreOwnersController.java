package com.warehouse.controller;

import com.warehouse.model.StoreOwner;
import com.warehouse.model.StoreOwnerDAO;
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

public class StoreOwnersController {
    @FXML private TableView<StoreOwner> storeOwnerTable;
    @FXML private TableColumn<StoreOwner, String> nameColumn;
    @FXML private TableColumn<StoreOwner, String> contactColumn;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Label errorLabel;

    private ObservableList<StoreOwner> storeOwnerList = FXCollections.observableArrayList();
    private DashboardController dashboardController;

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        // Set up table columns
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        contactColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContact()));
        storeOwnerTable.setItems(storeOwnerList);
        loadStoreOwners();
    }

    @FXML
    public void loadStoreOwners() {
        storeOwnerList.setAll(StoreOwnerDAO.getAllStoreOwners());
        errorLabel.setText("");
    }

    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StoreOwnerOverlay.fxml"));
            Parent overlayRoot = loader.load();
            StoreOwnerOverlayController controller = loader.getController();
            
            // Set up the controller
            controller.setDashboardController(dashboardController);
            controller.setStoreOwnersController(this);
            controller.setStoreOwner(null); // Add mode
            
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
        StoreOwner selected = storeOwnerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            errorLabel.setText("Aucun propriétaire sélectionné.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StoreOwnerOverlay.fxml"));
            Parent overlayRoot = loader.load();
            StoreOwnerOverlayController controller = loader.getController();
            
            // Set up the controller
            controller.setDashboardController(dashboardController);
            controller.setStoreOwnersController(this);
            controller.setStoreOwner(selected); // Edit mode
            
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
        StoreOwner selected = storeOwnerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            errorLabel.setText("❌ Aucun propriétaire sélectionné.");
            errorLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        // Check if store owner has transactions
        System.out.println("🔍 Vérification des transactions pour le propriétaire ID: " + selected.getId());
        int transactionCount = TransactionDAO.getTransactionCountForStoreOwner(selected.getId());
        System.out.println("📊 Nombre de transactions trouvées: " + transactionCount);
        
        if (transactionCount > 0) {
            // Cannot delete - has transactions
            Alert warningAlert = new Alert(Alert.AlertType.WARNING);
            warningAlert.setTitle("Suppression impossible");
            warningAlert.setHeaderText("Ce propriétaire ne peut pas être supprimé");
            warningAlert.setContentText(
                "Ce propriétaire a " + transactionCount + " transaction(s) associée(s).\\n\\n" +
                "📋 Détails du propriétaire:\\n" +
                "• Nom: " + selected.getName() + "\\n" +
                "• Contact: " + selected.getContact() + "\\n\\n" +
                "❌ Vous ne pouvez pas supprimer un propriétaire qui a des transactions.\\n\\n" +
                "💡 Solution:\\n" +
                "Si vous voulez vraiment supprimer ce propriétaire, vous devez d'abord:\\n" +
                "1. Supprimer toutes les transactions associées dans la vue Transactions\\n" +
                "2. Ou modifier ces transactions pour utiliser un autre propriétaire\\n\\n" +
                "⚠️ Cette restriction protège l'intégrité de vos données."
            );
            warningAlert.showAndWait();
            
            errorLabel.setText("⚠️ Impossible de supprimer: " + transactionCount + " transaction(s) associée(s).");
            errorLabel.setStyle("-fx-text-fill: orange;");
            System.out.println("⚠️ Suppression refusée: Le propriétaire a " + transactionCount + " transactions");
            return;
        }
        
        // No transactions - safe to delete, ask for confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer ce propriétaire ?");
        confirmAlert.setContentText(
            "Nom: " + selected.getName() + "\\n" +
            "Contact: " + selected.getContact() + "\\n\\n" +
            "✅ Aucune transaction associée\\n" +
            "⚠️ Cette action est irréversible !"
        );
        
        // Show confirmation and wait for response
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // User confirmed - proceed with deletion
                System.out.println("🗑️ Suppression du propriétaire ID: " + selected.getId() + " - " + selected.getName());
                
                if (StoreOwnerDAO.deleteStoreOwner(selected.getId())) {
                    loadStoreOwners();
                    errorLabel.setText("✅ Propriétaire supprimé avec succès !");
                    errorLabel.setStyle("-fx-text-fill: green;");
                    System.out.println("✅ Propriétaire supprimé avec succès");
                } else {
                    errorLabel.setText("❌ Échec de la suppression du propriétaire.");
                    errorLabel.setStyle("-fx-text-fill: red;");
                    System.err.println("❌ Échec de la suppression du propriétaire");
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
        loadStoreOwners();
    }
} 