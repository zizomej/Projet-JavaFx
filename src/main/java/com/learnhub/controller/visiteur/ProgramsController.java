package com.learnhub.controller.visiteur;

import com.learnhub.dao.FiliereDAO;
import com.learnhub.dao.ModuleDAO;
import com.learnhub.models.Filiere;
import com.learnhub.models.Module;
import com.learnhub.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.sql.SQLException;

public class ProgramsController {

    @FXML private TableView<Filiere>  filieresTable;
    @FXML private TableColumn<Filiere, String> colNom, colNiveau, colDuree, colCapacite, colUniv;
    @FXML private TableView<Module>   modulesTable;
    @FXML private TableColumn<Module, String> colModuleTitre, colModuleFiliere, colModuleSem, colModuleCredits;
    @FXML private ComboBox<String>    niveauFilter, filiereFilter;
    @FXML private TextField           searchField;
    @FXML private Label               countFilieresLabel, countModulesLabel;

    private final FiliereDAO filiereDAO = new FiliereDAO();
    private final ModuleDAO  moduleDAO  = new ModuleDAO();
    private ObservableList<Filiere> allFilieres = FXCollections.observableArrayList();
    private ObservableList<Module>  allModules  = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeAnnees"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capaciteMax"));
        colModuleTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colModuleSem.setCellValueFactory(new PropertyValueFactory<>("semestre"));
        colModuleCredits.setCellValueFactory(new PropertyValueFactory<>("credits"));
        niveauFilter.setItems(FXCollections.observableArrayList("Tous","L1","L2","L3","M1","M2"));
        niveauFilter.setValue("Tous");
        loadData();
    }

    private void loadData() {
        try {
            allFilieres = FXCollections.observableArrayList(filiereDAO.findAll());
            filieresTable.setItems(allFilieres);
            countFilieresLabel.setText(allFilieres.size() + " filière(s)");
            allModules = FXCollections.observableArrayList(moduleDAO.findAll());
            modulesTable.setItems(allModules);
            countModulesLabel.setText(allModules.size() + " module(s)");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML private void handleSearch() { loadData(); }
    @FXML private void goHome()     { nav("/fxml/visiteur/home.fxml","Accueil"); }
    @FXML private void goPrograms() { nav("/fxml/visiteur/programs.fxml","Programmes"); }
    @FXML private void goEvents()   { nav("/fxml/visiteur/events.fxml","Événements"); }
    @FXML private void goPartners() { nav("/fxml/visiteur/partners.fxml","Partenaires"); }
    @FXML private void goLogin()    { nav("/fxml/auth/login.fxml","Connexion"); }
    private void nav(String f, String t) {
        Stage s = (Stage) filieresTable.getScene().getWindow();
        NavigationUtil.navigateTo(s, f, t);
    }
}
