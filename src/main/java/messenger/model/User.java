package messenger.model;

import java.time.LocalDateTime;

public class User {
    private final int id;
    private final String username;
    private final String password;
    private final LocalDateTime createdAt;

    public User(int id, String username, String password, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
