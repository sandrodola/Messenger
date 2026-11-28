package messenger.server;

public interface Recipient {
    String getUsername();

    String getCurrentRoom();

    void send(String line);
}
