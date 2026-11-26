package messenger.db;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import messenger.model.Room;
import org.junit.jupiter.api.Test;

public class RoomDaoTest extends DaoTestSupport {
    private final RoomDao rooms = new RoomDao(db);

    @Test
    void generalRoomIsSeeded() throws Exception {
        var general = rooms.findByName("general");

        assertNotNull(general);
        assertEquals("general", general.getName());
    }

    @Test
    void findOrCreateReusesTheSameRoom() throws Exception {
        var first = rooms.findOrCreate("games");
        var second = rooms.findOrCreate("games");

        assertEquals(first.getId(), second.getId());
        assertEquals(1L, rooms.findAll().stream().filter(room -> room.getName().equals("games")).count());
    }

    @Test
    void roomsAreListedByName() throws Exception {
        rooms.create("zebra");
        rooms.create("arcade");

        assertEquals(List.of("arcade", "general", "zebra"), rooms.findAll().stream().map(Room::getName).toList());
    }
}
