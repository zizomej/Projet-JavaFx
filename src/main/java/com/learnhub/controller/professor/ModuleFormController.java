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
        // Contrôle de saisie en temps réel : n'autoriser que des chiffres pour Semestre et Crédits
        tfSemestre.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) {
                tfSemestre.setText(oldValue);
            }
        });

        tfCredits.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.matches("\\d*")) {
                tfCredits.setText(oldValue);
            }
        });

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

            // Enlever l'erreur si l'utilisateur saisit quelque chose
            tfCode.textProperty().addListener((obs, old, newVal) -> clearError(tfCode));
            tfIntitule.textProperty().addListener((obs, old, newVal) -> clearError(tfIntitule));
            tfSemestre.textProperty().addListener((obs, old, newVal) -> clearError(tfSemestre));
            tfCredits.textProperty().addListener((obs, old, newVal) -> clearError(tfCredits));
            cbFiliere.valueProperty().addListener((obs, old, newVal) -> clearError(cbFiliere));
            cbResponsable.valueProperty().addListener((obs, old, newVal) -> clearError(cbResponsable));

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

    private void showError(Control control, String message) {
        javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) control.getParent();
        if (!control.getStyle().contains("#ef4444")) {
            control.setStyle(control.getStyle() + "; -fx-border-color: #ef4444; -fx-border-width: 1px;");
        }
        Label errorLabel = new Label("• " + message);
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px;");
        errorLabel.getStyleClass().add("error-label");
        parent.getChildren().add(errorLabel);
    }

    private void clearError(Control c) {
        javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) c.getParent();
        parent.getChildren().removeIf(node -> node instanceof Label && node.getStyleClass().contains("error-label"));
        if (c.getStyle().contains("#ef4444")) {
            c.setStyle(c.getStyle().replace("; -fx-border-color: #ef4444; -fx-border-width: 1px;", ""));
        }
    }

    private void clearAllErrors() {
        Control[] controls = {tfCode, tfIntitule, tfSemestre, tfCredits, cbFiliere, cbResponsable};
        for (Control c : controls) {
            clearError(c);
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) tfCode.getScene().getWindow();
        stage.close();
    }


    @FXML
    private void handleSave() {
        clearAllErrors();
        boolean hasError = false;

        if (tfCode.getText() == null || tfCode.getText().trim().isEmpty()) { showError(tfCode, "Le code du module est obligatoire."); hasError = true; }
        if (tfIntitule.getText() == null || tfIntitule.getText().trim().isEmpty()) { showError(tfIntitule, "L'intitulé est obligatoire."); hasError = true; }
        if (tfSemestre.getText() == null || tfSemestre.getText().trim().isEmpty()) { showError(tfSemestre, "Le semestre est obligatoire."); hasError = true; }
        if (tfCredits.getText() == null || tfCredits.getText().trim().isEmpty()) { showError(tfCredits, "Les crédits sont obligatoires."); hasError = true; }
        if (cbResponsable.getValue() == null) { showError(cbResponsable, "Veuillez sélectionner un responsable."); hasError = true; }
        if (cbFiliere.getValue() == null) { showError(cbFiliere, "Veuillez sélectionner une filière."); hasError = true; }

        if (hasError) {
            return;
        }

        try {
            int semestre = Integer.parseInt(tfSemestre.getText().trim());
            int credits = Integer.parseInt(tfCredits.getText().trim());

            if (semestre <= 0 || semestre > 10) {
                showError(tfSemestre, "Le semestre doit être compris entre 1 et 10.");
                return;
            }
            if (credits <= 0 || credits > 30) {
                showError(tfCredits, "Les crédits doivent être compris entre 1 et 30.");
                return;
            }

            Module m = currentModule == null ? new Module() : currentModule;

            m.setCode(tfCode.getText().trim());
            m.setIntitule(tfIntitule.getText().trim());

            m.setSemestre(semestre);
            m.setCredits(credits);

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

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Les champs Semestre et Crédits doivent être des nombres valides.").show();
        }
    }
}
