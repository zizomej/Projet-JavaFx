package com.learnhub.dao;

import com.learnhub.models.Filiere;
import com.learnhub.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class FiliereDAOTest {

    private FiliereDAO filiereDAO;
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
        filiereDAO = new FiliereDAO();
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
        when(mockResultSet.getString("nom")).thenReturn("Génie Logiciel");

        List<Filiere> result = filiereDAO.findAll();

        assertFalse(result.isEmpty());
        assertEquals("Génie Logiciel", result.get(0).getNom());
        verify(mockStatement).executeQuery(contains("SELECT * FROM filiere"));
    }

    @Test
    void testInsert() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(50);

        Filiere f = new Filiere();
        f.setNom("Informatique");
        f.setCode("INF");
        f.setNiveau("Licence");

        filiereDAO.insert(f);

        assertEquals(50, f.getId());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testUpdate() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        Filiere f = new Filiere();
        f.setId(12);
        f.setNom("Mathématiques");

        filiereDAO.update(f);

        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).setInt(8, 12);
    }

    @Test
    void testDelete() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        filiereDAO.delete(99);

        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).setInt(1, 99);
    }
}
