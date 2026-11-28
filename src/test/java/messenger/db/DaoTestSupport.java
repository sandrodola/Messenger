package messenger.db;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.ResourceLock;

@ResourceLock("messenger_test")
abstract class DaoTestSupport {
    private static final int PORT = Integer.parseInt(setting("MESSENGER_TEST_DB_PORT", "3306"));

    final Database db = new Database(
            "jdbc:mysql://127.0.0.1:" + PORT + "/messenger_test",
            setting("MESSENGER_TEST_DB_USER", "root"),
            setting("MESSENGER_TEST_DB_PASSWORD", ""));

    @BeforeEach
    void resetDatabase() throws SQLException {
        try (var connection = db.getConnection();
             var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM messages");
            statement.executeUpdate("DELETE FROM users");
            statement.executeUpdate("DELETE FROM rooms WHERE name <> 'general'");
        }
    }

    private static String setting(String name, String fallback) {
        return System.getenv().getOrDefault(name, fallback);
    }
}
