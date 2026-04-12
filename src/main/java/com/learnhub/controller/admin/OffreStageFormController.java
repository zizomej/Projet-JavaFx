package com.learnhub.controller.admin;

import com.learnhub.dao.FiliereDAO;
import com.learnhub.dao.OffreStageDAO;
import com.learnhub.dao.PartenaireDAO;
import com.learnhub.models.Filiere;
import com.learnhub.models.OffreStage;
import com.learnhub.models.Partenaire;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class OffreStageFormController {

    @FXML private TextField titreField;
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField dureeField;
    @FXML private ComboBox<Partenaire> partenaireBox;
    @FXML private ComboBox<Filiere> filiereBox;
    @FXML private DatePicker publicationPicker;
    @FXML private TextArea descriptionArea;

    @FXML private Label titleIcon;
    @FXML private Label titleLabel;
    @FXML private Button saveButton;

    @FXML private Label titreError;
    @FXML private Label dureeError;
    @FXML private Label partenaireError;
    @FXML private Label filiereError;
    @FXML private Label descriptionError;

    private OffreStage offre;
    private boolean edit;
    private Stage stage;
    private Runnable onSaveCallback;
    private final OffreStageDAO dao = new OffreStageDAO();
    private final PartenaireDAO partenaireDao = new PartenaireDAO();
    private final FiliereDAO filiereDao = new FiliereDAO();

    public void initData(OffreStage offre, boolean edit, Stage stage, Runnable onSaveCallback) {
        this.offre = offre;
        this.edit = edit;
        this.stage = stage;
        this.onSaveCallback = onSaveCallback;

        typeBox.getItems().addAll("observation", "initiation", "perfectionnement", "pfe", "ete");

        if (edit) {
            titleIcon.setText("✏");
            titleLabel.setText("Modifier l'Offre de Stage");
            saveButton.setText("Enregistrer les modifications");
        }

        try {
            List<Partenaire> partenaires = partenaireDao.findAll();
            partenaireBox.setItems(FXCollections.observableArrayList(partenaires));
            List<Filiere> filieres = filiereDao.findAll();
            filiereBox.setItems(FXCollections.observableArrayList(filieres));
            
            if (edit && offre != null) {
                titreField.setText(safe(offre.getTitre()));
                typeBox.setValue(offre.getTypeStage() != null ? offre.getTypeStage() : "pfe");
                dureeField.setText(String.valueOf(offre.getDureeMois()));
                publicationPicker.setValue(parseDate(offre.getDatePublication()));
                descriptionArea.setText(safe(offre.getDescription()));
                
                partenaires.stream()
                        .filter(p -> p.getId() == offre.getPartenaireId())
                        .findFirst()
                        .ifPresent(partenaireBox::setValue);
                
                filieres.stream()
                        .filter(f -> f.getId() == offre.getFiliereId())
                        .findFirst()
                        .ifPresent(filiereBox::setValue);
            } else {
                typeBox.setValue("pfe");
                dureeField.setText("3");
                publicationPicker.setValue(LocalDate.now());
            }
        } catch (SQLException e) {
            showError("Erreur de chargement des listes: " + e.getMessage());
        }
    }

    @FXML
    public void handleCreate() {
        boolean valid = true;
        
        titreError.setVisible(false); titreError.setManaged(false);
        dureeError.setVisible(false); dureeError.setManaged(false);
        partenaireError.setVisible(false); partenaireError.setManaged(false);
        filiereError.setVisible(false); filiereError.setManaged(false);
        descriptionError.setVisible(false); descriptionError.setManaged(false);

        if (titreField.getText() == null || titreField.getText().trim().isEmpty()) {
            titreError.setVisible(true); titreError.setManaged(true);
            valid = false;
        }
        if (descriptionArea.getText() == null || descriptionArea.getText().trim().isEmpty()) {
            descriptionError.setVisible(true); descriptionError.setManaged(true);
            valid = false;
        }
        if (partenaireBox.getValue() == null) {
            partenaireError.setVisible(true); partenaireError.setManaged(true);
            valid = false;
        }
        if (filiereBox.getValue() == null) {
            filiereError.setVisible(true); filiereError.setManaged(true);
            valid = false;
        }

        if (!valid) return;

        offre.setTitre(titreField.getText().trim());
        offre.setDescription(descriptionArea.getText().trim());
        offre.setTypeStage(typeBox.getValue());
        
        try {
            offre.setDureeMois(Integer.parseInt(dureeField.getText().trim()));
        } catch(NumberFormatException e) {
            dureeError.setVisible(true); dureeError.setManaged(true);
            return;
        }
        
        offre.setDatePublication(publicationPicker.getValue() != null ? publicationPicker.getValue().toString() : LocalDate.now().toString());
        offre.setPartenaireId(partenaireBox.getValue().getId());
        offre.setPartenaireNom(partenaireBox.getValue().getNom());
        offre.setFiliereId(filiereBox.getValue().getId());
        offre.setFiliereNom(filiereBox.getValue().getNom());

        try {
            if (edit) {
                dao.update(offre);
            } else {
                dao.insert(offre);
            }
            if (onSaveCallback != null) onSaveCallback.run();
            if (stage != null) stage.close();
        } catch (SQLException e) {
            showError("Enregistrement impossible : " + e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        if (stage != null) stage.close();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
    
    private LocalDate parseDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) return LocalDate.now();
        try {
            return LocalDate.parse(rawDate);
        } catch (DateTimeParseException ignored) {
            return LocalDate.now();
        }
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}
