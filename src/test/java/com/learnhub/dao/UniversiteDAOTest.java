package com.learnhub.dao;

import com.learnhub.models.Universite;
import com.learnhub.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UniversiteDAOTest {

    private UniversiteDAO universiteDAO;
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
        universiteDAO = new UniversiteDAO();
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
        when(mockResultSet.getString("nom")).thenReturn("Université Test");

        List<Universite> result = universiteDAO.findAll();

        assertFalse(result.isEmpty());
        assertEquals("Université Test", result.get(0).getNom());
        verify(mockStatement).executeQuery(contains("SELECT * FROM universite"));
    }

    @Test
    void testInsert() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(10);

        Universite u = new Universite();
        u.setNom("UTEST");
        u.setType("Publique");
        u.setVille("Tunis");

        universiteDAO.insert(u);

        assertEquals(10, u.getId());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testUpdate() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        Universite u = new Universite();
        u.setId(5);
        u.setNom("Updated");

        universiteDAO.update(u);

        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).setInt(7, 5);
    }

    @Test
    void testDelete() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        universiteDAO.delete(100);

        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).setInt(1, 100);
    }
}
