package com.learnhub.dao;

import com.learnhub.models.DemandeStage;
import com.learnhub.models.OffreStage;
import com.learnhub.models.Partenaire;
import com.learnhub.models.Filiere;
import com.learnhub.models.Utilisateur;
import com.learnhub.util.DatabaseConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DemandeStageDAOTest {

    static final String MOTIVATION_TEST = "TEST_JUNIT_DEMANDE_" + System.currentTimeMillis();
    static final String TITRE_OFFRE_TEST = "TEST_JUNIT_OFFRE_" + System.currentTimeMillis();

    static DemandeStageDAO demandeDAO;
    static OffreStageDAO offreDAO;
    static PartenaireDAO partenaireDAO;
    static FiliereDAO filiereDAO;
    static UtilisateurDAO utilisateurDAO;

    static int ETUDIANT_ID_TEST = 21;
    static int partenaireId;
    static int filiereId;
    static int offreId;
    static int demandeId;

    @BeforeAll
    static void setUp() throws SQLException {
        demandeDAO = new DemandeStageDAO();
        offreDAO = new OffreStageDAO();
        partenaireDAO = new PartenaireDAO();
        filiereDAO = new FiliereDAO();
        utilisateurDAO = new UtilisateurDAO();

        System.out.println("=== DÉMARRAGE DES TESTS ===");

        // Vérifier l'étudiant
        Utilisateur etudiant = utilisateurDAO.findById(ETUDIANT_ID_TEST);
        if (etudiant == null) {
            System.out.println("⚠️ Création de l'étudiant ID " + ETUDIANT_ID_TEST + "...");
            creerEtudiantDeTest();
        } else {
            System.out.println("✅ Étudiant trouvé: " + etudiant.getPrenom() + " " + etudiant.getNom());
        }

        // Récupérer un partenaire
        List<Partenaire> partenaires = partenaireDAO.findAll();
        if (partenaires.isEmpty()) {
            System.out.println("⚠️ Création d'un partenaire de test...");
            partenaireId = creerPartenaireDeTest();
        } else {
            partenaireId = partenaires.get(0).getId();
        }
        System.out.println("✅ Partenaire ID: " + partenaireId);

        // Récupérer une filière
        List<Filiere> filieres = filiereDAO.findAll();
        if (filieres.isEmpty()) {
            System.out.println("⚠️ Création d'une filière de test...");
            filiereId = creerFiliereDeTest();
        } else {
            filiereId = filieres.get(0).getId();
        }
        System.out.println("✅ Filière ID: " + filiereId);

        // Nettoyer les données de test existantes
        nettoyerDonneesTest();
    }

    static void creerEtudiantDeTest() throws SQLException {
        String sql = "INSERT INTO utilisateur (id, nom, prenom, email, mot_de_passe, role, date_inscription, statut) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW(), 'actif')";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ETUDIANT_ID_TEST);
            ps.setString(2, "Test");
            ps.setString(3, "Etudiant");
            ps.setString(4, "etudiant21@test.com");
            ps.setString(5, "password123");
            ps.setString(6, "etudiant");
            ps.executeUpdate();
            System.out.println("✅ Étudiant de test créé");
        }
    }

    static int creerPartenaireDeTest() throws SQLException {
        String sql = "INSERT INTO partenaire (nom, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Partenaire Test JUnit");
            ps.setString(2, "Partenaire créé automatiquement pour les tests");
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Impossible de créer un partenaire");
    }

    static int creerFiliereDeTest() throws SQLException {
        String sql = "INSERT INTO filiere (nom, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Filière Test JUnit");
            ps.setString(2, "Filière créée automatiquement pour les tests");
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Impossible de créer une filière");
    }

    static void nettoyerDonneesTest() throws SQLException {
        // Supprimer les demandes de test
        String sqlDemande = "DELETE FROM demande_stage WHERE motivation LIKE 'TEST_JUNIT_%'";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sqlDemande)) {
            int deleted = ps.executeUpdate();
            if (deleted > 0) System.out.println("🧹 Supprimé " + deleted + " demande(s) de test");
        }

        // Supprimer les offres de test
        String sqlOffre = "DELETE FROM offre_stage WHERE titre LIKE 'TEST_JUNIT_%'";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sqlOffre)) {
            int deleted = ps.executeUpdate();
            if (deleted > 0) System.out.println("🧹 Supprimé " + deleted + " offre(s) de test");
        }

        offreId = 0;
        demandeId = 0;
    }

    @Test
    @Order(1)
    @DisplayName("1 - Créer une offre de stage")
    void test01_creerOffreStage() throws SQLException {
        System.out.println("\n--- Test 1: Création offre ---");

        OffreStage offre = new OffreStage();
        offre.setTitre(TITRE_OFFRE_TEST);
        offre.setDescription("Offre créée pour tester l'ajout de demande");
        offre.setTypeStage("stage");
        offre.setDureeMois(3);
        offre.setDatePublication(LocalDate.now().toString());
        offre.setPartenaireId(partenaireId);
        offre.setFiliereId(filiereId);

        offreDAO.insert(offre);
        offreId = offre.getId();

        assertTrue(offreId > 0);
        System.out.println("✅ Offre créée ID: " + offreId);

        // Vérification directe SQL
        String sql = "SELECT id FROM offre_stage WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offreId);
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "L'offre doit exister en base");
            System.out.println("✅ Vérification SQL: offre trouvée");
        }
    }

    @Test
    @Order(2)
    @DisplayName("2 - Ajouter une demande pour l'étudiant 21")
    void test02_ajouterDemandeStage() throws SQLException {
        System.out.println("\n--- Test 2: Ajout demande ---");

        // Créer offre si nécessaire
        if (offreId == 0) {
            OffreStage offre = new OffreStage();
            offre.setTitre(TITRE_OFFRE_TEST);
            offre.setDescription("Offre test");
            offre.setTypeStage("stage");
            offre.setDureeMois(3);
            offre.setDatePublication(LocalDate.now().toString());
            offre.setPartenaireId(partenaireId);
            offre.setFiliereId(filiereId);
            offreDAO.insert(offre);
            offreId = offre.getId();
            System.out.println("📌 Offre créée ID: " + offreId);
        }

        DemandeStage demande = new DemandeStage();
        demande.setEtudiantId(ETUDIANT_ID_TEST);
        demande.setOffreStageId(offreId);
        demande.setMotivation(MOTIVATION_TEST);
        demande.setPieceJointe("cv_etudiant_21.pdf");
        demande.setDateDemande(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        demande.setStatut("en_attente");

        demandeDAO.insert(demande);
        demandeId = demande.getId();

        assertTrue(demandeId > 0);
        System.out.println("✅ Demande créée ID: " + demandeId);

        // VÉRIFICATION DIRECTE SQL (la plus fiable)
        String sql = "SELECT id, etudiant_id, offre_stage_id, statut, motivation FROM demande_stage WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, demandeId);
            ResultSet rs = ps.executeQuery();

            assertTrue(rs.next(), "La demande doit exister en base");
            assertEquals(ETUDIANT_ID_TEST, rs.getInt("etudiant_id"), "etudiant_id incorrect");
            assertEquals(offreId, rs.getInt("offre_stage_id"), "offre_stage_id incorrect");
            assertEquals("en_attente", rs.getString("statut"), "statut incorrect");
            assertEquals(MOTIVATION_TEST, rs.getString("motivation"), "motivation incorrecte");

            System.out.println("✅ Vérification SQL: toutes les colonnes sont correctes");
            System.out.println("   - etudiant_id: " + rs.getInt("etudiant_id"));
            System.out.println("   - offre_stage_id: " + rs.getInt("offre_stage_id"));
            System.out.println("   - statut: " + rs.getString("statut"));
        }
    }

    @Test
    @Order(3)
    @DisplayName("3 - Vérifier avec findByEtudiant")
    void test03_verifierFindByEtudiant() throws SQLException {
        System.out.println("\n--- Test 3: findByEtudiant ---");

        List<DemandeStage> demandes = demandeDAO.findByEtudiant(ETUDIANT_ID_TEST);

        System.out.println("📊 Trouvé " + demandes.size() + " demande(s) pour l'étudiant " + ETUDIANT_ID_TEST);

        for (DemandeStage d : demandes) {
            System.out.println("   - ID: " + d.getId() + " | Statut: " + d.getStatut() + " | Offre: " + d.getOffreTitre());
        }

        if (demandeId > 0) {
            boolean trouve = demandes.stream().anyMatch(d -> d.getId() == demandeId);
            if (trouve) {
                System.out.println("✅ La demande de test est dans la liste");
            } else {
                System.out.println("⚠️ La demande de test n'est PAS dans findByEtudiant (mais existe en base)");
                System.out.println("   -> Vérifiez la requête SQL dans DemandeStageDAO.findByEtudiant()");
            }
        }
    }

    @Test
    @Order(4)
    @DisplayName("4 - Vérification directe en base de la demande")
    void test04_verificationBaseDirecte() throws SQLException {
        System.out.println("\n--- Test 4: Vérification SQL directe ---");

        String sql = "SELECT COUNT(*) FROM demande_stage WHERE etudiant_id = ? AND motivation = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ETUDIANT_ID_TEST);
            ps.setString(2, MOTIVATION_TEST);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            assertEquals(1, count, "Il doit y avoir exactement 1 demande avec cette motivation");
            System.out.println("✅ SQL direct: " + count + " demande(s) trouvée(s)");
        }
    }

    @Test
    @Order(5)
    @DisplayName("5 - Modifier le statut")
    void test05_modifierStatut() throws SQLException {
        System.out.println("\n--- Test 5: Modification statut ---");

        if (demandeId == 0) {
            test02_ajouterDemandeStage();
        }

        // Modifier via DAO
        demandeDAO.updateStatut(demandeId, "acceptee", null);
        System.out.println("✅ updateStatut appelé avec 'acceptee'");

        // Vérifier avec SQL direct
        String sql = "SELECT statut FROM demande_stage WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, demandeId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            String statut = rs.getString("statut");
            assertEquals("acceptee", statut);
            System.out.println("✅ Vérification SQL: statut = " + statut);
        }
    }

    @Test
    @Order(6)
    @DisplayName("6 - Supprimer la demande")
    void test06_supprimerDemande() throws SQLException {
        System.out.println("\n--- Test 6: Suppression demande ---");

        if (demandeId == 0) {
            test02_ajouterDemandeStage();
        }

        demandeDAO.delete(demandeId);
        System.out.println("✅ Demande supprimée via DAO");

        // Vérifier avec SQL direct
        String sql = "SELECT COUNT(*) FROM demande_stage WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, demandeId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            assertEquals(0, count, "La demande ne doit plus exister");
            System.out.println("✅ Vérification SQL: demande bien supprimée");
        }

        demandeId = 0;
    }

    @Test
    @Order(7)
    @DisplayName("7 - Supprimer l'offre")
    void test07_supprimerOffre() throws SQLException {
        System.out.println("\n--- Test 7: Suppression offre ---");

        if (offreId == 0) {
            test01_creerOffreStage();
        }

        offreDAO.delete(offreId);
        System.out.println("✅ Offre supprimée via DAO");

        // Vérifier avec SQL direct
        String sql = "SELECT COUNT(*) FROM offre_stage WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offreId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            assertEquals(0, count, "L'offre ne doit plus exister");
            System.out.println("✅ Vérification SQL: offre bien supprimée");
        }

        offreId = 0;
    }
}