package edu.utdallas.cs3354.whatt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 tests for DBMgr.
 *
 * The JDBC layer (Connection, PreparedStatement, ResultSet) is fully mocked so
 * these tests run without a real database.
*/
@DisplayName("DBMgr Tests")
class DBMgrTest {


    // Testable subclass — injects a mock Connection

    private static class TestablDBMgr extends DBMgr {
        private final Connection mockConnection;

        TestablDBMgr(Connection mockConnection) {
            this.mockConnection = mockConnection;
        }

        @Override
        protected Connection getConnection() {
            return mockConnection;
        }
    }


    // Shared mocks

    private Connection        mockConnection;
    private PreparedStatement mockStmt;
    private ResultSet         mockRs;
    private DBMgr             dbMgr;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = Mockito.mock(Connection.class);
        mockStmt       = Mockito.mock(PreparedStatement.class);
        mockRs         = Mockito.mock(ResultSet.class);
        dbMgr          = new TestablDBMgr(mockConnection);

        // Default stub — prepareStatement always returns the shared mock stmt
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);
    }

    // =========================================================================
    // validateCredentials
    // =========================================================================

    // T20 — correct credentials
    @Test
    @DisplayName("T20: correct username and password returns true")
    void validateCredentials_CorrectCredentials_ReturnsTrue() throws SQLException {
        // Simulate a row where the stored hash matches and account is active.
        // DBMgr hashes with Integer.toHexString(password.hashCode()), so we
        // pre-compute the expected hash for "pass123".
        String expectedHash = Integer.toHexString("pass123".hashCode());

        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString("password_hash")).thenReturn(expectedHash);
        when(mockRs.getBoolean("is_active")).thenReturn(true);

        boolean result = dbMgr.validateCredentials("alice", "pass123");

        assertTrue(result);
    }

    // T21 — wrong password
    @Test
    @DisplayName("T21: wrong password returns false")
    void validateCredentials_WrongPassword_ReturnsFalse() throws SQLException {
        String storedHash = Integer.toHexString("pass123".hashCode());

        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString("password_hash")).thenReturn(storedHash);
        when(mockRs.getBoolean("is_active")).thenReturn(true);

        // Pass a different password — hash will not match
        boolean result = dbMgr.validateCredentials("alice", "wrongpass");

        assertFalse(result);
    }

    // T22 — unknown username (no row returned)
    @Test
    @DisplayName("T22: unknown username returns false")
    void validateCredentials_UnknownUser_ReturnsFalse() throws SQLException {
        when(mockRs.next()).thenReturn(false);   // no matching row

        boolean result = dbMgr.validateCredentials("unknown", "pass123");

        assertFalse(result);
    }

    // T23 — null username
    @Test
    @DisplayName("T23: null username returns false without throwing")
    void validateCredentials_NullUsername_ReturnsFalse() throws SQLException {
        // PreparedStatement.setString(1, null) is valid JDBC;
        // the query simply returns no rows.
        when(mockRs.next()).thenReturn(false);

        boolean result = dbMgr.validateCredentials(null, "pass123");

        assertFalse(result);
    }


    // getUserRole


    @Test
    @DisplayName("getUserRole: returns role string for known user")
    void getUserRole_KnownUser_ReturnsRole() throws SQLException {
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString("role")).thenReturn("ADMIN");

        String role = dbMgr.getUserRole("alice");

        assertEquals("ADMIN", role);
    }

    @Test
    @DisplayName("getUserRole: returns null for unknown user")
    void getUserRole_UnknownUser_ReturnsNull() throws SQLException {
        when(mockRs.next()).thenReturn(false);

        String role = dbMgr.getUserRole("ghost");

        assertNull(role);
    }


    // updatePassword


    @Test
    @DisplayName("updatePassword: successful update returns true")
    void updatePassword_ValidUser_ReturnsTrue() throws SQLException {
        when(mockStmt.executeUpdate()).thenReturn(1);   // 1 row affected

        boolean result = dbMgr.updatePassword("alice", "newpass1");

        assertTrue(result);
    }

    @Test
    @DisplayName("updatePassword: no matching user returns false")
    void updatePassword_UnknownUser_ReturnsFalse() throws SQLException {
        when(mockStmt.executeUpdate()).thenReturn(0);   // 0 rows affected

        boolean result = dbMgr.updatePassword("ghost", "newpass1");

        assertFalse(result);
    }
}
