package com.learnhub.medical.repository;

import com.learnhub.medical.entity.RDV;
import com.learnhub.medical.util.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RDVRepositoryTest {

    private RDVRepository repository;
    private MockedStatic<DatabaseConnection> mockedDatabaseConnection;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private Statement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        repository = new RDVRepository();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockStatement = mock(Statement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDatabaseConnection = mockStatic(DatabaseConnection.class);
        mockedDatabaseConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
    }

    @AfterEach
    void tearDown() {
        mockedDatabaseConnection.close();
    }

    @Test
    void testFindAll_Success() throws SQLException {
        // Arrange
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("motif")).thenReturn("Consultation");
        when(mockResultSet.getDate("date_demande")).thenReturn(java.sql.Date.valueOf(LocalDate.now()));

        // Act
        List<RDV> result = repository.findAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Consultation", result.get(0).getMotif());
    }

    @Test
    void testIsCreneauTaken_True() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        // Act
        boolean result = repository.isCreneauTaken(1, LocalDate.now(), 0);

        // Assert
        assertTrue(result);
    }

    @Test
    void testGetStats_Success() throws SQLException {
        // Arrange
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("pending")).thenReturn(5);
        when(mockResultSet.getInt("confirmed")).thenReturn(10);

        // Act
        Map<String, Integer> stats = repository.getStats();

        // Assert
        assertEquals(5, stats.get("pending"));
        assertEquals(10, stats.get("confirmed"));
    }
}
