package com.learnhub.dao;

import com.learnhub.models.Filiere;
import com.learnhub.models.OffreStage;
import com.learnhub.models.Partenaire;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OffreStageDAOTest {

    static OffreStageDAO dao;
    static PartenaireDAO partenaireDAO;
    static FiliereDAO filiereDAO;
    static OffreStage testOffre;
    static int partenaireId;
    static int filiereId;

    @BeforeAll
    static void setup() throws SQLException {
        dao = new OffreStageDAO();
        partenaireDAO = new PartenaireDAO();
        filiereDAO = new FiliereDAO();

        List<Partenaire> partenaires = partenaireDAO.findAll();
        Assumptions.assumeFalse(partenaires.isEmpty(), "Au moins un partenaire est requis pour tester les offres");
        partenaireId = partenaires.get(0).getId();

        List<Filiere> filieres = filiereDAO.findAll();
        Assumptions.assumeFalse(filieres.isEmpty(), "Au moins une filiere est requise (filiere_id NOT NULL sur offre_stage)");
        filiereId = filieres.get(0).getId();

        testOffre = new OffreStage();
        testOffre.setTitre("TEST_JUNIT_OFFRE");
        testOffre.setDescription("Description test offre");
        testOffre.setTypeStage("observation");
        testOffre.setDureeMois(2);
        testOffre.setDatePublication(LocalDate.now().toString());
        testOffre.setPartenaireId(partenaireId);
        testOffre.setFiliereId(filiereId);
    }

    @Test
    @Order(1)
    void testAjouterOffre() throws SQLException {
        dao.insert(testOffre);
        List<OffreStage> list = dao.findAll();
        boolean found = false;
        for (OffreStage o : list) {
            if ("TEST_JUNIT_OFFRE".equals(o.getTitre())) {
                testOffre.setId(o.getId());
                found = true;
                break;
            }
        }
        assertTrue(found, "L'offre de test n'a pas ete trouvee apres insertion");
        assertTrue(testOffre.getId() > 0);
    }

    @Test
    @Order(2)
    void testRechercheOffre() throws SQLException {
        List<OffreStage> r = dao.search("TEST_JUNIT_OFFRE", "", null);
        assertFalse(r.isEmpty(), "La recherche devrait trouver l'offre de test");
    }

    @Test
    @Order(3)
    void testRechercheSansResultat() throws SQLException {
        List<OffreStage> r = dao.search("__aucune_offre_xyz_999__", "", null);
        assertTrue(r.isEmpty());
    }

    @Test
    @Order(4)
    void testModifierOffre() throws SQLException {
        if (testOffre.getId() == 0) {
            for (OffreStage o : dao.findAll()) {
                if ("TEST_JUNIT_OFFRE".equals(o.getTitre())) {
                    testOffre.setId(o.getId());
                    break;
                }
            }
        }
        testOffre.setDescription("Description modifiee");
        dao.update(testOffre);
        List<OffreStage> list = dao.findAll();
        boolean ok = list.stream().anyMatch(o -> o.getId() == testOffre.getId()
                && "Description modifiee".equals(o.getDescription()));
        assertTrue(ok, "L'offre n'a pas ete mise a jour");
    }

    @Test
    @Order(5)
    void testSupprimerOffre() throws SQLException {
        if (testOffre.getId() == 0) {
            for (OffreStage o : dao.findAll()) {
                if ("TEST_JUNIT_OFFRE".equals(o.getTitre())) {
                    testOffre.setId(o.getId());
                    break;
                }
            }
        }
        dao.delete(testOffre.getId());
        boolean still = dao.findAll().stream().anyMatch(o -> o.getId() == testOffre.getId());
        assertFalse(still, "L'offre n'a pas ete supprimee");
    }
}
