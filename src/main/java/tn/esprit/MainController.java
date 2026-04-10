package tn.esprit;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // Statistics Labels
    @FXML private Label lblCountEtudiants;
    @FXML private Label lblCountProfs;
    @FXML private Label lblCountMedecins;
    @FXML private Label lblCountAdmins;
    @FXML private Label lblCountParents;
    @FXML private Label lblTotalUsers;

    // Filters
    @FXML private TextField tfSearchFilter;
    @FXML private ComboBox<String> cbRoleFilter;
    @FXML private ComboBox<String> cbStatusFilter;

    // Table
    @FXML private TableView<Utilisateur> tableView;
    @FXML private TableColumn<Utilisateur, Utilisateur> colPhoto;
    @FXML private TableColumn<Utilisateur, String> colNomComplet;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colCin;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, String> colTelephone;
    @FXML private TableColumn<Utilisateur, LocalDate> colDate;
    @FXML private TableColumn<Utilisateur, String> colStatut;
    @FXML private TableColumn<Utilisateur, Utilisateur> colActions;

    private ObservableList<Utilisateur> userList = FXCollections.observableArrayList();

    public Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_universitaire", "root", "");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
        return conn;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupComboBoxes();
        setupTableColumns();
        loadDataAndRefresh();
    }

    private void setupComboBoxes() {
        cbRoleFilter.setItems(FXCollections.observableArrayList("Tous les rôles", "Etudiant", "Professeur", "Médecin", "Administrateur", "Parent"));
        cbRoleFilter.getSelectionModel().selectFirst();

        cbStatusFilter.setItems(FXCollections.observableArrayList("Tous les statuts", "Actif", "Inactif"));
        cbStatusFilter.getSelectionModel().selectFirst();
        
        tfSearchFilter.textProperty().addListener((obs, oldVal, newVal) -> applyFilters(null));
        cbRoleFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters(null));
        cbStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters(null));
    }

    private void setupTableColumns() {
        // Nom Complet (Nom + Prenom)
        colNomComplet.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNom() + " " + cellData.getValue().getPrenom()));
        
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCin.setCellValueFactory(new PropertyValueFactory<>("cin"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));

        // Custom Photo Column
        colPhoto.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
        colPhoto.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Utilisateur user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    StackPane pane = new StackPane();
                    Circle circle = new Circle(15, Color.web("#2196F3"));
                    String init1 = user.getNom() != null && !user.getNom().isEmpty() ? user.getNom().substring(0, 1).toUpperCase() : "";
                    String init2 = user.getPrenom() != null && !user.getPrenom().isEmpty() ? user.getPrenom().substring(0, 1).toUpperCase() : "";
                    Label lbl = new Label(init1 + init2);
                    lbl.setTextFill(Color.WHITE);
                    lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
                    pane.getChildren().addAll(circle, lbl);
                    setGraphic(pane);
                }
            }
        });

        // Custom Role Column (Badge)
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colRole.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(role);
                    if (role.toLowerCase().contains("etudiant")) lbl.getStyleClass().add("badge-etudiant");
                    else if (role.toLowerCase().contains("admin")) lbl.getStyleClass().add("badge-admin");
                    else if (role.toLowerCase().contains("prof")) lbl.getStyleClass().add("badge-prof");
                    else lbl.getStyleClass().add("badge-etudiant"); // default
                    setGraphic(lbl);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Custom Status Column (Dot + Text)
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5);
                    box.setAlignment(Pos.CENTER);
                    Circle dot = new Circle(4);
                    dot.setFill(statut.equalsIgnoreCase("Actif") ? Color.web("#0bb177") : Color.GRAY);
                    Label lbl = new Label(statut);
                    lbl.setStyle("-fx-text-fill: " + (statut.equalsIgnoreCase("Actif") ? "#0bb177" : "gray") + "; -fx-font-size:12px;");
                    box.getChildren().addAll(dot, lbl);
                    setGraphic(box);
                }
            }
        });

        // Custom Actions Column (Edit/Delete Buttons)
        colActions.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
        colActions.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Utilisateur user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER);

                    Button btnEdit = new Button("✎");
                    btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff9800; -fx-font-size: 16px; -fx-cursor: hand;");
                    btnEdit.setOnAction(e -> openEditUserDialog(user));

                    Button btnDelete = new Button("🗑");
                    btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #f44336; -fx-font-size: 16px; -fx-cursor: hand;");
                    btnDelete.setOnAction(e -> deleteUser(user));

                    box.getChildren().addAll(btnEdit, btnDelete);
                    setGraphic(box);
                }
            }
        });
    }

    private void loadDataAndRefresh() {
        userList.clear();
        Connection conn = getConnection();
        if (conn == null) return;
        
        int etudiants = 0, profs = 0, medecins = 0, admins = 0, parents = 0;

        String query = "SELECT * FROM utilisateur";
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            while(rs.next()) {
                Utilisateur user = new Utilisateur(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("mot_de_passe"),
                    rs.getString("cin"),
                    rs.getString("telephone"),
                    rs.getString("role"),
                    rs.getString("statut"),
                    rs.getInt("filiere_id"),
                    rs.getDate("date_inscription") != null ? rs.getDate("date_inscription").toLocalDate() : null
                );
                userList.add(user);

                // Counters
                String r = user.getRole() != null ? user.getRole().toLowerCase() : "";
                if (r.contains("etudiant")) etudiants++;
                else if (r.contains("prof")) profs++;
                else if (r.contains("admin")) admins++;
                else if (r.contains("medecin")) medecins++;
                else if (r.contains("parent")) parents++;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        tableView.setItems(userList);
        
        lblCountEtudiants.setText(String.valueOf(etudiants));
        lblCountProfs.setText(String.valueOf(profs));
        lblCountMedecins.setText(String.valueOf(medecins));
        lblCountAdmins.setText(String.valueOf(admins));
        if (lblCountParents != null) lblCountParents.setText(String.valueOf(parents));
        lblTotalUsers.setText(userList.size() + " utilisateurs");
    }

    @FXML
    private void applyFilters(ActionEvent event) {
        String search = tfSearchFilter.getText() == null ? "" : tfSearchFilter.getText().toLowerCase().trim();
        String roleFilter = cbRoleFilter.getValue();
        String statusFilter = cbStatusFilter.getValue();
        
        ObservableList<Utilisateur> filtered = FXCollections.observableArrayList();
        
        for (Utilisateur u : userList) {
            boolean matchSearch = search.isEmpty() || 
                (u.getNom() != null && u.getNom().toLowerCase().contains(search)) ||
                (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(search)) ||
                (u.getEmail() != null && u.getEmail().toLowerCase().contains(search)) ||
                (u.getCin() != null && u.getCin().toLowerCase().contains(search));
                
            boolean matchRole = roleFilter == null || roleFilter.equals("Tous les rôles");
            if (!matchRole && u.getRole() != null) {
                 String dbRole = u.getRole().toLowerCase();
                 String fRole = roleFilter.toLowerCase();
                 if (fRole.contains("etudiant") && dbRole.contains("etudiant")) matchRole = true;
                 else if (fRole.contains("prof") && dbRole.contains("prof")) matchRole = true;
                 else if (fRole.contains("méd") && (dbRole.contains("med") || dbRole.contains("méd"))) matchRole = true;
                 else if (fRole.contains("admin") && dbRole.contains("admin")) matchRole = true;
                 else if (fRole.contains("parent") && dbRole.contains("parent")) matchRole = true;
            }
                
            boolean matchStatus = statusFilter == null || statusFilter.equals("Tous les statuts");
            if (!matchStatus && u.getStatut() != null) {
                 matchStatus = u.getStatut().equalsIgnoreCase(statusFilter);
            }
                
            if (matchSearch && matchRole && matchStatus) {
                filtered.add(u);
            }
        }
        tableView.setItems(filtered);
        lblTotalUsers.setText(filtered.size() + " utilisateurs");
    }

    @FXML
    private void resetFilters(ActionEvent event) {
        tfSearchFilter.clear();
        cbRoleFilter.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
        tableView.setItems(userList);
        lblTotalUsers.setText(userList.size() + " utilisateurs");
    }

    @FXML
    private void openAddUserDialog(ActionEvent event) {
        showUserDialog(null);
    }

    private void openEditUserDialog(Utilisateur user) {
        showUserDialog(user);
    }

    private void deleteUser(Utilisateur user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cet utilisateur ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            String query = "DELETE FROM utilisateur WHERE id = ?";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, user.getId());
                ps.executeUpdate();
                loadDataAndRefresh();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    // ─── Validation des champs avec assert ────────────────────────────────────
    /**
     * Valide les données saisies avec des assertions Java.
     * IMPORTANT : activer les assertions JVM avec -ea pour que assert lève AssertionError.
     * En production, les messages d'erreur sont collectés et retournés.
     *
     * @return Message d'erreur combiné, ou chaîne vide si tout est valide.
     */
    private String validateUserInput(String nomVal, String prenomVal, String emailVal,
                                     String pwdVal, String cinVal, String telVal,
                                     String roleVal) {
        StringBuilder errors = new StringBuilder();

        // --- Assert : vérifications internes (actif avec -ea) ---
        try {
            assert nomVal != null : "Le nom ne peut pas être null";
            assert prenomVal != null : "Le prénom ne peut pas être null";
            assert emailVal != null : "L'email ne peut pas être null";
            assert pwdVal != null : "Le mot de passe ne peut pas être null";
            assert cinVal != null : "Le CIN ne peut pas être null";
            assert telVal != null : "Le téléphone ne peut pas être null";
        } catch (AssertionError ae) {
            errors.append("Erreur interne : ").append(ae.getMessage()).append("\n");
            return errors.toString();
        }

        // --- Validations métier ---
        if (nomVal.trim().isEmpty()) {
            errors.append("• Le nom est obligatoire.\n");
        } else {
            assert nomVal.trim().length() >= 2 : "Nom trop court";
            if (nomVal.trim().length() < 2)
                errors.append("• Le nom doit contenir au moins 2 caractères.\n");
        }

        if (prenomVal.trim().isEmpty()) {
            errors.append("• Le prénom est obligatoire.\n");
        } else {
            assert prenomVal.trim().length() >= 2 : "Prénom trop court";
            if (prenomVal.trim().length() < 2)
                errors.append("• Le prénom doit contenir au moins 2 caractères.\n");
        }

        if (emailVal.trim().isEmpty()) {
            errors.append("• L'email est obligatoire.\n");
        } else {
            boolean emailValid = emailVal.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
            assert emailValid : "Format email invalide : " + emailVal;
            if (!emailValid)
                errors.append("• L'email n'est pas valide (ex: nom@domaine.com).\n");
        }

        if (pwdVal.trim().isEmpty()) {
            errors.append("• Le mot de passe est obligatoire.\n");
        } else {
            assert pwdVal.length() >= 6 : "Mot de passe trop court";
            if (pwdVal.length() < 6)
                errors.append("• Le mot de passe doit contenir au moins 6 caractères.\n");
        }

        if (cinVal.trim().isEmpty()) {
            errors.append("• Le CIN est obligatoire.\n");
        } else {
            boolean cinValid = cinVal.matches("\\d{8}");
            assert cinValid : "CIN invalide (doit être 8 chiffres) : " + cinVal;
            if (!cinValid)
                errors.append("• Le CIN doit contenir exactement 8 chiffres.\n");
        }

        if (telVal.trim().isEmpty()) {
            errors.append("• Le téléphone est obligatoire.\n");
        } else {
            boolean telValid = telVal.matches("\\d{8}");
            assert telValid : "Téléphone invalide (doit être 8 chiffres) : " + telVal;
            if (!telValid)
                errors.append("• Le téléphone doit contenir exactement 8 chiffres.\n");
        }

        if (roleVal == null || roleVal.trim().isEmpty()) {
            errors.append("• Le rôle est obligatoire.\n");
        }

        return errors.toString();
    }

    // Programmatic Dialog for Add/Edit to avoid needing a second FXML right away
    private void showUserDialog(Utilisateur existingUser) {
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle(existingUser == null ? "Ajouter un Utilisateur" : "Modifier un Utilisateur");
        dialog.setHeaderText("Veuillez remplir les informations de l'utilisateur.");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // ── Champs de saisie ──────────────────────────────────────────────────
        TextField nom = new TextField(); nom.setPromptText("Nom (min 2 caractères)");
        TextField prenom = new TextField(); prenom.setPromptText("Prénom (min 2 caractères)");
        TextField email = new TextField(); email.setPromptText("nom@domaine.com");
        PasswordField pwd = new PasswordField(); pwd.setPromptText("Min 6 caractères");
        TextField cin = new TextField(); cin.setPromptText("8 chiffres"); cin.setMaxWidth(120);
        TextField tfTelephone = new TextField(); tfTelephone.setPromptText("8 chiffres"); tfTelephone.setMaxWidth(120);
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList("Etudiant", "Professeur", "Médecin", "Administrateur", "Parent"));
        ComboBox<String> statut = new ComboBox<>(FXCollections.observableArrayList("Actif", "Inactif"));
        TextField filiereId = new TextField("0"); filiereId.setMaxWidth(80);
        DatePicker dateInscr = new DatePicker(LocalDate.now());


        if (existingUser != null) {
            nom.setText(existingUser.getNom());
            prenom.setText(existingUser.getPrenom());
            email.setText(existingUser.getEmail());
            pwd.setText(existingUser.getMotDePasse());
            cin.setText(existingUser.getCin());
            tfTelephone.setText(existingUser.getTelephone());
            role.setValue(existingUser.getRole());
            statut.setValue(existingUser.getStatut());
            filiereId.setText(String.valueOf(existingUser.getFiliereId()));
            dateInscr.setValue(existingUser.getDateInscription());
        } else {
            role.getSelectionModel().selectFirst();
            statut.getSelectionModel().selectFirst();
        }

        grid.add(new Label("Nom *:"),        0, 0); grid.add(nom,         1, 0);
        grid.add(new Label("Prénom *:"),     0, 1); grid.add(prenom,      1, 1);
        grid.add(new Label("Email *:"),      0, 2); grid.add(email,       1, 2);
        grid.add(new Label("Mot de Passe *:"), 0, 3); grid.add(pwd,      1, 3);
        grid.add(new Label("CIN *:"),        0, 4); grid.add(cin,         1, 4);
        grid.add(new Label("Téléphone *:"),  0, 5); grid.add(tfTelephone, 1, 5);
        grid.add(new Label("Rôle *:"),       0, 6); grid.add(role,        1, 6);
        grid.add(new Label("Statut:"),       0, 7); grid.add(statut,      1, 7);
        grid.add(new Label("Filière ID:"),   0, 8); grid.add(filiereId,   1, 8);
        grid.add(new Label("Date Inscr:"),   0, 9); grid.add(dateInscr,   1, 9);

        dialog.getDialogPane().setContent(grid);

        // ── Récupérer le bouton Enregistrer pour le contrôler ─────────────────
        javafx.scene.Node saveBtn = dialog.getDialogPane().lookupButton(saveButtonType);

        // Désactiver si les champs requis sont vides au départ
        Runnable updateSaveBtn = () -> {
            String err = validateUserInput(
                nom.getText(), prenom.getText(), email.getText(),
                pwd.getText(), cin.getText(), tfTelephone.getText(),
                role.getValue()
            );
            saveBtn.setDisable(!err.isEmpty());
        };

        // Écouter les changements pour valider en temps réel
        nom.textProperty().addListener((o, ov, nv) -> updateSaveBtn.run());
        prenom.textProperty().addListener((o, ov, nv) -> updateSaveBtn.run());
        email.textProperty().addListener((o, ov, nv) -> updateSaveBtn.run());
        pwd.textProperty().addListener((o, ov, nv) -> updateSaveBtn.run());
        cin.textProperty().addListener((o, ov, nv) -> updateSaveBtn.run());
        tfTelephone.textProperty().addListener((o, ov, nv) -> updateSaveBtn.run());
        role.valueProperty().addListener((o, ov, nv) -> updateSaveBtn.run());

        // Validation initiale
        updateSaveBtn.run();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Double vérification avec assert avant insertion
                String nomVal   = nom.getText().trim();
                String prenomVal = prenom.getText().trim();
                String emailVal  = email.getText().trim();
                String pwdVal    = pwd.getText();
                String cinVal    = cin.getText().trim();
                String telVal    = tfTelephone.getText().trim();
                String roleVal   = role.getValue();

                // Assertions critiques (actives avec -ea)
                assert !nomVal.isEmpty()   : "Assertion échec : nom vide";
                assert !prenomVal.isEmpty() : "Assertion échec : prénom vide";
                assert emailVal.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$") : "Assertion échec : email invalide";
                assert pwdVal.length() >= 6 : "Assertion échec : mot de passe < 6 chars";
                assert cinVal.matches("\\d{8}")  : "Assertion échec : CIN invalide";
                assert telVal.matches("\\d{8}")  : "Assertion échec : téléphone invalide";
                assert roleVal != null && !roleVal.isEmpty() : "Assertion échec : rôle vide";

                int fId = 0;
                try { fId = Integer.parseInt(filiereId.getText()); } catch(Exception e){}
                return new Utilisateur(existingUser != null ? existingUser.getId() : 0,
                        nomVal, prenomVal, emailVal, pwdVal,
                        cinVal, telVal, roleVal, statut.getValue(),
                        fId, dateInscr.getValue());
            }
            return null;
        });

        Optional<Utilisateur> result = dialog.showAndWait();
        result.ifPresent(u -> {
            if (existingUser == null) {
                // Insert
                String q = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, cin, telephone, role, statut, filiere_id, date_inscription) VALUES (?,?,?,?,?,?,?,?,?,?)";
                executeInsertUpdate(q, u, false);
            } else {
                // Update
                String q = "UPDATE utilisateur SET nom=?, prenom=?, email=?, mot_de_passe=?, cin=?, telephone=?, role=?, statut=?, filiere_id=?, date_inscription=? WHERE id=?";
                executeInsertUpdate(q, u, true);
            }
        });
    }

    private void executeInsertUpdate(String query, Utilisateur u, boolean isUpdate) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getMotDePasse());
            ps.setString(5, u.getCin());
            ps.setString(6, u.getTelephone());
            ps.setString(7, u.getRole());
            ps.setString(8, u.getStatut());
            ps.setInt(9, u.getFiliereId());
            if (u.getDateInscription() != null) ps.setDate(10, Date.valueOf(u.getDateInscription()));
            else ps.setNull(10, Types.DATE);
            
            if (isUpdate) ps.setInt(11, u.getId());

            ps.executeUpdate();
            loadDataAndRefresh();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void goToUniversites(ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/tn/esprit/universite.fxml"));
            tableView.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/tn/esprit/dashboard.fxml"));
            tableView.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
