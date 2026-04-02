/**
 * Represents an authenticated user session.
 */
public class User {
    private String username;
    private String role;
    private boolean isLoggedIn;

    public User(String username, String role) {
        this.username = username;
        this.role = role;
        this.isLoggedIn = true;
    }

    public String getUsername() { return username; }
    public String getRole()     { return role; }
    public boolean isLoggedIn() { return isLoggedIn; }
    public void setLoggedIn(boolean loggedIn) { this.isLoggedIn = loggedIn; }

    @Override
    public String toString() {
        return "User{username='" + username + "', role='" + role + "', loggedIn=" + isLoggedIn + "}";
    }
}
