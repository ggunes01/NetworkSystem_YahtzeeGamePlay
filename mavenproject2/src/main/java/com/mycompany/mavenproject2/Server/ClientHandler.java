/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject2.Server;

import com.mycompany.mavenproject2.Common.NetworkMessage;
import com.mycompany.mavenproject2.Common.NetworkMessage.MessageType;
import java.io.*;
import java.net.Socket;

/**
 *
 * @author ggunes
 */
public class ClientHandler implements Runnable {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private GameSession currentSession;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    //It always listens the message from Client(Join,Create)
    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            while (true) {
                NetworkMessage msg = (NetworkMessage) in.readObject();
                handleMessage(msg);
            }
        } catch (Exception e) {
            System.out.println("Client Connection Error: " + e.getMessage());
        }
    }

    public void handleMessage(NetworkMessage msg) throws GameEngine.RoomNotFoundException, GameEngine.RoomFullException {
        try {
            switch (msg.getType()) {
                case CREATE_GAME:
                    // Create a new room and assign this handler to the room
                    String newId = GameEngine.createNewGame(this);
                    this.currentSession = GameEngine.getGameSession(newId);

                    // Send the generated ID to the client.
                    sendToClient(new NetworkMessage(MessageType.CREATE_GAME, newId, "SERVER"));
                    break;

                case JOIN_GAME:
                    String joinId = (String) msg.getData();
                    // Try to join a curent room
                    GameSession session = GameEngine.joinGame(joinId, this);

                    if (session != null) {
                        this.currentSession = session;
                        // Announce to both player that the game is started (Broadcast)
                        broadcastToRoom(new NetworkMessage(MessageType.GAME_UPDATE, session.getGameState(), "SERVER"));
                    } else {
                        sendToClient(new NetworkMessage(MessageType.ERROR, "Room couln't find or Full!", "SERVER"));
                    }
                    break;

                case SAVE_SCORE:
                    if (currentSession != null) {
                        // Get category information from the incoming message.(Ones, Full House vb.)
                        String category = (String) msg.getData();

                        //  Enter this score into GameEngine (Calculation is done here)
                        // We send 'this' because the handler understands which player (P1 or P2) the point should be awarded to.
                        GameEngine.processScore(currentSession, category, this);

                        // PUBLISH THE UPDATE
                        // We send the new GameState, which is generated after the score is entered, to everyone.
                        NetworkMessage updateMsg = new NetworkMessage(
                                MessageType.GAME_UPDATE,
                                currentSession.getGameState(),
                                "SERVER"
                        );
                        broadcastToRoom(updateMsg);

                        System.out.println("Point saved and shared: " + category);
                    }
                    break;

                case CHAT:
                    // Chat Message
                    broadcastToRoom(msg);
                    break;
            }
        } catch (IOException e) {
            System.err.println("Message process error: " + e.getMessage());
        }

    }

    // Assistant method for sending message too all players
    private void broadcastToRoom(NetworkMessage msg) throws IOException {
        if (currentSession != null) {
            currentSession.getPlayer1().sendToClient(msg);
            if (currentSession.getPlayer2() != null) {
                currentSession.getPlayer2().sendToClient(msg);
            }
        }
    }

    public void sendToClient(NetworkMessage msg) throws IOException {
        out.writeObject(msg);
        out.flush();
    }

}
