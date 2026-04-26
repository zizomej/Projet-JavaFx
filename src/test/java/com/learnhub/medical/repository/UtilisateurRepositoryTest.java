package com.learnhub.medical.repository;

import com.learnhub.medical.entity.Utilisateur;
import com.learnhub.medical.util.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UtilisateurRepositoryTest {

    private UtilisateurRepository repository;
    private MockedStatic<DatabaseConnection> mockedDatabaseConnection;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        repository = new UtilisateurRepository();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDatabaseConnection = mockStatic(DatabaseConnection.class);
        mockedDatabaseConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
    }

    @AfterEach
    void tearDown() {
        mockedDatabaseConnection.close();
    }

    @Test
    void testFindByEmail_Success() throws SQLException {
        // Arrange
        String email = "test@example.com";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("email")).thenReturn(email);
        when(mockResultSet.getString("nom")).thenReturn("Doe");
        when(mockResultSet.getString("prenom")).thenReturn("John");
        when(mockResultSet.getString("role")).thenReturn("STUDENT");

        // Act
        Utilisateur result = repository.findByEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("Doe", result.getNom());
        assertEquals("STUDENT", result.getRole());
        verify(mockPreparedStatement).setString(1, email);
    }

    @Test
    void testFindByEmail_NotFound() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Utilisateur result = repository.findByEmail("unknown@example.com");

        // Assert
        assertNull(result);
    }

    @Test
    void testFindAll_ReturnsList() throws SQLException {
        // Arrange
        when(mockConnection.createStatement()).thenReturn(mock(java.sql.Statement.class));
        when(mockConnection.createStatement().executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false); // One result then end
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("email")).thenReturn("user@example.com");

        // Act
        List<Utilisateur> results = repository.findAll();

        // Assert
        assertEquals(1, results.size());
        assertEquals("user@example.com", results.get(0).getEmail());
    }
}
