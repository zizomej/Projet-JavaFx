package com.learnhub.controller.visiteur;

import com.learnhub.dao.PartenaireDAO;
import com.learnhub.dao.OffreStageDAO;
import com.learnhub.dao.FiliereDAO;
import com.learnhub.models.Partenaire;
import com.learnhub.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.sql.SQLException;

public class PartnersController {

    @FXML private TableView<Partenaire> partenaireTable;
    @FXML private TableColumn<Partenaire,String> colNom, colSecteur, colVille, colEmail, colStatut;
    @FXML private TextField searchField;
    @FXML private Label totalPartenairesLabel, totalOffresLabel, totalFilieresLabel;

    private final PartenaireDAO  partenaireDAO  = new PartenaireDAO();
    private final OffreStageDAO  offreStageDAO  = new OffreStageDAO();
    private final FiliereDAO     filiereDAO     = new FiliereDAO();
    private ObservableList<Partenaire> allData  = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colSecteur.setCellValueFactory(new PropertyValueFactory<>("secteurActivite"));
        colVille.setCellValueFactory(new PropertyValueFactory<>("ville"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        loadData();
    }

    private void loadData() {
        try {
            allData = FXCollections.observableArrayList(partenaireDAO.findAll());
            partenaireTable.setItems(allData);
            totalPartenairesLabel.setText(String.valueOf(partenaireDAO.count()));
            totalOffresLabel.setText(String.valueOf(offreStageDAO.count()));
            totalFilieresLabel.setText(String.valueOf(filiereDAO.count()));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void handleSearch() {
        String s = searchField.getText().trim().toLowerCase();
        partenaireTable.setItems(s.isEmpty() ? allData :
            allData.filtered(p -> (p.getNom()!=null && p.getNom().toLowerCase().contains(s)) ||
                                  (p.getVille()!=null && p.getVille().toLowerCase().contains(s))));
    }

    @FXML private void goHome()     { nav("/fxml/visiteur/home.fxml","Accueil"); }
    @FXML private void goPrograms() { nav("/fxml/visiteur/programs.fxml","Programmes"); }
    @FXML private void goEvents()   { nav("/fxml/visiteur/events.fxml","Événements"); }
    @FXML private void goPartners() { nav("/fxml/visiteur/partners.fxml","Partenaires"); }
    @FXML private void goLogin()    { nav("/fxml/auth/login.fxml","Connexion"); }
    private void nav(String f, String t) {
        Stage s = (Stage) partenaireTable.getScene().getWindow();
        NavigationUtil.navigateTo(s, f, t);
    }
}
