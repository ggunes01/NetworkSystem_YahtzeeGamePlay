/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject2.Server;

import com.mycompany.mavenproject2.Common.Dice;
import com.mycompany.mavenproject2.Common.GameState;
import com.mycompany.mavenproject2.Common.ScoringLogic;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author ggunes
 */
public class GameEngine {

    private static ConcurrentHashMap<String, GameSession> activeGames = new ConcurrentHashMap<>();

    //Function to create a new game
    public static String createNewGame(ClientHandler creator) {

        String gameId = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        GameSession session = new GameSession(gameId);
        session.setPlayer1(creator);

        activeGames.put(gameId, session);
        return gameId;
    }

    //Function to join an active game
    public static GameSession joinGame(String gameId, ClientHandler joiner)
            throws RoomNotFoundException, RoomFullException {

        GameSession session = activeGames.get(gameId);

        if (session == null) {
            throw new RoomNotFoundException("Invalid room ID: " + gameId);
        }

        if (session.getPlayer2() != null) {
            throw new RoomFullException("The room is full: " + gameId);
        }

        session.setPlayer2(joiner);
        return session;
    }

    /* THIS METHOD WILL BE CALLED WHEN A PLAYER WANTS TO SAVE THE SCORE AND THE TURN PASSES TO THE NEXT PLAYER*/
    //Method calculates the score and writes it to the table
    public static void processScore(GameSession session, String category, ClientHandler sender) {
        GameState state = session.getGameState();

        // It detects: is sender 1 or 2 ?
        int senderId = (sender == session.getPlayer1()) ? 1 : 2;

        // Turn control
        if (state.getCurrentPlayer() != senderId) {
            // Burada istersen hata mesajı gönderebilirsin: "Sıra sende değil!"
            return;
        }

        // Calculate the score
        int score = ScoringLogic.calculateScore(category, state.getCurrentDices());

        // Write the point to the player' map
        if (senderId == 1) {
            state.getPlayer1Score().put(category, score);
            state.setCurrentPlayer(2); // Turn to 2. player
        } else {
            state.getPlayer2Score().put(category, score);
            state.setCurrentPlayer(1); // Turn to 1. player
        }

        // For new your reset the dices and rights
        state.setRollsLeft(3);
        for (Dice d : state.getCurrentDices()) {
            d.setHeld(false); // Release the held dices
            d.setNumber(0);    // Make all dices 0
        }

        // Game over control
        // If both players all categories are full, game is over
        if (isScoreTableFull(state.getPlayer1Score()) && isScoreTableFull(state.getPlayer2Score())) {
            state.setGameOver(true);
        }
    }

    private static boolean isScoreTableFull(Map<String, Integer> scoreMap) {
        // If there is a -1 value in Map, the score table is still empty
        return !scoreMap.containsValue(-1);
    }

    //It provides GameSession Object for pointed ID
    public static GameSession getGameSession(String gameId) {
        return activeGames.get(gameId);
    }

    //Exception to handle error, in case of the room is not exist
    public static class RoomNotFoundException extends Exception {

        public RoomNotFoundException(String message) {
            super(message);
        }
    }
    //Exception to handle error, in case of the room is not empty
    public static class RoomFullException extends Exception {

        public RoomFullException(String message) {
            super(message);
        }
    }
}
