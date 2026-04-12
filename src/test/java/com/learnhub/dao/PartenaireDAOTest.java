package com.learnhub.dao;

import com.learnhub.models.Partenaire;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PartenaireDAOTest {

    static PartenaireDAO dao;
    static Partenaire testPartenaire;

    @BeforeAll
    static void setup() {
        dao = new PartenaireDAO();
        testPartenaire = new Partenaire();
        testPartenaire.setNom("TEST_JUNIT_PT");
        testPartenaire.setSecteur("Technologie");
        testPartenaire.setVille("Tunis");
        testPartenaire.setEmail("test_junit_pt@learnhub.test");
        testPartenaire.setTelephone("00000000");
        testPartenaire.setStatut("actif");
        testPartenaire.setAdresse("Adresse test");
        testPartenaire.setPays("TN");
        testPartenaire.setWebsite("https://learnhub.test");
        testPartenaire.setDescription("Partenaire cree par les tests unitaires");
    }

    @Test
    @Order(1)
    void testAjouterPartenaire() throws SQLException {
        dao.insert(testPartenaire);
        List<Partenaire> list = dao.findAll();
        boolean found = false;
        for (Partenaire p : list) {
            if ("TEST_JUNIT_PT".equals(p.getNom())) {
                testPartenaire.setId(p.getId());
                found = true;
                break;
            }
        }
        assertTrue(found, "Le partenaire de test n'a pas ete trouve apres insertion");
        assertTrue(testPartenaire.getId() > 0);
    }

    @Test
    @Order(2)
    void testRecherchePartenaire() throws SQLException {
        List<Partenaire> r = dao.search("TEST_JUNIT_PT", null, null);
        assertFalse(r.isEmpty(), "La recherche par nom devrait retourner au moins un resultat");
    }

    @Test
    @Order(3)
    void testModifierPartenaire() throws SQLException {
        if (testPartenaire.getId() == 0) {
            for (Partenaire p : dao.findAll()) {
                if ("TEST_JUNIT_PT".equals(p.getNom())) {
                    testPartenaire.setId(p.getId());
                    break;
                }
            }
        }
        testPartenaire.setVille("Sfax");
        dao.update(testPartenaire);
        List<Partenaire> list = dao.findAll();
        boolean ok = list.stream().anyMatch(p -> p.getId() == testPartenaire.getId() && "Sfax".equals(p.getVille()));
        assertTrue(ok, "La ville du partenaire n'a pas ete mise a jour");
    }

    @Test
    @Order(4)
    void testSupprimerPartenaire() throws SQLException {
        if (testPartenaire.getId() == 0) {
            for (Partenaire p : dao.findAll()) {
                if ("TEST_JUNIT_PT".equals(p.getNom())) {
                    testPartenaire.setId(p.getId());
                    break;
                }
            }
        }
        dao.delete(testPartenaire.getId());
        boolean still = dao.findAll().stream().anyMatch(p -> p.getId() == testPartenaire.getId());
        assertFalse(still, "Le partenaire n'a pas ete supprime");
    }
}
