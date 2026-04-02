package edu.utdallas.cs3354.whatt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 tests for UserController.
 *
 * DBMgr is mocked so these tests exercise only the controller's
 * session-management logic — not the database layer.
 */
@DisplayName("UserController Tests")
class UserControllerTest {

    private DBMgr mockDbMgr;
    private UserController controller;

    @BeforeEach
    void setUp() {
        mockDbMgr    = Mockito.mock(DBMgr.class);
        controller   = new UserController(mockDbMgr);   // constructor injection
    }

    // =========================================================================
    // verifyLogin
    // =========================================================================

    // T16 — valid credentials
    @Test
    @DisplayName("T16: valid credentials returns true and sets current user")
    void verifyLogin_ValidCredentials_ReturnsTrueAndSetsUser() {
        when(mockDbMgr.validateCredentials("alice", "pass123")).thenReturn(true);
        when(mockDbMgr.getUserRole("alice")).thenReturn("USER");

        boolean result = controller.verifyLogin("alice", "pass123");

        assertTrue(result);
        assertTrue(controller.isLoggedIn());
        assertNotNull(controller.getCurrentUser());
        assertEquals("alice", controller.getCurrentUser().getUsername());
    }

    // T17 — wrong password
    @Test
    @DisplayName("T17: wrong password returns false and leaves session empty")
    void verifyLogin_WrongPassword_ReturnsFalse() {
        when(mockDbMgr.validateCredentials("alice", "wrongpass")).thenReturn(false);

        boolean result = controller.verifyLogin("alice", "wrongpass");

        assertFalse(result);
        assertFalse(controller.isLoggedIn());
        assertNull(controller.getCurrentUser());
    }

    // T18 — blank username
    @Test
    @DisplayName("T18: blank username is rejected before hitting the DB")
    void verifyLogin_BlankUsername_ReturnsFalse() {
        boolean result = controller.verifyLogin("", "pass123");

        assertFalse(result);
        // DB must never be called with blank input
        verify(mockDbMgr, never()).validateCredentials(anyString(), anyString());
    }

    // T19 — null username
    @Test
    @DisplayName("T19: null username is rejected before hitting the DB")
    void verifyLogin_NullUsername_ReturnsFalse() {
        boolean result = controller.verifyLogin(null, "pass123");

        assertFalse(result);
        verify(mockDbMgr, never()).validateCredentials(anyString(), anyString());
    }

    // =========================================================================
    // logout
    // =========================================================================

    @Test
    @DisplayName("logout: clears session and records logout event")
    void logout_ActiveSession_ClearsSessionAndReturnsTrue() {
        // Arrange — establish a session first
        when(mockDbMgr.validateCredentials("alice", "pass123")).thenReturn(true);
        when(mockDbMgr.getUserRole("alice")).thenReturn("USER");
        controller.verifyLogin("alice", "pass123");

        // Act
        boolean result = controller.logout();

        // Assert
        assertTrue(result);
        assertFalse(controller.isLoggedIn());
        assertNull(controller.getCurrentUser());
        verify(mockDbMgr, times(1)).logLogoutEvent(anyString());
    }

    @Test
    @DisplayName("logout: returns false with no active session")
    void logout_NoSession_ReturnsFalse() {
        boolean result = controller.logout();

        assertFalse(result);
        verify(mockDbMgr, never()).logLogoutEvent(anyString());
    }

    // =========================================================================
    // changePassword
    // =========================================================================

    @Test
    @DisplayName("changePassword: correct old password updates successfully")
    void changePassword_CorrectOldPassword_ReturnsTrue() {
        // Establish session
        when(mockDbMgr.validateCredentials("alice", "pass123")).thenReturn(true);
        when(mockDbMgr.getUserRole("alice")).thenReturn("USER");
        controller.verifyLogin("alice", "pass123");

        when(mockDbMgr.validateCredentials("alice", "pass123")).thenReturn(true);
        when(mockDbMgr.updatePassword("alice", "newpass1")).thenReturn(true);

        boolean result = controller.changePassword("pass123", "newpass1");

        assertTrue(result);
    }

    @Test
    @DisplayName("changePassword: wrong old password is rejected")
    void changePassword_WrongOldPassword_ReturnsFalse() {
        when(mockDbMgr.validateCredentials("alice", "pass123")).thenReturn(true);
        when(mockDbMgr.getUserRole("alice")).thenReturn("USER");
        controller.verifyLogin("alice", "pass123");

        when(mockDbMgr.validateCredentials("alice", "wrongold")).thenReturn(false);

        boolean result = controller.changePassword("wrongold", "newpass1");

        assertFalse(result);
        verify(mockDbMgr, never()).updatePassword(anyString(), anyString());
    }
}
