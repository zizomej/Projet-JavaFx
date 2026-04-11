package com.learnhub.dao;

import com.learnhub.models.Note;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NoteDAOTest {
    
    static NoteDAO dao;
    static Note testNote;

    @BeforeAll
    static void setup() {
        dao = new NoteDAO();
        testNote = new Note();
        testNote.setValeur(15.5);
        testNote.setTypeNote("N_TST");
        testNote.setCoefficient(2.0);
        testNote.setDateSaisie(LocalDate.now().toString());

        int etudId = 21;
        int profId = 20;
        int modId = 1;
        
        try {
            com.learnhub.dao.ModuleDAO mDao = new com.learnhub.dao.ModuleDAO();
            java.util.List<com.learnhub.models.Module> modules = mDao.findAll();
            if(!modules.isEmpty()) modId = modules.get(0).getId();
        } catch(Exception ignored) {}

        testNote.setEtudiantId(etudId);
        testNote.setEnseignantId(profId);
        testNote.setModuleId(modId);
    }

    @Test
    @Order(1)
    void testAjouterNote() throws SQLException {
        dao.add(testNote);
        
        List<Note> notes = dao.findAll();
        for(Note n : notes) {
            if("N_TST".equals(n.getTypeNote()) && n.getValeur() == 15.5) {
                testNote.setId(n.getId());
                break;
            }
        }
        assertTrue(testNote.getId() > 0, "La note n'a pas été ajoutée");
    }

    @Test
    @Order(2)
    void testAfficherNotes() throws SQLException {
        List<Note> notes = dao.findAll();
        assertFalse(notes.isEmpty(), "La liste des notes est vide");
    }

    @Test
    @Order(3)
    void testModifierNote() throws SQLException {
        if (testNote.getId() == 0) {
            for(Note n : dao.findAll()) {
                if("N_TST".equals(n.getTypeNote())) {
                    testNote.setId(n.getId());
                    break;
                }
            }
        }

        testNote.setValeur(18.0);
        dao.update(testNote);
        
        boolean isModified = false;
        for (Note n : dao.findAll()) {
             if (n.getId() == testNote.getId() && n.getValeur() == 18.0) {
                 isModified = true;
                 break;
             }
        }
        assertTrue(isModified, "La note n'a pas été modifiée");
    }

    @Test
    @Order(4)
    void testSupprimerNote() throws SQLException {
        if (testNote.getId() == 0) {
            for(Note n : dao.findAll()) {
                if("N_TST".equals(n.getTypeNote())) {
                    testNote.setId(n.getId());
                    break;
                }
            }
        }

        dao.delete(testNote.getId());
        
        boolean stillExists = dao.findAll().stream().anyMatch(n -> n.getId() == testNote.getId());
        assertFalse(stillExists, "La note n'a pas été supprimée");
    }
}
