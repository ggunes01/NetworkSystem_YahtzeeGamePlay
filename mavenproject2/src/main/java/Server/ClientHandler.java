/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

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

                    // 2. İstemciye oluşturulan ID'yi gönder Send the ID to the Clıent
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
                        sendToClient(new NetworkMessage(MessageType.ERROR, "Oda bulunamadı veya dolu!", "SERVER"));
                    }
                    break;

                case SAVE_SCORE:
                    if (currentSession != null) {
                        // Handle the point
                        GameEngine.processScore(currentSession, (String) msg.getData(), this);
                        // Announce the current situation to all players
                        broadcastToRoom(new NetworkMessage(MessageType.GAME_UPDATE, currentSession.getGameState(), "SERVER"));
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
