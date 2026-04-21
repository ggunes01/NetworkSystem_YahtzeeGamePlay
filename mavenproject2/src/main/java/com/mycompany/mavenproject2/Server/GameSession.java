/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject2.Server;

import com.mycompany.mavenproject2.Common.GameState;

/**
 *
 * @author ggunes
 */
public class GameSession {
    private String gameId ;
    private ClientHandler player1;
    private ClientHandler player2 ;
    private GameState gameState;

    public GameSession(String gameId) {
        this.gameId = gameId;
        this.gameState = gameState;
    }

    public String getGameId() {
        return gameId;
    }

    public ClientHandler getPlayer1() {
        return player1;
    }

    public ClientHandler getPlayer2() {
        return player2;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setPlayer1(ClientHandler player1) {
        this.player1 = player1;
    }

    public void setPlayer2(ClientHandler player2) {
        this.player2 = player2;
    }

}
