package messenger.db;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.IntStream;

import messenger.model.Message;
import org.junit.jupiter.api.Test;

public class MessageDaoTest extends DaoTestSupport {
    private final UserDao users = new UserDao(db);
    private final RoomDao rooms = new RoomDao(db);
    private final MessageDao messages = new MessageDao(db);

    @Test
    void roomMessageReturnsStoredFields() throws Exception {
        var sender = users.register("luka", "pass1234");
        var general = rooms.findByName("general");

        var saved = messages.saveRoomMessage(sender.getId(), general.getId(), "hello room");

        assertStoredMessage(saved, sender.getId(), "hello room");
        assertEquals(Integer.valueOf(general.getId()), saved.getRoomId());
        assertNull(saved.getReceiverId());
        assertFalse(saved.isPrivate());
    }

    @Test
    void privateMessageReturnsStoredFields() throws Exception {
        var sender = users.register("luka", "pass1234");
        var receiver = users.register("nino", "pass5678");

        var saved = messages.savePrivateMessage(sender.getId(), receiver.getId(), "private hello");

        assertStoredMessage(saved, sender.getId(), "private hello");
        assertNull(saved.getRoomId());
        assertEquals(Integer.valueOf(receiver.getId()), saved.getReceiverId());
        assertTrue(saved.isPrivate());
    }

    @Test
    void historyReturnsNewestTwentyInOrderAndOnlyFromThatRoom() throws Exception {
        var sender = users.register("luka", "pass1234");
        var general = rooms.findByName("general");
        var games = rooms.create("games");

        for (int number = 1; number <= 25; number++) {
            messages.saveRoomMessage(sender.getId(), general.getId(), "message " + number);
            if (number == 24) {
                messages.saveRoomMessage(sender.getId(), games.getId(), "other room");
            }
        }

        var expected = IntStream.rangeClosed(6, 25).mapToObj(number -> "message " + number).toList();
        var history = messages.findLastInRoom(general.getId(), 20);

        assertEquals(expected, history.stream().map(Message::getText).toList());
        assertEquals(26, messages.count());
    }

    private void assertStoredMessage(Message saved, int senderId, String text) throws Exception {
        assertTrue(saved.getId() > 0);
        assertEquals(senderId, saved.getSenderId());
        assertEquals("luka", saved.getSenderName());
        assertNotNull(saved.getSentAt());
        assertEquals(text, saved.getText());

        var stored = messages.findById(saved.getId());
        assertNotNull(stored);
        assertEquals(saved.getSenderName(), stored.getSenderName());
        assertEquals(saved.getSentAt(), stored.getSentAt());
        assertEquals(saved.getRoomId(), stored.getRoomId());
        assertEquals(saved.getReceiverId(), stored.getReceiverId());
        assertEquals(text, stored.getText());
    }
}
