package messenger.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import messenger.exception.UsernameTakenException;
import messenger.model.User;

public class UserDao {
    private final Database db;

    public UserDao(Database db) {
        this.db = db;
    }

    public User register(String username, String password) throws SQLException, UsernameTakenException {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection connection = db.getConnection()) {
            int id;
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, username);
                statement.setString(2, password);
                try {
                    statement.executeUpdate();
                } catch (SQLException e) {
                    if (e.getErrorCode() == 1062) {
                        throw new UsernameTakenException(username);
                    }
                    throw e;
                }
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("User insert did not return an id");
                    }
                    id = keys.getInt(1);
                }
            }
            return findById(connection, id);
        }
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password, created_at FROM users WHERE username = ?";
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? readUser(rows) : null;
            }
        }
    }

    public User findById(int id) throws SQLException {
        try (Connection connection = db.getConnection()) {
            return findById(connection, id);
        }
    }

    public User checkLogin(String username, String password) throws SQLException {
        User user = findByUsername(username);
        return user != null && user.getPassword().equals(password) ? user : null;
    }

    private User findById(Connection connection, int id) throws SQLException {
        String sql = "SELECT id, username, password, created_at FROM users WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? readUser(rows) : null;
            }
        }
    }

    private User readUser(ResultSet row) throws SQLException {
        return new User(
                row.getInt("id"),
                row.getString("username"),
                row.getString("password"),
                row.getObject("created_at", LocalDateTime.class));
    }
}
