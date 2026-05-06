/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject2.Server;

import com.mycompany.mavenproject2.Common.Dice;
import com.mycompany.mavenproject2.Common.GameState;
import com.mycompany.mavenproject2.Common.NetworkMessage;
import com.mycompany.mavenproject2.Common.NetworkMessage.MessageType;
import java.io.*;
import java.net.Socket;

/**
 *
 * @author ggunes
 */
// Handles communication between the server and a single connected client.
// Listens for incoming messages (create game, join game, chat, game updates)
// and processes them accordingly. Also responsible for sending messages
// back to the client and broadcasting updates to players in the same game session.
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

            System.out.println("New client connected. Waiting for action...");

            while (true) {
                NetworkMessage msg = (NetworkMessage) in.readObject();
                handleMessage(msg);
            }
        } catch (Exception e) {
            System.out.println("Client Connection Lost: " + e.getMessage());

            handleClientDisconnected();
            ServerMain.allClients.remove(this);
        }
    }

    public void handleMessage(NetworkMessage msg) throws GameEngine.RoomNotFoundException, GameEngine.RoomFullException {
        try {
            switch (msg.getType()) {
                case CREATE_GAME:
                    // Create a new room and assign this handler to the room
                    String newRoomId = GameEngine.createNewGame(this);
                    this.currentSession = GameEngine.getGameSession(newRoomId);

                    // Send the generated ID to the client.
                    sendToClient(new NetworkMessage(MessageType.CREATE_GAME, newRoomId, "SERVER"));
                    break;

                case JOIN_GAME:
                    String joinId = (String) msg.getData();
                    // Try to join a curent room
                    GameSession session = GameEngine.joinGame(joinId, this);

                    if (session != null) {
                        this.currentSession = session;

                        broadcastToRoom(new NetworkMessage(MessageType.JOIN_GAME, joinId, "SERVER"));
                        // Announce to both player that the game is started (Broadcast)
                        broadcastToRoom(new NetworkMessage(MessageType.GAME_UPDATE, session.getGameState(), "SERVER"));
                    } else {
                        sendToClient(new NetworkMessage(MessageType.ERROR, "Room couln't find or Full!", "SERVER"));
                    }
                    break;

                case SAVE_SCORE:
                    if (currentSession != null) {
                        Object[] receivedData = (Object[]) msg.getData();
                        String cat = (String) receivedData[0];

                        // Apply the score (The order changes here)
                        GameEngine.processScore(currentSession, cat, this);

                        // Send updated status to everyone
                        NetworkMessage updateMsg = new NetworkMessage(
                                MessageType.GAME_UPDATE,
                                currentSession.getGameState(),
                                "SERVER"
                        );
                        broadcastToRoom(updateMsg);
                    }
                    break;

                case CHAT:
                    // Chat Message
                    broadcastToRoom(msg);
                    break;
                case GAME_UPDATE:
                    if (currentSession != null) {
                        GameState incomingState = (GameState) msg.getData();
                        // Fully update the main status on the server.
                        currentSession.setGameState(incomingState);

                        // Spread the word to all players.
                        broadcastToRoom(new NetworkMessage(MessageType.GAME_UPDATE, incomingState, "SERVER"));
                    }
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
        out.reset();
        out.writeObject(msg);
        out.flush();
    }

    private void handleClientDisconnected() {
        if (currentSession == null) {
            return;
        }

        GameSession disconnectedSession = currentSession;
        ClientHandler otherPlayer = null;
        String message = null;

        if (disconnectedSession.getPlayer1() == this) {
            otherPlayer = disconnectedSession.getPlayer2();
            message = "Player 1 left.";
        } else if (disconnectedSession.getPlayer2() == this) {
            otherPlayer = disconnectedSession.getPlayer1();
            message = "Player 2 left.";
        }

        GameEngine.closeGame(disconnectedSession);
        currentSession = null;

        if (otherPlayer != null) {
            otherPlayer.currentSession = null;
            try {
                otherPlayer.sendToClient(new NetworkMessage(MessageType.ERROR, message, "PLAYER_LEFT"));
            } catch (IOException e) {
                System.err.println("Player leave notification could not be sent: " + e.getMessage());
            }
        }
    }

}
