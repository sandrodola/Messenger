package messenger.model;

import java.time.LocalDateTime;

public class Room {
    private final int id;
    private final String name;
    private final LocalDateTime createdAt;

    public Room(int id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
