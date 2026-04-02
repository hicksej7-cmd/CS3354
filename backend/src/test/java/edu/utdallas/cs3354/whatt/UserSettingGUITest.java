package edu.utdallas.cs3354.whatt;

import edu.utdallas.cs3354.whatt.UserSettingGUI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 tests for UserSettingGUI.
 *
 * UserController is mocked so these tests exercise only the GUI layer's
 */
@DisplayName("UserSettingGUI Tests")
class UserSettingGUITest {

    private UserController mockController;
    private UserSettingGUI gui;

    // A reusable user stub returned by the mock on successful login
    private User stubUser;

    @BeforeEach
    void setUp() {
        mockController = Mockito.mock(UserController.class);
        gui = new UserSettingGUI(mockController);   // constructor injection (see note below)
        stubUser = new User("alice", "USER");
    }


    // enterCredentials


    // T01 — valid credentials
    @Test
    @DisplayName("T01: valid username and password returns success")
    void enterCredentials_ValidCredentials_ReturnsSuccess() {
        when(mockController.verifyLogin("alice", "pass123")).thenReturn(true);
        when(mockController.getCurrentUser()).thenReturn(stubUser);

        UserSettingGUI.LoginResponse response = gui.enterCredentials("alice", "pass123");

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("Welcome"));
        assertEquals("alice", response.getUsername());
    }

    // T02 — wrong password
    @Test
    @DisplayName("T02: wrong password returns failure with appropriate message")
    void enterCredentials_WrongPassword_ReturnsFailure() {
        when(mockController.verifyLogin("alice", "wrongpass")).thenReturn(false);

        UserSettingGUI.LoginResponse response = gui.enterCredentials("alice", "wrongpass");

        assertFalse(response.isSuccess());
        assertEquals("Invalid username or password.", response.getMessage());
        assertNull(response.getUsername());
    }

    // T03 — blank username
    @Test
    @DisplayName("T03: blank username returns failure")
    void enterCredentials_BlankUsername_ReturnsFailure() {
        when(mockController.verifyLogin("", "pass123")).thenReturn(false);

        UserSettingGUI.LoginResponse response = gui.enterCredentials("", "pass123");

        assertFalse(response.isSuccess());
    }

    // T04 — blank password
    @Test
    @DisplayName("T04: blank password returns failure")
    void enterCredentials_BlankPassword_ReturnsFailure() {
        when(mockController.verifyLogin("alice", "")).thenReturn(false);

        UserSettingGUI.LoginResponse response = gui.enterCredentials("alice", "");

        assertFalse(response.isSuccess());
    }

    // T05 — null username
    @Test
    @DisplayName("T05: null username returns failure without throwing")
    void enterCredentials_NullUsername_ReturnsFailure() {
        when(mockController.verifyLogin(null, "pass123")).thenReturn(false);

        UserSettingGUI.LoginResponse response = gui.enterCredentials(null, "pass123");

        assertFalse(response.isSuccess());
    }

    // T06 — null password
    @Test
    @DisplayName("T06: null password returns failure without throwing")
    void enterCredentials_NullPassword_ReturnsFailure() {
        when(mockController.verifyLogin("alice", null)).thenReturn(false);

        UserSettingGUI.LoginResponse response = gui.enterCredentials("alice", null);

        assertFalse(response.isSuccess());
    }


    // handleChangePassword


    // T07 — not logged in
    @Test
    @DisplayName("T07: changePassword when not logged in returns failure")
    void handleChangePassword_NotLoggedIn_ReturnsFailure() {
        when(mockController.isLoggedIn()).thenReturn(false);

        UserSettingGUI.LoginResponse response =
                gui.handleChangePassword("pass123", "newpass1", "newpass1");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().toLowerCase().contains("logged in"));
    }

    // T08 — correct old password, matching new/confirm
    @Test
    @DisplayName("T08: valid change password request returns success")
    void handleChangePassword_ValidInputs_ReturnsSuccess() {
        when(mockController.isLoggedIn()).thenReturn(true);
        when(mockController.changePassword("pass123", "newpass1")).thenReturn(true);
        when(mockController.getCurrentUser()).thenReturn(stubUser);

        UserSettingGUI.LoginResponse response =
                gui.handleChangePassword("pass123", "newpass1", "newpass1");

        assertTrue(response.isSuccess());
    }

    // T09 — wrong old password
    @Test
    @DisplayName("T09: wrong old password returns failure with 'incorrect' message")
    void handleChangePassword_WrongOldPassword_ReturnsFailure() {
        when(mockController.isLoggedIn()).thenReturn(true);
        when(mockController.changePassword("wrongold", "newpass1")).thenReturn(false);

        UserSettingGUI.LoginResponse response =
                gui.handleChangePassword("wrongold", "newpass1", "newpass1");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().toLowerCase().contains("incorrect"));
    }

    // T10 — new password does not match confirm
    @Test
    @DisplayName("T10: mismatched new and confirm passwords returns failure")
    void handleChangePassword_MismatchedPasswords_ReturnsFailure() {
        when(mockController.isLoggedIn()).thenReturn(true);

        UserSettingGUI.LoginResponse response =
                gui.handleChangePassword("pass123", "newpass1", "different");

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().toLowerCase().contains("match"));
    }

    // T11 — null confirmPassword
    @Test
    @DisplayName("T11: null confirmPassword returns failure")
    void handleChangePassword_NullConfirm_ReturnsFailure() {
        when(mockController.isLoggedIn()).thenReturn(true);

        UserSettingGUI.LoginResponse response =
                gui.handleChangePassword("pass123", "newpass1", null);

        assertFalse(response.isSuccess());
    }


    // handleLogout


    // T12 — active session
    @Test
    @DisplayName("T12: logout with active session returns success")
    void handleLogout_ActiveSession_ReturnsSuccess() {
        when(mockController.isLoggedIn()).thenReturn(true);
        when(mockController.logout()).thenReturn(true);

        UserSettingGUI.LoginResponse response = gui.handleLogout();

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().toLowerCase().contains("logged out"));
    }

    // T13 — no active session
    @Test
    @DisplayName("T13: logout with no session returns failure")
    void handleLogout_NoSession_ReturnsFailure() {
        when(mockController.isLoggedIn()).thenReturn(false);

        UserSettingGUI.LoginResponse response = gui.handleLogout();

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("No active session"));
    }


    // getSessionStatus


    // T14 — currently logged in
    @Test
    @DisplayName("T14: getSessionStatus while logged in returns user details")
    void getSessionStatus_LoggedIn_ReturnsUserDetails() {
        when(mockController.isLoggedIn()).thenReturn(true);
        when(mockController.getCurrentUser()).thenReturn(stubUser);

        UserSettingGUI.LoginResponse response = gui.getSessionStatus();

        assertTrue(response.isSuccess());
        assertEquals("alice", response.getUsername());
        assertNotNull(response.getRole());
    }

    // T15 — not logged in
    @Test
    @DisplayName("T15: getSessionStatus while not logged in returns no session")
    void getSessionStatus_NotLoggedIn_ReturnsNoSession() {
        when(mockController.isLoggedIn()).thenReturn(false);

        UserSettingGUI.LoginResponse response = gui.getSessionStatus();

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("No active session"));
        assertNull(response.getUsername());
    }
}
