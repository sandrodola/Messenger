package messenger.server;

import java.util.List;
import java.util.Map;

public class ChatService {
    private Map<String, Recipient> online;

    public synchronized boolean addOnline(Recipient r) {
        throw new UnsupportedOperationException();
    }

    public synchronized void removeOnline(String username) {
    }

    public synchronized boolean isOnline(String username) {
        throw new UnsupportedOperationException();
    }

    public synchronized void broadcastToRoom(String room, String line, String exceptUsername) {
    }

    public synchronized boolean sendPrivate(String toUsername, String line) {
        throw new UnsupportedOperationException();
    }

    public synchronized int countInRoom(String room) {
        throw new UnsupportedOperationException();
    }

    public synchronized List<String> onlineUsers() {
        throw new UnsupportedOperationException();
    }
}
