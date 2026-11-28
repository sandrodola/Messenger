package messenger.server;

import java.io.IOException;
import java.net.ServerSocket;

import messenger.db.Database;
import messenger.db.MessageDao;
import messenger.db.RoomDao;
import messenger.db.UserDao;

public class ChatServer {
    private final int port;
    private final Database db;

    public ChatServer(Database db, int port) {
        this.db = db;
        this.port = port;
    }

    public void start() throws IOException {
        var chat = new ChatService();
        var users = new UserDao(db);
        var rooms = new RoomDao(db);
        var messages = new MessageDao(db);
        try (var server = new ServerSocket(port)) {
            while (true) {
                var socket = server.accept();
                try {
                    var handler = new ClientHandler(socket, chat, users, rooms, messages);
                    new Thread(handler).start();
                } catch (IOException e) {
                    System.err.println("Could not set up client: " + e.getMessage());
                    try {
                        socket.close();
                    } catch (IOException closeError) {
                        System.err.println("Could not close client socket: " + closeError.getMessage());
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        var db = new Database(
                "jdbc:mysql://127.0.0.1:3306/messenger",
                "root",
                System.getenv("MESSENGER_DB_PASSWORD"));
        try {
            new ChatServer(db, 5000).start();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}
