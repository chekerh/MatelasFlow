package com.warehouse.controller;

import com.warehouse.model.StoreOwner;
import com.warehouse.model.StoreOwnerDAO;
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
    @FXML private TableColumn<StoreOwner, Integer> idColumn;
    @FXML private TableColumn<StoreOwner, String> nameColumn;
    @FXML private TableColumn<StoreOwner, String> contactColumn;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Label errorLabel;

    private ObservableList<StoreOwner> storeOwnerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        contactColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContact()));
        storeOwnerTable.setItems(storeOwnerList);
        loadStoreOwners();
    }

    @FXML
    private void loadStoreOwners() {
        storeOwnerList.setAll(StoreOwnerDAO.getAllStoreOwners());
        errorLabel.setText("");
    }

    @FXML
    private void handleAdd() {
        showStoreOwnerDialog(null);
    }

    @FXML
    private void handleEdit() {
        StoreOwner selected = storeOwnerTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showStoreOwnerDialog(selected);
        } else {
            errorLabel.setText("No store owner selected.");
        }
    }

    @FXML
    private void handleDelete() {
        StoreOwner selected = storeOwnerTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (StoreOwnerDAO.deleteStoreOwner(selected.getId())) {
                loadStoreOwners();
            } else {
                errorLabel.setText("Failed to delete store owner.");
            }
        } else {
            errorLabel.setText("No store owner selected.");
        }
    }

    @FXML
    private void handleRefresh() {
        loadStoreOwners();
    }

    private void showStoreOwnerDialog(StoreOwner owner) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StoreOwnerDialog.fxml"));
            Parent dialogRoot = loader.load();
            StoreOwnerDialogController controller = loader.getController();
            if (owner != null) controller.setStoreOwner(owner);
            Stage dialogStage = new Stage();
            dialogStage.setTitle(owner == null ? "Add Store Owner" : "Edit Store Owner");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();
            if (controller.isOkClicked()) {
                StoreOwner edited = controller.getStoreOwner();
                boolean success = owner == null ? StoreOwnerDAO.addStoreOwner(edited) : StoreOwnerDAO.updateStoreOwner(edited);
                if (success) {
                    loadStoreOwners();
                } else {
                    errorLabel.setText("Failed to save store owner.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error opening dialog.");
        }
    }
} 