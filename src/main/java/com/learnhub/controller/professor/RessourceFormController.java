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
        cbType.getSelectionModel().selectFirst();
    }

    public void setModule(Module module) {
        this.currentModule = module;
        if (module != null && moduleNameLabel != null)
            moduleNameLabel.setText("Module: " + module.getCode() + " - " + module.getIntitule());
    }

    public void setRessource(Ressource ressource) {
        this.currentRessource = ressource;
        if (ressource != null) {
            tfTitre.setText(ressource.getTitre());
            cbType.setValue(ressource.getType());
            fileNameLabel.setText(ressource.getUrl());
            selectedFileUrl = ressource.getUrl() != null ? ressource.getUrl() : "";
            chkPublic.setSelected(ressource.isEstPublic());
        } else {
            chkPublic.setSelected(true);
        }
    }

    public void setOnSuccess(Runnable onSuccess) { this.onSuccess = onSuccess; }

    @FXML
    private void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier");
        Stage stage = (Stage) tfTitre.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedFileUrl = file.getAbsolutePath();
            fileNameLabel.setText(file.getName());
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) tfTitre.getScene().getWindow()).close();
    }

    @FXML
    private void handleSave() {
        if (tfTitre.getText().isEmpty() || selectedFileUrl.isEmpty() || currentModule == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs obligatoires.").show();
            return;
        }
        Ressource r = currentRessource == null ? new Ressource() : currentRessource;
        r.setTitre(tfTitre.getText().trim());
        r.setType(cbType.getValue());
        r.setUrl(selectedFileUrl);
        r.setEstPublic(chkPublic.isSelected());
        r.setModuleId(currentModule.getId());
        try {
            if (r.getId() == 0) ressourceDAO.add(r); else ressourceDAO.update(r);
            if (onSuccess != null) onSuccess.run();
            handleClose();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).show();
        }
    }
}
