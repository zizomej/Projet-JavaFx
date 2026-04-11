package com.learnhub.dao;

import com.learnhub.models.Module;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ModuleDAOTest {
    
    static ModuleDAO dao;
    static Module testModule;

    @BeforeAll
    static void setup() {
        dao = new ModuleDAO();
        testModule = new Module();
        testModule.setCode("MDL10"); // Nom court pour eviter les erreurs de longueur
        testModule.setIntitule("Module Simple");
        testModule.setSemestre(1);
        testModule.setCredits(5);
        
        // IDs par défaut (selon votre base de données)
        testModule.setFiliere_id(1); 
        testModule.setResponsable_id(20); // 20 = Hedi
    }

    @Test
    @Order(1)
    void testAjouterModule() throws SQLException {
        dao.add(testModule);
        
        List<Module> modules = dao.findAll();
        for(Module m : modules) {
            if("MDL10".equals(m.getCode())) {
                testModule.setId(m.getId());
                break;
            }
        }
        assertTrue(testModule.getId() > 0, "Le module n'a pas été ajouté");
    }

    @Test
    @Order(2)
    void testAfficherModules() throws SQLException {
        List<Module> modules = dao.findAll();
        assertFalse(modules.isEmpty(), "La liste des modules est vide");
    }

    @Test
    @Order(3)
    void testModifierModule() throws SQLException {
        // Au cas où ce test est exécuté tout seul (séparément de l'ajout)
        if (testModule.getId() == 0) {
            for(Module m : dao.findAll()) {
                if("MDL10".equals(m.getCode())) {
                    testModule.setId(m.getId());
                    break;
                }
            }
        }

        testModule.setIntitule("Module Modifie");
        dao.update(testModule);
        
        boolean isModified = false;
        for (Module m : dao.findAll()) {
             if (m.getId() == testModule.getId() && "Module Modifie".equals(m.getIntitule())) {
                 isModified = true;
                 break;
             }
        }
        assertTrue(isModified, "Le module n'a pas été modifié");
    }

    @Test
    @Order(4)
    void testSupprimerModule() throws SQLException {
        // Au cas où ce test est exécuté tout seul
        if (testModule.getId() == 0) {
            for(Module m : dao.findAll()) {
                if("MDL10".equals(m.getCode())) {
                    testModule.setId(m.getId());
                    break;
                }
            }
        }

        dao.delete(testModule.getId());
        
        boolean stillExists = dao.findAll().stream().anyMatch(m -> m.getId() == testModule.getId());
        assertFalse(stillExists, "Le module n'a pas été supprimé");
    }
}
