package com.learnhub.dao;

import com.learnhub.models.Seance;
import com.learnhub.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SeanceDAOTest {

    private SeanceDAO seanceDAO;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private static MockedStatic<DatabaseConnection> mockedStatic;

    @BeforeAll
    static void setupGlobal() {
        mockedStatic = mockStatic(DatabaseConnection.class);
    }

    @AfterAll
    static void tearDownGlobal() {
        mockedStatic.close();
    }

    @BeforeEach
    void setUp() throws SQLException {
        seanceDAO = new SeanceDAO();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockedStatic.when(DatabaseConnection::getInstance).thenReturn(mockConnection);
    }

    @Test
    void testFindAll() throws SQLException {
        Statement mockStatement = mock(Statement.class);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("module_titre")).thenReturn("JavaFX");

        List<Seance> result = seanceDAO.findAll();

        assertFalse(result.isEmpty());
        assertEquals("JavaFX", result.get(0).getModuleTitre());
        verify(mockStatement).executeQuery(contains("SELECT s.*"));
    }

    @Test
    void testInsert() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(20);

        Seance s = new Seance();
        s.setModuleId(1);
        s.setEnseignantId(1);
        s.setDate("2026-04-12");
        s.setHeureDebut("09:00");
        s.setHeureFin("12:00");
        s.setSalle("A101");
        s.setType("Cours");

        seanceDAO.insert(s);

        assertEquals(20, s.getId());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testUpdate() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        Seance s = new Seance();
        s.setId(10);
        s.setSalle("B202");

        seanceDAO.update(s);

        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).setInt(8, 10);
    }

    @Test
    void testDelete() throws SQLException {

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        seanceDAO.delete(500);


        verify(mockPreparedStatement, times(2)).executeUpdate();
        verify(mockPreparedStatement, times(2)).setInt(1, 500);
    }
}
