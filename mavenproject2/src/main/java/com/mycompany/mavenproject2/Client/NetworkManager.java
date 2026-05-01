/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject2.Client;

import com.mycompany.mavenproject2.Common.NetworkMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author ggunes
 */

// Manages the client-side network connection with the game server.
// Responsible for sending messages to the server and continuously
// listening for incoming messages on a separate thread.
// Received messages are forwarded to the GUI through the INetworkListener.

public class NetworkManager implements Runnable {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean isRunning = false;
     // Marks whether the connection was closed intentionally by the client.
    // This prevents normal exits from being treated as server failures.
    private boolean manualDisconnect = false;
    private INetworkListener listener;
    // Stores the last successful connection details so reconnect can try the same server again.
    private String lastIp;
    private int lastPort;

    // Opens a connection to the server and starts a background listener thread.
    public synchronized void connect(String ip, int port, INetworkListener listener) throws IOException {
        this.lastIp = ip;
        this.lastPort = port;
        this.listener = listener;
        this.manualDisconnect = false;
        this.socket = new Socket(ip, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.isRunning = true;

        Thread listenThread = new Thread(this);
        listenThread.start();
    }

    // Continuously listens for messages from the server until the connection is closed.
    @Override
    public void run() {
        try {
            while (isRunning) {
                NetworkMessage incoming = (NetworkMessage) in.readObject();
                handleComingMessage(incoming);

            }
        } catch (Exception e) {
            System.err.println("Connection error : " + e.getMessage());
            isRunning = false;
            closeResources();

            if (!manualDisconnect && listener != null) {
                listener.onErrorMessageReceived(new NetworkMessage(
                        NetworkMessage.MessageType.ERROR,
                        "The server connection was interrupted. The game cannot continue.",
                        "NETWORK"));
            }

        }
    }

    //Warn to GUI for type of incoming message
    public void handleComingMessage(NetworkMessage msg) {

        if (listener == null) {
            return;
        }

        switch (msg.getType()) {
            case CHAT:
                listener.onChatMessageReceived(msg);
                break;
            case GAME_UPDATE:
                listener.onGameStatusUpdate(msg);
                break;
            case ERROR:
                listener.onErrorMessageReceived(msg);
                break;
            case JOIN_GAME:
                listener.onJoinMessageReceived(msg);
                break;
            case CREATE_GAME:
                listener.onCreateMessageReceived(msg);
                break;
        }
    }

    // Sends a message to the server through the current output stream.
    public synchronized void sendMessage(NetworkMessage msg) throws IOException {
        if (out != null) {
            out.reset();
            out.writeObject(msg);
            out.flush();

        }
    }

    // Attempts to reconnect using the last known IP, port, and listener.
    public synchronized boolean reconnect() {
        closeResources();
        if (lastIp == null || lastPort == 0 || listener == null) {
            return false;
        }

        try {
            connect(lastIp, lastPort, listener);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    //Disconnection method to prevent memory leak. Also, it checks Null Excepitons
    public synchronized void disconnect() throws IOException {
        manualDisconnect = true;
        isRunning = false;
        if (in != null) in.close();
        if (out != null) out.close();
        if (socket != null) socket.close();
    }

    // Safely closes socket resources and clears references.
    private void closeResources() {
        try {
            if (in != null) in.close();
        } catch (IOException e) {
            System.err.println("Input stream could not be closed: " + e.getMessage());
        }
        try {
            if (out != null) out.close();
        } catch (IOException e) {
            System.err.println("Output stream could not be closed: " + e.getMessage());
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Socket could not be closed: " + e.getMessage());
        }
        in = null;
        out = null;
        socket = null;
    }

    public void setListener(INetworkListener listener) {
        this.listener = listener;
    }
    
    
}
