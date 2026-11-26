package messenger.server;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import messenger.exception.InvalidCommandException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class CommandParserTest {
    private final CommandParser parser = new CommandParser();

    @ParameterizedTest
    @MethodSource("validCommands")
    void parsesCommandNameAndArguments(String line, String name, List<String> args) throws Exception {
        Command command = parser.parse(line);

        assertEquals(name, command.getName());
        assertEquals(args, command.getArgs());
    }

    static Stream<Arguments> validCommands() {
        return Stream.of(
                arguments("REGISTER luka 1234", "REGISTER", List.of("luka", "1234")),
                arguments("LOGIN luka 1234", "LOGIN", List.of("luka", "1234")),
                arguments("login luka 1234", "LOGIN", List.of("luka", "1234")),
                arguments("MSG hello everyone", "MSG", List.of("hello everyone")),
                arguments("HISTORY", "HISTORY", List.of()),
                arguments("USERS", "USERS", List.of()),
                arguments("QUIT", "QUIT", List.of()),
                arguments("JOIN games", "JOIN", List.of("games")),
                arguments("ROOMS", "ROOMS", List.of()),
                arguments("W nino come to games!", "W", List.of("nino", "come to games!")));
    }

    @ParameterizedTest
    @MethodSource("invalidCommands")
    void reportsTheSpecifiedError(String line, String message) {
        var error = assertThrows(InvalidCommandException.class, () -> parser.parse(line));

        assertEquals(message, error.getMessage());
    }

    static Stream<Arguments> invalidCommands() {
        return Stream.of(
                arguments("", "Empty command"),
                arguments("   ", "Empty command"),
                arguments("HELLO", "Unknown command: HELLO"),
                arguments("LOGIN luka", "Usage: LOGIN <username> <password>"),
                arguments("LOGIN luka 1234 extra", "Usage: LOGIN <username> <password>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"USERS extra", "JOIN", "JOIN games extra", "MSG", "W nino"})
    void rejectsTheWrongNumberOfArguments(String line) {
        assertThrows(InvalidCommandException.class, () -> parser.parse(line));
    }
}
