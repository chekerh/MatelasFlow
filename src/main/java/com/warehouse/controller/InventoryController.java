package com.warehouse.controller;

import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
import com.warehouse.model.User;
import com.warehouse.model.UserDAO;
import com.warehouse.util.ActivityLogger;
import com.warehouse.ui.SkeletonPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.concurrent.CompletableFuture;

public class InventoryController {
    @FXML private TableView<Mattress> mattressTable;
    @FXML private TableColumn<Mattress, String> typeColumn;
    @FXML private TableColumn<Mattress, String> sizeColumn;
    @FXML private TableColumn<Mattress, String> brandColumn;
    @FXML private TableColumn<Mattress, Integer> quantityColumn;
    @FXML private TableColumn<Mattress, Double> unitPriceColumn;
    @FXML private TableColumn<Mattress, Double> prixColumn;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Label errorLabel;

    private ObservableList<Mattress> mattressList = FXCollections.observableArrayList();
    private DashboardController dashboardController;
    private Mattress draggedMattress;
    private final SkeletonPane tableSkeleton = SkeletonPane.forTable(420, 220);
    private final Label emptyPlaceholder = new Label("Aucun matelas trouvé dans la base de données.");

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        // Use flexible resize policy for dynamic column sizing
        mattressTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        mattressTable.setFixedCellSize(56);
        
        typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        sizeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSize()));
        brandColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
            cellData.getValue().getReference()
        ));
        quantityColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        unitPriceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getUnitPrice()).asObject());
        prixColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getSalePrice()).asObject());
        
        unitPriceColumn.setCellFactory(column -> createPriceCell());
        prixColumn.setCellFactory(column -> createPriceCell());
        typeColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        sizeColumn.setStyle("-fx-alignment: CENTER;");
        brandColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        quantityColumn.setStyle("-fx-alignment: CENTER;");
        unitPriceColumn.setStyle("-fx-alignment: CENTER;");
        prixColumn.setStyle("-fx-alignment: CENTER;");
        
        // Set minimum widths for columns
        typeColumn.setMinWidth(150);
        sizeColumn.setMinWidth(120);
        brandColumn.setMinWidth(150);
        quantityColumn.setMinWidth(80);
        unitPriceColumn.setMinWidth(140);
        prixColumn.setMinWidth(140);

        mattressTable.setPlaceholder(tableSkeleton);
        mattressTable.setItems(mattressList);
        if (MattressDAO.isSortOrderSupported()) {
            enableRowReordering();
        } else {
            System.out.println("[InventoryController] L'ordre manuel est indisponible tant que la colonne sort_order n'est pas ajoutée.");
        }
        loadMattresses();
    }

    private TableCell<Mattress, Double> createPriceCell() {
        return new TableCell<Mattress, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", item));
                }
            }
        };
    }

    @FXML
    public void loadMattresses() {
        showSkeleton(true);
        CompletableFuture
            .supplyAsync(MattressDAO::getAllMattresses)
            .thenAccept(list -> Platform.runLater(() -> {
                mattressList.setAll(list);
                System.out.println("[InventoryController] Matelas chargés: " + mattressList.size());
                errorLabel.setText("");
                // Auto-resize after data is loaded
                Platform.runLater(() -> {
                    PauseTransition pause = new PauseTransition(Duration.millis(150));
                    pause.setOnFinished(e -> autoResizeColumns());
                    pause.play();
                });
                showSkeleton(false);
            }))
            .exceptionally(ex -> {
                com.warehouse.util.ErrorHandler.handleError(
                    "InventoryController.loadMattresses",
                    ex,
                    () -> {
                        String userMessage = com.warehouse.util.ErrorHandler.getUserFriendlyMessage(ex);
                        errorLabel.setText("Erreur: " + userMessage);
                        if (dashboardController != null) {
                            dashboardController.showNotification("Erreur lors du chargement des matelas: " + userMessage, true);
                        }
                    }
                );
                return null;
            });
    }

    private void showSkeleton(boolean loading) {
        if (loading) {
            mattressTable.setPlaceholder(tableSkeleton);
        } else {
            mattressTable.setPlaceholder(mattressList.isEmpty() ? emptyPlaceholder : new Label(""));
        }
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
            errorLabel.setText("Aucun matelas sélectionné.");
            return;
        }
        
        // Check if mattress has transactions (foreign key constraint will prevent deletion)
        // Show confirmation dialog
        javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.CONFIRMATION
        );
        confirmAlert.setTitle("Supprimer le matelas");
        confirmAlert.setHeaderText("Voulez-vous vraiment supprimer ce matelas ?");
        confirmAlert.setContentText(
            "Type: " + selected.getType() + "\n" +
            "Taille: " + selected.getSize() + "\n" +
            "Référence: " + selected.getReference() + "\n" +
            "Quantité: " + selected.getQuantity() + "\n\n" +
            "⚠️ Attention: Cette action est irréversible.\n" +
            "Si ce matelas a des transactions associées, la suppression sera bloquée."
        );
        confirmAlert.initOwner(mattressTable.getScene().getWindow());
        
        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == javafx.scene.control.ButtonType.OK) {
                try {
                    if (MattressDAO.deleteMattress(selected.getId())) {
                        loadMattresses();
                        if (dashboardController != null) {
                            dashboardController.showNotification("Matelas supprimé avec succès!", false);
                        }
                    } else {
                        errorLabel.setText("Échec de la suppression. Le matelas peut avoir des transactions associées.");
                        if (dashboardController != null) {
                            dashboardController.showNotification(
                                "Impossible de supprimer le matelas. Il existe probablement des transactions associées.", 
                                true
                            );
                        }
                    }
                } catch (Exception e) {
                    errorLabel.setText("Erreur lors de la suppression: " + e.getMessage());
                    if (dashboardController != null) {
                        dashboardController.showNotification(
                            "Erreur lors de la suppression: " + e.getMessage(), 
                            true
                        );
                    }
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadMattresses();
    }

    private void enableRowReordering() {
        mattressTable.setRowFactory(tv -> {
            TableRow<Mattress> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    draggedMattress = row.getItem();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(draggedMattress.getType());
                    db.setContent(content);
                    event.consume();
                }
            });

            row.setOnDragOver(event -> {
                if (draggedMattress != null && row.getItem() != null && draggedMattress != row.getItem()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    event.consume();
                }
            });

            row.setOnDragDropped(event -> handleMattressDrop(row, event));
            row.setOnDragDone(event -> draggedMattress = null);
            return row;
        });

        mattressTable.setOnDragOver(event -> {
            if (draggedMattress != null) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        mattressTable.setOnDragDropped(event -> {
            if (draggedMattress != null) {
                mattressList.remove(draggedMattress);
                mattressList.add(draggedMattress);
                persistMattressOrder();
                draggedMattress = null;
                event.setDropCompleted(true);
                event.consume();
            }
        });
    }

    private void handleMattressDrop(TableRow<Mattress> row, DragEvent event) {
        if (draggedMattress == null) {
            return;
        }
        int dropIndex = row.isEmpty() ? mattressList.size() : row.getIndex();
        mattressList.remove(draggedMattress);
        if (dropIndex > mattressList.size()) {
            dropIndex = mattressList.size();
        }
        mattressList.add(dropIndex, draggedMattress);
        mattressTable.getSelectionModel().select(draggedMattress);
        persistMattressOrder();
        draggedMattress = null;
        event.setDropCompleted(true);
        event.consume();
    }

    private void persistMattressOrder() {
        if (MattressDAO.isSortOrderSupported()) {
            MattressDAO.updateSortOrder(mattressTable.getItems());
        }
    }
    
    /**
     * Auto-resize columns to fit their content
     */
    private void autoResizeColumns() {
        if (mattressTable.getItems().isEmpty()) {
            return;
        }
        
        // Calculate optimal widths based on content
        for (TableColumn<?, ?> column : mattressTable.getColumns()) {
            if (column.isVisible()) {
                double maxWidth = column.getMinWidth();
                // Sample from items (check first 50 to avoid performance issues)
                int sampleSize = Math.min(50, mattressList.size());
                for (int i = 0; i < sampleSize; i++) {
                    Mattress m = mattressList.get(i);
                    if (m != null) {
                        String content = getColumnContent(m, column);
                        if (content != null && !content.isEmpty()) {
                            // Estimate width based on character count (rough estimate: 7-8 pixels per character)
                            double estimatedWidth = content.length() * 7.5 + 30; // Add padding
                            maxWidth = Math.max(maxWidth, estimatedWidth);
                        }
                    }
                }
                // Set pref width but respect min/max constraints
                double optimalWidth = Math.max(column.getMinWidth(), Math.min(maxWidth, 400));
                column.setPrefWidth(optimalWidth);
            }
        }
    }
    
    /**
     * Get the string content for a column
     */
    private String getColumnContent(Mattress m, TableColumn<?, ?> column) {
        if (column == typeColumn) {
            return m.getType() != null ? m.getType() : "";
        } else if (column == sizeColumn) {
            return m.getSize() != null ? m.getSize() : "";
        } else if (column == brandColumn) {
            return m.getReference() != null ? m.getReference() : "";
        } else if (column == quantityColumn) {
            return String.valueOf(m.getQuantity());
        } else if (column == unitPriceColumn) {
            return String.format("%.2f DT", m.getUnitPrice());
        } else if (column == prixColumn) {
            return String.format("%.2f DT", m.getSalePrice());
        }
        return "";
    }
} 