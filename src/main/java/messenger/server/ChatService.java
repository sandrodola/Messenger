package messenger.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatService {
    private Map<String, Recipient> online = new HashMap<>();;

    public synchronized boolean addOnline(Recipient r) {
        return online.putIfAbsent(r.getUsername(), r) == null;
    }

    public synchronized void removeOnline(String username) {
        online.remove(username);
    }

    public synchronized boolean isOnline(String username) {
        return online.containsKey(username);
    }

    public synchronized void broadcastToRoom(String room, String line, String exceptUsername) {
        for (Recipient person : online.values()) {
            if (Objects.equals(person.getCurrentRoom(), room)
                    && (exceptUsername == null || !exceptUsername.equals(person.getUsername()))) {
                person.send(line);
            }
        }
    }

    public synchronized boolean sendPrivate(String toUsername, String line) {
        if(!online.containsKey(toUsername)){
            return false;
        }
        online.get(toUsername).send(line);
        return true;
    }

    public synchronized int countInRoom(String room) {
        int count = 0;
        for (Recipient person : online.values()) {
            if (Objects.equals(person.getCurrentRoom(), room)) {
                count++;
            }
        }
        return count;
    }

    public synchronized List<String> onlineUsers() {
        return online.values().stream()
                .sorted(java.util.Comparator.comparing(Recipient::getUsername))
                .map(person -> person.getUsername() + " (" + person.getCurrentRoom() + ")")
                .toList();
    }

}
