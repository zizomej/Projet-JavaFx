package com.learnhub.medical.controller;

import com.learnhub.medical.entity.Creneau;
import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.entity.Utilisateur;
import com.learnhub.medical.repository.CreneauRepository;
import com.learnhub.medical.repository.RDVRepository;
import com.learnhub.medical.repository.UtilisateurRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class EditRDVController {

    @FXML private Label lblHeaderTitle;
    @FXML private Button btnDelete;
    @FXML private Button btnSave;

    @FXML private ComboBox<Utilisateur> comboStudent;
    @FXML private TextField txtMotif;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> comboStatut;
    @FXML private ComboBox<Creneau> comboCreneau;
    @FXML private TextArea txtDescription;
    @FXML private TextArea txtCompteRendu;

    private final RDVRepository rdvRepo = new RDVRepository();
    private final CreneauRepository creneauRepo = new CreneauRepository();
    private final UtilisateurRepository userRepo = new UtilisateurRepository();

    private RDV currentRDV;
    private RDVManagementController parentController;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        comboStatut.setItems(FXCollections.observableArrayList("En attente", "Confirmé", "Annulé"));
        
        setupStudentCombo();
        setupCreneauCombo();
        
        datePicker.setValue(LocalDate.now());
    }

    private void setupStudentCombo() {
        try {
            List<Utilisateur> students = userRepo.findAll(); // Normalement filtrer par rôle 'etudiant'
            comboStudent.setItems(FXCollections.observableArrayList(students));
            comboStudent.setConverter(new StringConverter<>() {
                @Override public String toString(Utilisateur u) { return u == null ? "" : u.getFullName() + " (" + u.getEmail() + ")"; }
                @Override public Utilisateur fromString(String s) { return null; }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupCreneauCombo() {
        try {
            List<Creneau> slots = creneauRepo.findAll();
            comboCreneau.setItems(FXCollections.observableArrayList(slots));
            comboCreneau.setConverter(new StringConverter<>() {
                @Override public String toString(Creneau c) { return c == null ? "" : c.getJour() + " à " + c.getHeure(); }
                @Override public Creneau fromString(String s) { return null; }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setEditData(RDV rdv, RDVManagementController parent) {
        this.currentRDV = rdv;
        this.parentController = parent;
        this.isEditMode = (rdv != null);

        if (isEditMode) {
            lblHeaderTitle.setText("Modifier le Rendez-vous #" + rdv.getId());
            btnDelete.setVisible(true);
            btnSave.setText("Mettre à jour");
            
            txtMotif.setText(rdv.getMotif());
            txtDescription.setText(rdv.getDescription());
            txtCompteRendu.setText(rdv.getCompteRendu());
            datePicker.setValue(rdv.getDateDemande());
            comboStatut.setValue(rdv.getStatut());
            
            // Sélectionner l'étudiant et le créneau correspondants
            selectStudentById(rdv.getEtudiantId());
            selectCreneauById(rdv.getCreneauId());
        } else {
            lblHeaderTitle.setText("Nouveau Rendez-vous");
            btnDelete.setVisible(false);
            btnSave.setText("Enregistrer");
            comboStatut.setValue("En attente");
        }
    }

    private void selectStudentById(int id) {
        for (Utilisateur u : comboStudent.getItems()) {
            if (u.getId() == id) {
                comboStudent.setValue(u);
                break;
            }
        }
    }

    private void selectCreneauById(int id) {
        for (Creneau c : comboCreneau.getItems()) {
            if (c.getId() == id) {
                comboCreneau.setValue(c);
                break;
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) return;

        try {
            RDV rdv = isEditMode ? currentRDV : new RDV();
            rdv.setMotif(txtMotif.getText());
            rdv.setDescription(txtDescription.getText());
            rdv.setCompteRendu(txtCompteRendu.getText());
            rdv.setDateDemande(datePicker.getValue());
            rdv.setStatut(comboStatut.getValue());
            rdv.setEtudiantId(comboStudent.getValue().getId());
            rdv.setCreneauId(comboCreneau.getValue().getId());

            if (isEditMode) {
                rdvRepo.update(rdv);
            } else {
                rdvRepo.save(rdv);
            }

            if (parentController != null) parentController.handleRefresh();
            close();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'enregistrer le rendez-vous");
        }
    }

    @FXML
    private void handleDelete() {
        if (currentRDV == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce rendez-vous ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    rdvRepo.delete(currentRDV.getId());
                    if (parentController != null) parentController.handleRefresh();
                    close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML private void handleCancel() { close(); }

    private boolean validateForm() {
        if (comboStudent.getValue() == null || txtMotif.getText().isBlank() || comboCreneau.getValue() == null) {
            showAlert("Validation", "Veuillez remplir les champs obligatoires (*)");
            return false;
        }
        return true;
    }

    private void close() {
        ((Stage) txtMotif.getScene().getWindow()).close();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
