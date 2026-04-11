package com.learnhub.controller.visiteur;

import com.learnhub.dao.EvenementDAO;
import com.learnhub.models.Evenement;
import com.learnhub.util.NavigationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class EventsController {

    @FXML private TableView<Evenement> eventsTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;

    private final EvenementDAO evenementDAO = new EvenementDAO();
    private ObservableList<Evenement> allData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        typeFilter.setItems(FXCollections.observableArrayList(
            "Tous", "CONFERENCE", "WORKSHOP", "CULTUREL", "SPORT", "AUTRE"));
        typeFilter.setValue("Tous");
        loadData();
    }

    private void setupTable() {
        TableColumn<Evenement, Integer> colId   = new TableColumn<>("ID");
        TableColumn<Evenement, String>  colTitre = new TableColumn<>("Titre");
        TableColumn<Evenement, String>  colType  = new TableColumn<>("Type");
        TableColumn<Evenement, String>  colDate  = new TableColumn<>("Date début");
        TableColumn<Evenement, String>  colLieu  = new TableColumn<>("Lieu");
        TableColumn<Evenement, String>  colStatut= new TableColumn<>("Statut");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeEvenement"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        colId.setPrefWidth(55); colTitre.setPrefWidth(280);
        colType.setPrefWidth(130); colDate.setPrefWidth(130);
        colLieu.setPrefWidth(150); colStatut.setPrefWidth(110);

        eventsTable.getColumns().addAll(colId, colTitre, colType, colDate, colLieu, colStatut);
    }

    private void loadData() {
        try {
            allData = FXCollections.observableArrayList(evenementDAO.findAll());
            eventsTable.setItems(allData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String q = searchField.getText().trim().toLowerCase();
        String type = typeFilter.getValue();
        ObservableList<Evenement> filtered = allData.filtered(ev -> {
            boolean matchQ = q.isEmpty() || ev.getTitre().toLowerCase().contains(q);
            boolean matchType = type == null || type.equals("Tous") || type.equals(ev.getTypeEvenement());
            return matchQ && matchType;
        });
        eventsTable.setItems(filtered);
    }

    @FXML private void goHome()     { nav("/fxml/visiteur/home.fxml",     "Accueil"); }
    @FXML private void goPrograms() { nav("/fxml/visiteur/programs.fxml", "Programmes"); }
    @FXML private void goEvents()   { nav("/fxml/visiteur/events.fxml",   "Actualités"); }
    @FXML private void goPartners() { nav("/fxml/visiteur/partners.fxml", "Partenaires"); }
    @FXML private void goLogin()    { nav("/fxml/auth/login.fxml",        "Connexion"); }

    private void nav(String path, String title) {
        Stage stage = (Stage) eventsTable.getScene().getWindow();
        NavigationUtil.navigateTo(stage, path, title);
    }
}
