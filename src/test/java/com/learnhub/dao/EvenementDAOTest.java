package com.learnhub.dao;

import com.learnhub.models.Evenement;
import com.learnhub.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class EvenementDAOTest {

    private EvenementDAO evenementDAO;
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
        evenementDAO = new EvenementDAO();
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
        when(mockResultSet.getString("titre")).thenReturn("Conférence AI");

        List<Evenement> result = evenementDAO.findAll();

        assertFalse(result.isEmpty());
        assertEquals("Conférence AI", result.get(0).getTitre());
        verify(mockStatement).executeQuery(contains("SELECT e.*"));
    }

    @Test
    void testInsert() throws SQLException {
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(101);

        Evenement e = new Evenement();
        e.setTitre("Gala");
        e.setDateDebut(LocalDate.now());
        e.setDateFin(LocalDate.now());
        e.setHeureDebut(LocalTime.of(18, 0));
        e.setHeureFin(LocalTime.of(22, 0));
        e.setLieuId(1);
        e.setStatut("Planifié");

        evenementDAO.insert(e);

        assertEquals(101, e.getId());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void testUpdate() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        Evenement e = new Evenement();
        e.setId(55);
        e.setTitre("Updated Gala");
        e.setDateDebut(LocalDate.now());
        e.setDateFin(LocalDate.now());
        e.setHeureDebut(LocalTime.of(18, 0));
        e.setHeureFin(LocalTime.of(22, 0));

        evenementDAO.update(e);

        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement).setInt(10, 55);
    }

    @Test
    void testDelete() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        evenementDAO.delete(202);

        verify(mockPreparedStatement).executeUpdate();
        verify(mockPreparedStatement, times(1)).setInt(1, 202);
    }
}
