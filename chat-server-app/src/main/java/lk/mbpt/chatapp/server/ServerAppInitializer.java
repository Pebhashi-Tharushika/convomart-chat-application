package lk.mbpt.chatapp.server;

import lk.mbpt.chatapp.server.model.User;
import lk.mbpt.chatapp.shared.Dep10Headers;
import lk.mbpt.chatapp.shared.Dep10Message;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;



public class ServerAppInitializer {

    private static volatile ArrayList<User> userList = new ArrayList<>();
    private static volatile String chatHistory = "";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5050);
        System.out.println("Server is listening to 5050");

        while (true) {
            System.out.println("Waiting for an incoming connection");
            Socket localSocket = serverSocket.accept();

            /* add the newly logged user*/
            ObjectInputStream objectInputStream = new ObjectInputStream(localSocket.getInputStream());
            String newUser = "";
            try {
                Dep10Message msg = (Dep10Message) objectInputStream.readObject();
                if (msg.getHeader() == Dep10Headers.USERNAME) {
                    newUser = msg.getBody().toString();
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            User user = new User(localSocket, objectInputStream, newUser);
            userList.add(user);
            System.out.println("New connection: " + user.getUsername());

            new Thread(() -> {
                try {
                    /* send the chat history and logged users list first */
                    sendChatHistory(user);
                    broadcastLoggedUsers();
                    ObjectInputStream ois = user.getObjectInputStream();

                    /* When a new chat message arrives, send it all the users
                    * And when a user has left, remove that user from logged users list */
                    while (true) {
                        Dep10Message msg = (Dep10Message) ois.readObject();
                        if (msg.getHeader() == Dep10Headers.MSG) {
                            chatHistory += String.format("%s: %s \n", user.getUsername(), msg.getBody());
                            broadcastChatHistory();
                        } else if (msg.getHeader() == Dep10Headers.EXIT) {
                            removeUser(user);
                            return;
                        }
                    }
                } catch (Exception e) {
                    removeUser(user);
                    /* handle the sudden disappearing of a client */
                    if (e instanceof  EOFException) return;
                    e.printStackTrace();
                }
            }).start();
        }
    }



    private static void removeUser(User user){

        if (userList.contains(user)){
            userList.remove(user);
            broadcastLoggedUsers();

            if (!user.getLocalSocket().isClosed()) {
                try {
                    /* When a user has left, close its connection */
                    user.getLocalSocket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void broadcastChatHistory() {
        for (User user : userList) {

            /* send a new chat message to all the users */
            new Thread(() -> {
                try {
                    ObjectOutputStream oos = user.getObjectOutputStream();
                    oos.writeObject(new Dep10Message(Dep10Headers.MSG, chatHistory));
                    oos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private static void sendChatHistory(User user) throws IOException {
        ObjectOutputStream oos = user.getObjectOutputStream();
        Dep10Message msg = new Dep10Message(Dep10Headers.MSG, chatHistory);
        oos.writeObject(msg);
        oos.flush();
    }

    private static void broadcastLoggedUsers() {

        /* collect all the names of logged users */
        ArrayList<String> loggedUserList = new ArrayList<>();
        for (User user : userList) {
            loggedUserList.add(user.getUsername());
        }

        /* send logged users list to each user */
        for (User user : userList) {
            new Thread(() -> {
                try {
                    ObjectOutputStream oos = user.getObjectOutputStream();
                    Dep10Message msg = new Dep10Message(Dep10Headers.USERS, loggedUserList);
                    oos.writeObject(msg);
                    oos.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }
}
