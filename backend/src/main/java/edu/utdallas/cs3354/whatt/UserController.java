import java.util.UUID;

/**
 * Handles login/logout business logic.
 * Sits between the GUI layer and the database layer.
 */
public class UserController {

    private final DBMgr dbMgr;

    private User currentUser;


    private String currentSessionId;

    public UserController() {
        this.dbMgr = new DBMgr();
    }


    public boolean verifyLogin(String username, String password) {
        if (!isValidInput(username, password)) {
            System.out.println("[UserController] Login rejected: blank credentials.");
            return false;
        }

        if (dbMgr.validateCredentials(username, password)) {
            String role = dbMgr.getUserRole(username);
            currentUser      = new User(username, role);
            currentSessionId = generateSessionId();
            dbMgr.logLoginEvent(username, currentSessionId);
            System.out.println("[UserController] Login successful for: " + username);
            return true;
        }

        System.out.println("[UserController] Login failed for: " + username);
        return false;
    }


    public boolean logout() {
        if (currentUser == null) {
            System.out.println("[UserController] Logout called with no active session.");
            return false;
        }

        currentUser.setLoggedIn(false);
        dbMgr.logLogoutEvent(currentSessionId);
        System.out.println("[UserController] Logged out: " + currentUser.getUsername());

        currentUser      = null;
        currentSessionId = null;
        return true;
    }


    public boolean changePassword(String oldPassword, String newPassword) {
        if (currentUser == null) {
            System.out.println("[UserController] changePassword: no active session.");
            return false;
        }
        if (!isValidInput(currentUser.getUsername(), oldPassword)) {
            return false;
        }
        if (!dbMgr.validateCredentials(currentUser.getUsername(), oldPassword)) {
            System.out.println("[UserController] changePassword: old password incorrect.");
            return false;
        }
        return dbMgr.updatePassword(currentUser.getUsername(), newPassword);
    }

    public boolean isLoggedIn() {
        return currentUser != null && currentUser.isLoggedIn();
    }


    public User getCurrentUser() {
        return currentUser;
    }

    private boolean isValidInput(String username, String password) {
        return username != null && !username.isBlank()
            && password != null && !password.isBlank();
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}
