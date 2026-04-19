package com.learnhub.controller.admin.demandestage;

import com.learnhub.dao.DemandeStageDAO;
import com.learnhub.models.DemandeStage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Controller du popup professionnel Accepter / Refuser une demande de stage.
 *
 * Usage :
 *   StatutDemandePopupController ctrl = loader.getController();
 *   ctrl.initData(demande, "acceptee", stage, onConfirm, score, categorie);
 */
public class StatutDemandePopupController {

    @FXML private HBox  headerBox;
    @FXML private Label headerIcon;
    @FXML private Label headerTitle;
    @FXML private Label headerSubtitle;
    @FXML private Label lblEtudiant;
    @FXML private Label lblOffre;
    @FXML private HBox  scoreBox;
    @FXML private Label lblScore;
    @FXML private Label lblCategorie;
    @FXML private TextArea txtCommentaire;
    @FXML private Label lblErreur;
    @FXML private Button btnConfirm;

    private DemandeStage demande;
    private String       action;   // "acceptee" ou "refusee"
    private Stage        stage;
    private Runnable     onConfirm;

    private final DemandeStageDAO dao = new DemandeStageDAO();

    // ─── Initialisation ──────────────────────────────────────────────────────────

    /**
     * @param demande   La demande concernée
     * @param action    "acceptee" ou "refusee"
     * @param stage     La fenêtre modale (pour la fermer)
     * @param onConfirm Callback appelé après succès (pour rafraîchir le tableau)
     * @param score     Score de matching (0-100) ; -1 = non disponible
     * @param categorie Catégorie ("Excellent", "Standard", "Faible")
     */
    public void initData(DemandeStage demande, String action, Stage stage,
                         Runnable onConfirm, int score, String categorie) {
        this.demande   = demande;
        this.action    = action;
        this.stage     = stage;
        this.onConfirm = onConfirm;

        // Nom + offre
        if (lblEtudiant != null) lblEtudiant.setText(safe(demande.getEtudiantNom()));
        if (lblOffre    != null) lblOffre.setText(safe(demande.getOffreTitre()));

        boolean isAccept = "acceptee".equalsIgnoreCase(action);

        // Couleur header + texte
        if (headerBox != null) {
            String bg = isAccept ? "#059669" : "#dc2626";
            headerBox.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 16 16 0 0; -fx-padding: 22 28;");
        }
        if (headerIcon  != null) headerIcon.setText(isAccept ? "✅" : "❌");
        if (headerTitle != null) headerTitle.setText(isAccept ? "Accepter la candidature" : "Refuser la candidature");
        if (headerSubtitle != null)
            headerSubtitle.setText(isAccept
                ? "L'étudiant sera notifié de la bonne nouvelle"
                : "Veuillez indiquer un motif de refus si possible");

        // Bouton confirm
        if (btnConfirm != null) {
            String btnColor = isAccept ? "#059669" : "#dc2626";
            btnConfirm.setText(isAccept ? "✅  Accepter" : "❌  Refuser");
            btnConfirm.setStyle(
                "-fx-background-color: " + btnColor + "; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 8; -fx-padding: 10 28; -fx-cursor: hand; -fx-border-width: 0; -fx-font-size: 14px;");
        }

        // Score matching
        if (score >= 0 && scoreBox != null) {
            scoreBox.setVisible(true);
            scoreBox.setManaged(true);
            if (lblScore     != null) lblScore.setText(score + "%");
            if (lblCategorie != null) {
                String catStyle = switch (categorie == null ? "" : categorie.toLowerCase()) {
                    case "excellent" -> "-fx-background-color: #d1fae5; -fx-text-fill: #065f46;";
                    case "standard"  -> "-fx-background-color: #fef3c7; -fx-text-fill: #b45309;";
                    default          -> "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
                };
                lblCategorie.setText(categorie != null ? categorie : "");
                lblCategorie.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4 10;" +
                                      "-fx-background-radius: 999; " + catStyle);
                // Couleur du score selon catégorie
                String scoreColor = switch (categorie == null ? "" : categorie.toLowerCase()) {
                    case "excellent" -> "-fx-text-fill: #059669;";
                    case "standard"  -> "-fx-text-fill: #d97706;";
                    default          -> "-fx-text-fill: #dc2626;";
                };
                if (lblScore != null) lblScore.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; " + scoreColor);
            }
        }
    }

    // ─── Actions ─────────────────────────────────────────────────────────────────

    @FXML
    public void handleConfirm() {
        if (demande == null || action == null) return;
        try {
            String commentaire = txtCommentaire != null ? txtCommentaire.getText().trim() : null;
            dao.updateStatut(demande.getId(), action, commentaire);
            if (onConfirm != null) onConfirm.run();
            closeStage();
        } catch (SQLException e) {
            showError("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @FXML
    public void handleClose() {
        closeStage();
    }

    // ─── Privé ───────────────────────────────────────────────────────────────────

    private void closeStage() {
        if (stage != null) stage.close();
    }

    private void showError(String msg) {
        if (lblErreur != null) {
            lblErreur.setText(msg);
            lblErreur.setVisible(true);
            lblErreur.setManaged(true);
        }
    }

    private String safe(String s) { return s == null ? "" : s; }
}
