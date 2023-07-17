package lk.mbpt.chatapp.server.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class User {
    private Socket localSocket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private String username;

    public User(Socket localSocket, ObjectInputStream objectInputStream, String username) throws IOException {
        this.localSocket = localSocket;
        this.objectInputStream = objectInputStream;
        this.username = username;
        /* Before starting to read from the ObjectInputStream from the client side, setup the ObjectOutputStream first from the server side */
        objectOutputStream = new ObjectOutputStream(localSocket.getOutputStream());
        objectOutputStream.flush();
    }

    public Socket getLocalSocket() {
        return localSocket;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    public ObjectInputStream getObjectInputStream() throws IOException {
        /* return a single instance of objectInputStream every time */
        return objectInputStream != null ? objectInputStream : (objectInputStream = new ObjectInputStream(localSocket.getInputStream()));
    }

    public String getRemoteIpAddress(){
        return ((InetSocketAddress)(localSocket.getRemoteSocketAddress())).getHostString();
    }

    public String getUsername() {
        return username;
    }
}
