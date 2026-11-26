package messenger.exception;

public class UsernameTakenException extends Exception {
    public UsernameTakenException(String username) {
        super("Username " + username + " is taken");
    }
}
