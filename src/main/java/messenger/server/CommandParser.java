package messenger.server;

import java.util.List;
import java.util.Locale;

import messenger.exception.InvalidCommandException;

public class CommandParser {
    public Command parse(String line) throws InvalidCommandException {
        if (line == null || line.isBlank()) {
            throw new InvalidCommandException("Empty command");
        }

        String input = line.strip();
        String[] commandAndArgs = input.split("\\s+", 2);
        String name = commandAndArgs[0].toUpperCase(Locale.ROOT);

        return switch (name) {
            case "REGISTER", "LOGIN" -> {
                String[] parts = input.split("\\s+");
                if (parts.length != 3) {
                    throw new InvalidCommandException("Usage: " + name + " <username> <password>");
                }
                yield new Command(name, List.of(parts[1], parts[2]));
            }
            case "JOIN" -> {
                String[] parts = input.split("\\s+");
                if (parts.length != 2) {
                    throw new InvalidCommandException("Usage: JOIN <room>");
                }
                yield new Command(name, List.of(parts[1]));
            }
            case "MSG" -> {
                if (commandAndArgs.length != 2) {
                    throw new InvalidCommandException("Usage: MSG <message>");
                }
                yield new Command(name, List.of(commandAndArgs[1]));
            }
            case "W" -> {
                String[] parts = input.split("\\s+", 3);
                if (parts.length != 3) {
                    throw new InvalidCommandException("Usage: W <username> <message>");
                }
                yield new Command(name, List.of(parts[1], parts[2]));
            }
            case "HISTORY", "USERS", "ROOMS", "QUIT" -> {
                if (commandAndArgs.length != 1) {
                    throw new InvalidCommandException("Usage: " + name);
                }
                yield new Command(name, List.of());
            }
            default -> throw new InvalidCommandException("Unknown command: " + name);
        };
    }
}
