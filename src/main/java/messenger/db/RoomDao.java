package messenger.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import messenger.model.Room;

public class RoomDao {
    private final Database db;

    public RoomDao(Database db) {
        this.db = db;
    }

    public Room findByName(String name) throws SQLException {
        try (Connection connection = db.getConnection()) {
            return findByName(connection, name);
        }
    }

    public Room create(String name) throws SQLException {
        String sql = "INSERT INTO rooms (name) VALUES (?)";
        try (Connection connection = db.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                statement.executeUpdate();
            }
            return findByName(connection, name);
        }
    }

    public Room findOrCreate(String name) throws SQLException {
        Room existing = findByName(name);
        if (existing != null) {
            return existing;
        }

        try {
            return create(name);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                Room created = findByName(name);
                if (created != null) {
                    return created;
                }
            }
            throw e;
        }
    }

    public List<Room> findAll() throws SQLException {
        String sql = "SELECT id, name, created_at FROM rooms ORDER BY name";
        List<Room> rooms = new ArrayList<>();
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                rooms.add(readRoom(rows));
            }
        }
        return rooms;
    }

    private Room findByName(Connection connection, String name) throws SQLException {
        String sql = "SELECT id, name, created_at FROM rooms WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? readRoom(rows) : null;
            }
        }
    }

    private Room readRoom(ResultSet row) throws SQLException {
        return new Room(row.getInt("id"), row.getString("name"), row.getObject("created_at", LocalDateTime.class));
    }
}
