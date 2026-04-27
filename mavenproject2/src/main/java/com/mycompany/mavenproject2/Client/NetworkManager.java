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
    private INetworkListener listener;

    public void connect(String ip, int port, INetworkListener listener) throws IOException {
        this.listener = listener;
        this.socket = new Socket(ip, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.isRunning = true;

        Thread listenThread = new Thread(this);
        listenThread.start();
    }

    //It always listens the port for a new message
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

    //Message Sender Function
    public void sendMessage(NetworkMessage msg) throws IOException {
        if (out != null) {
            out.reset();
            out.writeObject(msg);
            out.flush();

        }
    }

    //Disconnection method to prevent memory leak. Also, it checks Null Excepitons
    public void disconnect() throws IOException {
    isRunning = false;
    if (in != null) in.close();
    if (out != null) out.close();
    if (socket != null) socket.close();
}

    public void setListener(INetworkListener listener) {
        this.listener = listener;
    }
    
    
}
