package com.learnhub.controller.professor;

import com.learnhub.dao.ModuleDAO;
import com.learnhub.models.Module;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class ModuleFormController {

    @FXML private TextField tfCode;
    @FXML private ComboBox<String> cbFiliere;
    @FXML private TextField tfIntitule;
    @FXML private TextField tfSemestre;
    @FXML private TextField tfCredits;
    @FXML private ComboBox<String> cbResponsable;

    private Module currentModule;
    private Runnable onSuccess;
    private final ModuleDAO moduleDAO = new ModuleDAO();
    private final com.learnhub.dao.FiliereDAO filiereDAO = new com.learnhub.dao.FiliereDAO();
    private final com.learnhub.dao.UtilisateurDAO utilisateurDAO = new com.learnhub.dao.UtilisateurDAO();

    private java.util.List<com.learnhub.models.Filiere> filiereList = new java.util.ArrayList<>();
    private java.util.List<com.learnhub.models.Utilisateur> professeurList = new java.util.ArrayList<>();

    @FXML
    public void initialize() {
        try {
            filiereList = filiereDAO.findAll();
            for (com.learnhub.models.Filiere f : filiereList) {
                cbFiliere.getItems().add(f.getNom());
            }

            // Using "ROLE_PROFESSOR" or "professor" based on what is in database. Let's do findAll for now and filter, or just use findByRole.
            // In typical setup, role is "PROFESSOR" or "ROLE_PROFESSOR". I'll use findByRole("professeur").
            professeurList = utilisateurDAO.findByRole("professeur");
            if (professeurList.isEmpty()) {
                professeurList = utilisateurDAO.findByRole("PROFESSOR");
            }
            if (professeurList.isEmpty()) {
                professeurList = utilisateurDAO.findByRole("ROLE_PROFESSOR");
            }
            for (com.learnhub.models.Utilisateur p : professeurList) {
                cbResponsable.getItems().add("Prof. " + p.getNom() + " " + p.getPrenom());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setModule(Module module) {
        this.currentModule = module;
        if (module != null) {
            tfCode.setText(module.getCode());
            tfIntitule.setText(module.getIntitule());
            tfSemestre.setText(String.valueOf(module.getSemestre()));
            tfCredits.setText(String.valueOf(module.getCredits()));

            for (int i = 0; i < filiereList.size(); i++) {
                if (filiereList.get(i).getId() == module.getFiliere_id()) {
                    cbFiliere.getSelectionModel().select(i);
                    break;
                }
            }

            for (int i = 0; i < professeurList.size(); i++) {
                if (professeurList.get(i).getId() == module.getResponsable_id()) {
                    cbResponsable.getSelectionModel().select(i);
                    break;
                }
            }
        }
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) tfCode.getScene().getWindow();
        stage.close();
    }


    @FXML
    private void handleSave() {

        if (tfCode.getText().isEmpty() ||
                tfIntitule.getText().isEmpty() ||
                tfSemestre.getText().isEmpty() ||
                tfCredits.getText().isEmpty() ||
                cbResponsable.getValue() == null ||
                cbFiliere.getValue() == null) {

            new Alert(Alert.AlertType.WARNING,
                    "Veuillez remplir tous les champs.").show();
            return;
        }

        Module m = currentModule == null ? new Module() : currentModule;

        m.setCode(tfCode.getText());
        m.setIntitule(tfIntitule.getText());

        m.setSemestre(Integer.parseInt(tfSemestre.getText()));
        m.setCredits(Integer.parseInt(tfCredits.getText()));

        int filiereIndex = cbFiliere.getSelectionModel().getSelectedIndex();
        m.setFiliere_id(filiereList.get(filiereIndex).getId());

        int resIndex = cbResponsable.getSelectionModel().getSelectedIndex();
        m.setResponsable_id(professeurList.get(resIndex).getId());

        if (m.getId() == 0) {
            moduleDAO.add(m);
        } else {
            moduleDAO.update(m);
        }

        if (onSuccess != null) onSuccess.run();
        handleClose();
    }
}
