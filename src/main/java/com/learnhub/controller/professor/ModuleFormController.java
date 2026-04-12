package com.learnhub.controller.professor;

import com.learnhub.dao.FiliereDAO;
import com.learnhub.dao.ModuleDAO;
import com.learnhub.dao.UtilisateurDAO;
import com.learnhub.models.Filiere;
import com.learnhub.models.Module;
import com.learnhub.models.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    private final FiliereDAO filiereDAO = new FiliereDAO();
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    private List<Filiere> filiereList = new ArrayList<>();
    private List<Utilisateur> professeurList = new ArrayList<>();

    @FXML
    public void initialize() {
        try {
            filiereList = filiereDAO.findAll();
            for (Filiere f : filiereList) cbFiliere.getItems().add(f.getNom());

            professeurList = utilisateurDAO.findByRole("ROLE_PROFESSEUR");
            if (professeurList.isEmpty()) professeurList = utilisateurDAO.findByRole("professeur");
            if (professeurList.isEmpty()) professeurList = utilisateurDAO.findByRole("PROFESSOR");
            for (Utilisateur p : professeurList) {
                cbResponsable.getItems().add(p.getPrenom() + " " + p.getNom());
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
                    cbFiliere.getSelectionModel().select(i); break;
                }
            }
            for (int i = 0; i < professeurList.size(); i++) {
                if (professeurList.get(i).getId() == module.getResponsable_id()) {
                    cbResponsable.getSelectionModel().select(i); break;
                }
            }
        }
    }

    public void setOnSuccess(Runnable onSuccess) { this.onSuccess = onSuccess; }

    @FXML
    private void handleClose() {
        ((Stage) tfCode.getScene().getWindow()).close();
    }

    @FXML
    private void handleSave() {
        if (tfCode.getText().isEmpty() || tfIntitule.getText().isEmpty() ||
                tfSemestre.getText().isEmpty() || tfCredits.getText().isEmpty() ||
                cbResponsable.getValue() == null || cbFiliere.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Veuillez remplir tous les champs.").show();
            return;
        }

        Module m = currentModule == null ? new Module() : currentModule;
        m.setCode(tfCode.getText().trim());
        m.setIntitule(tfIntitule.getText().trim());
        try {
            m.setSemestre(Integer.parseInt(tfSemestre.getText().trim()));
            m.setCredits(Integer.parseInt(tfCredits.getText().trim()));
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Semestre et crédits doivent être des nombres.").show();
            return;
        }

        int fi = cbFiliere.getSelectionModel().getSelectedIndex();
        if (fi >= 0) m.setFiliere_id(filiereList.get(fi).getId());

        int ri = cbResponsable.getSelectionModel().getSelectedIndex();
        if (ri >= 0) m.setResponsable_id(professeurList.get(ri).getId());

        try {
            if (m.getId() == 0) moduleDAO.add(m); else moduleDAO.update(m);
            if (onSuccess != null) onSuccess.run();
            handleClose();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).show();
        }
    }
}
