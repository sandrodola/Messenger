package messenger.server;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import messenger.db.MessageDao;
import messenger.db.RoomDao;
import messenger.db.UserDao;
import messenger.exception.InvalidCommandException;
import messenger.exception.UsernameTakenException;
import messenger.model.Message;
import messenger.model.Room;
import messenger.model.User;

public class ClientHandler implements Runnable, Recipient {
    private static final int HISTORY_LIMIT = 20;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ChatService chat;
    private UserDao users;
    private RoomDao rooms;
    private MessageDao messages;
    private User user;
    private volatile Room currentRoom;

    public ClientHandler(Socket socket, ChatService chat, UserDao users, RoomDao rooms, MessageDao messages) throws IOException {
        this.socket = socket;
        this.chat = chat;
        this.users = users;
        this.rooms = rooms;
        this.messages = messages;

        this.in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
    }

    @Override
    public void run() {
        CommandParser parser = new CommandParser();
        try (Socket connection = socket) {
            String line;
            while ((line = in.readLine()) != null) {
                Command command;
                try {
                    command = parser.parse(line);
                } catch (InvalidCommandException e) {
                    send("ERROR " + e.getMessage());
                    continue;
                }

                String name = command.getName();
                if (user == null
                        && !name.equals("REGISTER")
                        && !name.equals("LOGIN")
                        && !name.equals("QUIT")) {
                    send("ERROR Please log in first");
                    continue;
                }

                switch (name) {
                    case "REGISTER" -> handleRegister(command);
                    case "LOGIN" -> handleLogin(command);
                    case "MSG" -> handleMsg(command);
                    case "HISTORY" -> handleHistory(command);
                    case "USERS" -> handleUsers(command);
                    case "JOIN" -> handleJoin(command);
                    case "ROOMS" -> handleRooms(command);
                    case "W" -> handleWhisper(command);
                    case "QUIT" -> {
                        handleQuit(command);
                        return;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Client connection error: " + e.getMessage());
        } finally {
            logout();
        }
    }

    @Override
    public void send(String line) {
        out.println(line);
    }

    @Override
    public String getUsername() {
        return user == null ? null : user.getUsername();
    }


    @Override
    public String getCurrentRoom() {
        Room room = currentRoom;
        return room == null ? null : room.getName();
    }

    private void handleRegister(Command c) {
        String username = c.getArg(0);
        String password = c.getArg(1);
        try {
            users.register(username, password);
            send("OK Registered");
        } catch (UsernameTakenException e) {
            send("ERROR " + e.getMessage());
        } catch (SQLException e) {
            send("ERROR Registration failed");
        }
    }

    private void handleLogin(Command c) {
        if (user != null) {
            send("ERROR Already logged in");
            return;
        }

        String username = c.getArg(0);
        String password = c.getArg(1);
        try {
            User authenticatedUser = users.checkLogin(username, password);
            if (authenticatedUser == null) {
                send("ERROR Invalid username or password");
                return;
            }

            Room general = rooms.findByName("general");
            if (general == null) {
                send("ERROR General room is unavailable");
                return;
            }

            user = authenticatedUser;
            currentRoom = general;
            if (!chat.addOnline(this)) {
                user = null;
                currentRoom = null;
                send("ERROR User is already online");
                return;
            }

            send("OK Logged in");
        } catch (SQLException e) {
            send("ERROR Login failed");
        }
    }

    private void handleMsg(Command c) {
        Room room = currentRoom;
        try {
            Message message = messages.saveRoomMessage(user.getId(), room.getId(), c.getArg(0));
            chat.broadcastToRoom(room.getName(), message.format(), null);
        } catch (SQLException e) {
            send("ERROR Could not save room message");
        }
    }

    private void handleHistory(Command c) {
        try {
            List<Message> history = messages.findLastInRoom(currentRoom.getId(), HISTORY_LIMIT);
            StringBuilder reply = new StringBuilder("HISTORY ").append(history.size());
            for (Message message : history) {
                reply.append('\n').append(message.format());
            }
            send(reply.toString());
        } catch (SQLException e) {
            send("ERROR Could not load room history");
        }
    }

    private void handleUsers(Command c) {
        send("USERS " + String.join(", ", chat.onlineUsers()));
    }

    private void handleJoin(Command c) {
        try {
            Room room = rooms.findOrCreate(c.getArg(0));
            currentRoom = room;
            send("OK Joined " + room.getName());
        } catch (SQLException e) {
            send("ERROR Could not join room");
        }
    }

    private void handleRooms(Command c) {
        try {
            List<String> entries = new ArrayList<>();
            for (Room room : rooms.findAll()) {
                String name = room.getName();
                entries.add(name + " (" + chat.countInRoom(name) + ")");
            }
            send("ROOMS " + String.join(", ", entries));
        } catch (SQLException e) {
            send("ERROR Could not load rooms");
        }
    }

    private void handleWhisper(Command c) {
        try {
            User recipient = users.findByUsername(c.getArg(0));
            if (recipient == null || !chat.isOnline(recipient.getUsername())) {
                send("ERROR User is offline");
                return;
            }

            Message message = messages.savePrivateMessage(user.getId(), recipient.getId(), c.getArg(1));
            if (chat.sendPrivate(recipient.getUsername(), "PRIVATE " + message.format())) {
                send("OK Private message sent");
            } else {
                send("ERROR Private message saved but user went offline");
            }
        } catch (SQLException e) {
            send("ERROR Could not save private message");
        }
    }

    private void handleQuit(Command c) {
        send("OK Goodbye");
    }

    private void logout() {
        if (user != null) {
            chat.removeOnline(user.getUsername());
            user = null;
            currentRoom = null;
        }
    }
}
