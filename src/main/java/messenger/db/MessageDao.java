package messenger.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import messenger.model.Message;

public class MessageDao {
    private final Database db;

    public MessageDao(Database db) {
        this.db = db;
    }

    public Message saveRoomMessage(int senderId, int roomId, String text) throws SQLException {
        return saveMessage(senderId, roomId, null, text);
    }

    public Message savePrivateMessage(int senderId, int receiverId, String text) throws SQLException {
        return saveMessage(senderId, null, receiverId, text);
    }

    public Message findById(int id) throws SQLException {
        try (Connection connection = db.getConnection()) {
            return findById(connection, id);
        }
    }

    public List<Message> findLastInRoom(int roomId, int limit) throws SQLException {
        String sql = """
                SELECT m.id, m.sender_id, u.username AS sender_name, m.room_id, m.receiver_id, m.text, m.sent_at
                FROM messages m JOIN users u ON u.id = m.sender_id
                WHERE m.room_id = ?
                ORDER BY m.id DESC
                LIMIT ?
                """;
        List<Message> history = new ArrayList<>();
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roomId);
            statement.setInt(2, limit);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    history.add(readMessage(rows));
                }
            }
        }
        Collections.reverse(history);
        return history;
    }

    public int count() throws SQLException {
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM messages");
             ResultSet rows = statement.executeQuery()) {
            rows.next();
            return rows.getInt(1);
        }
    }

    private Message saveMessage(int senderId, Integer roomId, Integer receiverId, String text) throws SQLException {
        String sql = "INSERT INTO messages (sender_id, room_id, receiver_id, text) VALUES (?, ?, ?, ?)";
        try (Connection connection = db.getConnection()) {
            int id;
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, senderId);
                statement.setObject(2, roomId, Types.INTEGER);
                statement.setObject(3, receiverId, Types.INTEGER);
                statement.setString(4, text);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Message insert did not return an id");
                    }
                    id = keys.getInt(1);
                }
            }
            return findById(connection, id);
        }
    }

    private Message findById(Connection connection, int id) throws SQLException {
        String sql = """
                SELECT m.id, m.sender_id, u.username AS sender_name, m.room_id, m.receiver_id, m.text, m.sent_at
                FROM messages m JOIN users u ON u.id = m.sender_id
                WHERE m.id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? readMessage(rows) : null;
            }
        }
    }

    private Message readMessage(ResultSet row) throws SQLException {
        return new Message(
                row.getInt("id"),
                row.getInt("sender_id"),
                row.getString("sender_name"),
                row.getObject("room_id", Integer.class),
                row.getObject("receiver_id", Integer.class),
                row.getString("text"),
                row.getObject("sent_at", LocalDateTime.class));
    }
}
