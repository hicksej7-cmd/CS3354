import java.sql.*;

/**
 * Database Manager responsible for all session operations.
 * Interfaces directly with the database.
 */
public class DBMgr {

    //Connection config
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/your_db";
    private static final String DB_USER = "your_db_user";
    private static final String DB_PASS = "your_db_password";

    /**
     * Opens and returns a new database connection.
     *
     * @return a live {@link Connection}
     * @throws SQLException if the connection cannot be established
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public boolean validateCredentials(String username, String password) {
        String sql = "SELECT password_hash, is_active FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    boolean isActive  = rs.getBoolean("is_active");
                    return isActive && hashPassword(password).equals(storedHash);
                }
            }

        } catch (SQLException e) {
            System.err.println("[DBMgr] validateCredentials error: " + e.getMessage());
        }
        return false;
    }


    public String getUserRole(String username) {
        String sql = "SELECT role FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }

        } catch (SQLException e) {
            System.err.println("[DBMgr] getUserRole error: " + e.getMessage());
        }
        return null;
    }


    public void logLoginEvent(String username, String sessionId) {
        String sql = "INSERT INTO login_sessions (username, session_id, login_time) "
                   + "VALUES (?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, sessionId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[DBMgr] logLoginEvent error: " + e.getMessage());
        }
    }


    public void logLogoutEvent(String sessionId) {
        String sql = "UPDATE login_sessions SET logout_time = NOW() "
                   + "WHERE session_id = ? AND logout_time IS NULL";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sessionId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[DBMgr] logLogoutEvent error: " + e.getMessage());
        }
    }


    public boolean updatePassword(String username, String newPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hashPassword(newPassword));
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[DBMgr] updatePassword error: " + e.getMessage());
        }
        return false;
    }

    private String hashPassword(String password) {
        // TODO: replace with BCrypt.hashpw(password, BCrypt.gensalt()) or equivalent
        return Integer.toHexString(password.hashCode());
    }
}
