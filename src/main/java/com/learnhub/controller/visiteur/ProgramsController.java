package com.learnhub.controller.visiteur;

import com.learnhub.dao.CoursDAO;
import com.learnhub.models.Cours;
import com.learnhub.util.NavigationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class ProgramsController {

    @FXML private BorderPane rootPane;
    @FXML private Label totalLabel;
    @FXML private TextField searchField;

    @FXML private TableView<Cours> coursTable;
    @FXML private TableColumn<Cours, String> titreCol;
    @FXML private TableColumn<Cours, String> matiereCol;
    @FXML private TableColumn<Cours, String> niveauCol;
    @FXML private TableColumn<Cours, String> filiereCol;
    @FXML private TableColumn<Cours, String> profCol;
    @FXML private TableColumn<Cours, String> statCol;

    @FXML
    public void initialize() {
        titreCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitre()));
        matiereCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCode()));
        niveauCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getSemestre())));
        filiereCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCredits())));
        profCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProfesseurNom()));
        statCol.setCellValueFactory(c -> new SimpleStringProperty("Disponible"));

        try {
            List<Cours> all = new CoursDAO().findAll();
            ObservableList<Cours> data = FXCollections.observableArrayList(all);
            FilteredList<Cours> filtered = new FilteredList<>(data, p -> true);

            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filtered.setPredicate(cours -> {
                    if (newVal == null || newVal.isBlank()) return true;
                    String lower = newVal.toLowerCase();
                    return (cours.getTitre() != null && cours.getTitre().toLowerCase().contains(lower))
                        || (cours.getCode() != null && cours.getCode().toLowerCase().contains(lower))
                        || String.valueOf(cours.getSemestre()).contains(lower)
                        || String.valueOf(cours.getCredits()).contains(lower)
                        || (cours.getProfesseurNom() != null && cours.getProfesseurNom().toLowerCase().contains(lower));
                });
                totalLabel.setText(filtered.size() + " programme(s) trouvé(s)");
            });

            coursTable.setItems(filtered);
            totalLabel.setText(all.size() + " programme(s) disponible(s)");
        } catch (SQLException e) {
            totalLabel.setText("Erreur de chargement");
            e.printStackTrace();
        }
    }

    @FXML private void goHome()     { navigate("/fxml/visiteur/home.fxml", "LearnHub — Accueil"); }
    @FXML private void goPrograms() { /* déjà ici */ }
    @FXML private void goEvents()   { navigate("/fxml/visiteur/events.fxml", "Événements"); }
    @FXML private void goPartners() { navigate("/fxml/visiteur/partners.fxml", "Partenaires"); }
    @FXML private void goLogin()    { navigate("/fxml/auth/login.fxml", "Connexion"); }
    @FXML private void goRegister() { navigate("/fxml/auth/register.fxml", "Inscription"); }

    @FXML private void goHomeLabel(MouseEvent e) { goHome(); }

    private void navigate(String fxml, String title) {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        NavigationUtil.navigateTo(stage, fxml, title);
    }
}
