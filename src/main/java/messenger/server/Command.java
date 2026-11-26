package messenger.server;

import java.util.List;

public class Command {
    private String name;
    private List<String> args;

    public Command(String name, List<String> args) {
    }

    public String getName() {
        throw new UnsupportedOperationException();
    }

    public List<String> getArgs() {
        throw new UnsupportedOperationException();
    }

    public String getArg(int index) {
        throw new UnsupportedOperationException();
    }
}
