package com.learnhub.controller.professor;

import com.learnhub.dao.RessourceDAO;
import com.learnhub.models.Module;
import com.learnhub.models.Ressource;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;

public class RessourceFormController {

    @FXML private Label moduleNameLabel;
    @FXML private TextField tfTitre;
    @FXML private ComboBox<String> cbType;
    @FXML private Label fileNameLabel;
    @FXML private CheckBox chkPublic;

    private Module currentModule;
    private Ressource currentRessource;
    private Runnable onSuccess;
    private final RessourceDAO ressourceDAO = new RessourceDAO();
    private String selectedFileUrl = "";

    @FXML
    public void initialize() {
        cbType.setItems(FXCollections.observableArrayList("PDF", "DOC", "VIDEO", "IMAGE", "ZIP"));
        if (cbType.getItems().size() > 0) cbType.getSelectionModel().selectFirst();

        // Contrôle de saisie : limiter la saisie à 100 caractères maximum
        tfTitre.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 100) {
                tfTitre.setText(oldValue);
            }
        });

        tfTitre.textProperty().addListener((obs, old, newVal) -> clearError(tfTitre));
        cbType.valueProperty().addListener((obs, old, newVal) -> clearError(cbType));
    }

    public void setModule(Module module) {
        this.currentModule = module;
        if (module != null) {
            moduleNameLabel.setText("Module: " + module.getCode() + " - " + module.getIntitule());
        }
    }

    public void setRessource(Ressource ressource) {
        this.currentRessource = ressource;
        if (ressource != null) {
            tfTitre.setText(ressource.getTitre());
            cbType.setValue(ressource.getType());
            fileNameLabel.setText(ressource.getUrl());
            selectedFileUrl = ressource.getUrl();
            chkPublic.setSelected(ressource.isEstPublic());
        } else {
            chkPublic.setSelected(true);
        }
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    @FXML
    private void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier");
        Stage stage = (Stage) tfTitre.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedFileUrl = file.getName();
            fileNameLabel.setText(selectedFileUrl);
            clearError(fileNameLabel);
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) tfTitre.getScene().getWindow();
        stage.close();
    }

    private void showError(javafx.scene.Node node, String message) {
        javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) node.getParent();
        javafx.scene.layout.Pane targetParent = parent;
        
        if (parent instanceof javafx.scene.layout.HBox && parent.getParent() instanceof javafx.scene.layout.VBox) {
            targetParent = (javafx.scene.layout.Pane) parent.getParent();
            if (!parent.getStyle().contains("#ef4444")) {
                parent.setStyle(parent.getStyle() + "; -fx-border-color: #ef4444; -fx-border-width: 1px;");
            }
        } else {
            if (!node.getStyle().contains("#ef4444")) {
                node.setStyle(node.getStyle() + "; -fx-border-color: #ef4444; -fx-border-width: 1px;");
            }
        }
        
        Label errorLabel = new Label("• " + message);
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px;");
        errorLabel.getStyleClass().add("error-label");
        targetParent.getChildren().add(errorLabel);
    }

    private void clearError(javafx.scene.Node node) {
        javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) node.getParent();
        javafx.scene.layout.Pane targetParent = parent;
        
        if (parent instanceof javafx.scene.layout.HBox && parent.getParent() instanceof javafx.scene.layout.VBox) {
            targetParent = (javafx.scene.layout.Pane) parent.getParent();
            if (parent.getStyle().contains("#ef4444")) {
                parent.setStyle(parent.getStyle().replace("; -fx-border-color: #ef4444; -fx-border-width: 1px;", ""));
            }
        } else {
            if (node.getStyle().contains("#ef4444")) {
                node.setStyle(node.getStyle().replace("; -fx-border-color: #ef4444; -fx-border-width: 1px;", ""));
            }
        }
        
        targetParent.getChildren().removeIf(n -> n instanceof Label && n.getStyleClass().contains("error-label"));
    }

    private void clearAllErrors() {
        clearError(tfTitre);
        clearError(cbType);
        clearError(fileNameLabel);
    }

    @FXML
    private void handleSave() {
        clearAllErrors();
        boolean hasError = false;

        if (tfTitre.getText() == null || tfTitre.getText().trim().isEmpty()) { 
            showError(tfTitre, "Le titre de la ressource est obligatoire."); 
            hasError = true; 
        }
        if (cbType.getValue() == null) {
            showError(cbType, "Le type de ressource est obligatoire.");
            hasError = true;
        }
        if (selectedFileUrl.isEmpty()) { 
            showError(fileNameLabel, "Veuillez sélectionner un fichier à uploader."); 
            hasError = true; 
        }

        if (hasError) {
            return;
        }

        Ressource r = currentRessource == null ? new Ressource() : currentRessource;
        r.setTitre(tfTitre.getText().trim());
        r.setType(cbType.getValue());
        r.setUrl(selectedFileUrl);
        r.setEstPublic(chkPublic.isSelected());
        r.setModuleId(currentModule.getId());

        try {
            if (r.getId() == 0) {
                ressourceDAO.add(r);
            } else {
                ressourceDAO.update(r);
            }
            if (onSuccess != null) onSuccess.run();
            handleClose();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur lors de la sauvegarde : " + e.getMessage()).show();
        }
    }
}
