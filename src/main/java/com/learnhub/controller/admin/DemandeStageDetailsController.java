package com.learnhub.controller.admin;

import com.learnhub.dao.DemandeStageDAO;
import com.learnhub.models.DemandeStage;
import com.learnhub.util.NavigationUtil;
import com.learnhub.util.SessionManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class DemandeStageDetailsController {

    // Static holder for passing demande between pages
    private static DemandeStage demandeToShow;

    public static void setDemandeToShow(DemandeStage demande) {
        demandeToShow = demande;
    }

    // FXML fields
    @FXML private Label lblTitre;
    @FXML private Label lblDate;
    @FXML private Label lblStatutActuel;
    @FXML private Label lblEtudiantNom;
    @FXML private Label lblEtudiantEmail;
    @FXML private Label lblEtudiantId;
    @FXML private Label lblOffreTitre;
    @FXML private Label lblPartenaireNom;
    @FXML private TextArea txtMotivation;
    @FXML private Label lblPieceJointe;
    @FXML private ComboBox<String> comboStatut;

    private final DemandeStageDAO dao = new DemandeStageDAO();
    private DemandeStage currentDemande;

    @FXML
    public void initialize() {
        comboStatut.getItems().addAll("en_attente", "acceptee", "refusee", "en_cours");

        if (demandeToShow != null) {
            currentDemande = demandeToShow;
            populateFields(currentDemande);
        }
    }

    private void populateFields(DemandeStage d) {
        if (lblTitre != null) lblTitre.setText("📋 Demande de Stage #" + d.getId());
        if (lblDate != null) lblDate.setText("Reçue le " + safe(d.getDateDemande()));
        if (lblStatutActuel != null) {
            String s = safe(d.getStatut());
            lblStatutActuel.setText(getStatutIcon(s) + " " + s);
            lblStatutActuel.setStyle(getStatutStyle(s));
        }
        if (lblEtudiantNom != null) lblEtudiantNom.setText(safe(d.getEtudiantNom()));
        if (lblEtudiantEmail != null) lblEtudiantEmail.setText(safe(d.getEtudiantEmail()));
        if (lblEtudiantId != null) lblEtudiantId.setText("#" + d.getEtudiantId());
        if (lblOffreTitre != null) lblOffreTitre.setText(safe(d.getOffreTitre()));
        if (lblPartenaireNom != null) lblPartenaireNom.setText(safe(d.getPartenaireNom()));
        if (txtMotivation != null) txtMotivation.setText(safe(d.getMotivation()));
        if (lblPieceJointe != null) {
            String pj = d.getPieceJointe();
            lblPieceJointe.setText(pj == null || pj.isBlank() ? "Aucune pièce jointe" : pj);
        }
        if (comboStatut != null) comboStatut.setValue(safe(d.getStatut()));
    }

    @FXML
    public void handleUpdateStatut() {
        if (currentDemande == null || comboStatut.getValue() == null) return;
        try {
            dao.updateStatut(currentDemande.getId(), comboStatut.getValue(), null);
            currentDemande.setStatut(comboStatut.getValue());
            if (lblStatutActuel != null) {
                String s = comboStatut.getValue();
                lblStatutActuel.setText(getStatutIcon(s) + " " + s);
                lblStatutActuel.setStyle(getStatutStyle(s));
            }
            new Alert(Alert.AlertType.INFORMATION, "Statut mis à jour.").show();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).show();
        }
    }

    @FXML
    public void handleDownloadCv() {
        if (currentDemande == null) {
            return;
        }
        String path = currentDemande.getPieceJointe();
        if (path == null || path.isBlank() || "piece-non-fournie".equalsIgnoreCase(path.trim())) {
            new Alert(Alert.AlertType.INFORMATION, "Aucun fichier CV n'a été fourni pour cette demande.").show();
            return;
        }
        String trimmed = path.trim();
        boolean isRemote = trimmed.startsWith("http://") || trimmed.startsWith("https://");
        File src = isRemote ? null : new File(trimmed);
        if (!isRemote && (src == null || !src.isFile())) {
            new Alert(Alert.AlertType.WARNING,
                    "Le fichier n'est plus accessible sur le serveur :\n" + path).show();
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer le CV");
        chooser.setInitialFileName(isRemote ? "cv.pdf" : src.getName());
        Stage st = getStage();
        if (st == null) {
            return;
        }
        File dest = chooser.showSaveDialog(st);
        if (dest == null) {
            return;
        }
        Task<Void> task;
        if (isRemote) {
            task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    try (InputStream in = new URL(trimmed).openStream()) {
                        Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    return null;
                }
            };
        } else {
            final File local = src;
            task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    Files.copy(local.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    return null;
                }
            };
        }
        task.setOnSucceeded(e -> new Alert(Alert.AlertType.INFORMATION, "CV enregistré :\n" + dest.getAbsolutePath()).show());
        task.setOnFailed(e -> new Alert(Alert.AlertType.ERROR, "Impossible de copier le fichier.").show());
        new Thread(task, "cv-download").start();
    }

    @FXML
    public void handleDelete() {
        if (currentDemande == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer la demande de \"" + currentDemande.getEtudiantNom() + "\" ?",
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    dao.delete(currentDemande.getId());
                    handleBack();
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).show();
                }
            }
        });
    }

    @FXML
    public void handleBack() {
        navigate("/fxml/admin/demandes_stage.fxml", "Demandes de Stage");
    }

    private String getStatutIcon(String s) {
        return switch (s) {
            case "acceptee" -> "✅";
            case "refusee" -> "❌";
            case "en_cours" -> "🔄";
            default -> "⏳";
        };
    }

    private String getStatutStyle(String s) {
        return switch (s) {
            case "acceptee" -> "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 20; -fx-font-size: 14px;";
            case "refusee" -> "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 20; -fx-font-size: 14px;";
            case "en_cours" -> "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 20; -fx-font-size: 14px;";
            default -> "-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 20; -fx-font-size: 14px;";
        };
    }

    private String safe(String s) { return s == null ? "" : s; }

    @FXML private void goDashboard()     { navigate("/fxml/admin/dashboard.fxml",      "Tableau de bord"); }
    @FXML private void goUtilisateurs()  { navigate("/fxml/admin/utilisateurs.fxml",   "Utilisateurs"); }
    @FXML private void goModules()       { navigate("/fxml/admin/modules.fxml",         "Modules"); }
    @FXML private void goSeances()       { navigate("/fxml/admin/seances.fxml",         "Séances"); }
    @FXML private void goNotes()         { navigate("/fxml/admin/notes.fxml",           "Notes"); }
    @FXML private void goPresences()     { navigate("/fxml/admin/presences.fxml",       "Présences"); }
    @FXML private void goFilieres()      { navigate("/fxml/admin/filieres.fxml",        "Filières"); }
    @FXML private void goEvenements()    { navigate("/fxml/admin/evenements.fxml",      "Événements"); }
    @FXML private void goRdv()           { navigate("/fxml/admin/rdv.fxml",             "RDV Médicaux"); }
    @FXML private void goCreneaux()      { navigate("/fxml/admin/creneaux.fxml",        "Créneaux"); }
    @FXML private void goPartenaires()   { navigate("/fxml/admin/partenaires.fxml",     "Partenaires"); }
    @FXML private void goOffresStage()   { navigate("/fxml/admin/offres_stage.fxml",    "Offres de Stage"); }
    @FXML private void goDemandesStage() { navigate("/fxml/admin/demandes_stage.fxml",  "Demandes de Stage"); }

    @FXML private void handleLogout() {
        SessionManager.getInstance().logout();
        Stage stage = getStage();
        if (stage != null) NavigationUtil.navigateToFixed(stage, "/fxml/auth/login.fxml", "Connexion", 1100, 700);
    }

    private void navigate(String fxml, String title) {
        Stage stage = getStage();
        if (stage != null) NavigationUtil.navigateTo(stage, fxml, title);
    }

    private Stage getStage() {
        if (lblTitre != null && lblTitre.getScene() != null)
            return (Stage) lblTitre.getScene().getWindow();
        return null;
    }
}
