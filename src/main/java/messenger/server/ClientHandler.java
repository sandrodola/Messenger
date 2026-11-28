package messenger.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import messenger.db.MessageDao;
import messenger.db.RoomDao;
import messenger.db.UserDao;
import messenger.model.Room;
import messenger.model.User;

public class ClientHandler implements Runnable, Recipient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ChatService chat;
    private UserDao users;
    private RoomDao rooms;
    private MessageDao messages;
    private User user;
    private Room currentRoom;

    public ClientHandler(Socket socket, ChatService chat, UserDao users, RoomDao rooms, MessageDao messages)
            throws IOException {
    }

    @Override
    public void run() {
    }

    @Override
    public void send(String line) {
    }

    @Override
    public String getUsername() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCurrentRoom() {
        throw new UnsupportedOperationException();
    }

    private void handleRegister(Command c) {
    }

    private void handleLogin(Command c) {
    }

    private void handleMsg(Command c) {
    }

    private void handleHistory(Command c) {
    }

    private void handleUsers(Command c) {
    }

    private void handleJoin(Command c) {
    }

    private void handleRooms(Command c) {
    }

    private void handleWhisper(Command c) {
    }

    private void handleQuit(Command c) {
    }

    private void logout() {
    }
}
