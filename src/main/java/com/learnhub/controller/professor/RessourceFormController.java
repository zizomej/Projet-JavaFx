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
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) tfTitre.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSave() {
        if (tfTitre.getText().isEmpty() || selectedFileUrl.isEmpty() || currentModule == null) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs obligatoires.");
            a.show();
            return;
        }

        Ressource r = currentRessource == null ? new Ressource() : currentRessource;
        r.setTitre(tfTitre.getText());
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
