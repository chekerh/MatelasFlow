package com.warehouse.controller;

import com.warehouse.model.Mattress;
import com.warehouse.model.MattressDAO;
import com.warehouse.model.User;
import com.warehouse.model.UserDAO;
import com.warehouse.util.ActivityLogger;
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

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    public void initialize() {
        mattressTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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

        mattressTable.setPlaceholder(new Label("Aucun matelas trouvé dans la base de données."));
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
        mattressList.setAll(MattressDAO.getAllMattresses());
        System.out.println("[InventoryController] Matelas chargés: " + mattressList.size());
        if (!mattressList.isEmpty()) {
            Mattress sample = mattressList.get(0);
            System.out.println("[InventoryController] Exemple -> type=" + sample.getType() + ", taille=" + sample.getSize());
        }
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
        if (selected != null) {
            if (MattressDAO.deleteMattress(selected.getId())) {
                loadMattresses();
            } else {
                errorLabel.setText("Échec de la suppression du matelas.");
            }
        } else {
            errorLabel.setText("Aucun matelas sélectionné.");
        }
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
} 