package edu.utdallas.cs3354.whatt;

/**
 * UserSettingGUI — Backend service API for the login/logout subsystem.
 *
 * This class acts as the boundary between the React frontend and the
 * business logic layer. Each public method corresponds to a user-facing
 * action in the UI.
 * No I/O, console output, or UI logic lives here.
 */
public class UserSettingGUI {

    private final UserController userController;

    public UserSettingGUI(UserController mockController) {
        this.userController = new UserController();
    }

    public static class LoginResponse {
        private final boolean success;
        private final String  message;
        private final String  username;
        private final String  role;

        private LoginResponse(boolean success, String message, User user) {
            this.success  = success;
            this.message  = message;
            this.username = (user != null) ? user.getUsername() : null;
            this.role     = (user != null) ? user.getRole()     : null;
        }

        public boolean isSuccess()  { return success;  }
        public String  getMessage() { return message;  }
        public String  getUsername(){ return username; }
        public String  getRole()    { return role;     }
    }
    

    
    public LoginResponse enterCredentials(String username, String password) {
        boolean success = userController.verifyLogin(username, password);

        if (success) {
            return new LoginResponse(
                true,
                "Login successful. Welcome, " + username + "!",
                userController.getCurrentUser()
            );
        }

        return new LoginResponse(false, "Invalid username or password.", null);
    }

   
    public LoginResponse handleLogout() {
        if (!userController.isLoggedIn()) {
            return new LoginResponse(false, "No active session to log out from.", null);
        }

        boolean success = userController.logout();
        return success
            ? new LoginResponse(true,  "You have been logged out successfully.", null)
            : new LoginResponse(false, "Logout failed unexpectedly.", null);
    }

    
    public LoginResponse handleChangePassword(String oldPassword,
                                              String newPassword,
                                              String confirmPassword) {
        if (!userController.isLoggedIn()) {
            return new LoginResponse(false, "You must be logged in to change your password.", null);
        }

        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            return new LoginResponse(false, "New passwords do not match.", null);
        }

        boolean success = userController.changePassword(oldPassword, newPassword);
        return success
            ? new LoginResponse(true,  "Password changed successfully.", userController.getCurrentUser())
            : new LoginResponse(false, "Current password is incorrect.", null);
    }

    
    public LoginResponse getSessionStatus() {
        if (userController.isLoggedIn()) {
            return new LoginResponse(
                true,
                "Session active.",
                userController.getCurrentUser()
            );
        }
        return new LoginResponse(false, "No active session.", null);
    }
}
