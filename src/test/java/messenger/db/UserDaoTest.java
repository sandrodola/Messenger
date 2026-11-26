package messenger.db;

import static org.junit.jupiter.api.Assertions.*;

import messenger.exception.UsernameTakenException;
import org.junit.jupiter.api.Test;

public class UserDaoTest extends DaoTestSupport {
    private final UserDao users = new UserDao(db);

    @Test
    void registerReturnsStoredUser() throws Exception {
        var user = users.register("luka", "pass1234");

        assertTrue(user.getId() > 0);
        assertNotNull(user.getCreatedAt());
        assertEquals("luka", users.findById(user.getId()).getUsername());
    }

    @Test
    void rejectsDuplicateUsername() throws Exception {
        users.register("luka", "pass1234");

        assertThrows(UsernameTakenException.class, () -> users.register("luka", "different"));
    }

    @Test
    void returnsNullForUnknownUsername() throws Exception {
        assertNull(users.findByUsername("missing"));
    }

    @Test
    void loginRequiresTheCorrectPassword() throws Exception {
        var registered = users.register("luka", "pass1234");

        var loggedIn = users.checkLogin("luka", "pass1234");
        assertNotNull(loggedIn);
        assertEquals(registered.getId(), loggedIn.getId());
        assertNull(users.checkLogin("luka", "wrong"));
    }
}
