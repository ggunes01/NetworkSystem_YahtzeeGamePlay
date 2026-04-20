/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject2.Common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ggunes
 */
//This class is to manage game
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L; //Version Checking

    private Dice[] currentDices; //Array to Save Dices' status
    private int rollsLeft; // Remainder Dice Rolling
    private int currentPlayer; //Player 
    private boolean gameOver;//Game Statu

    //Hash Tables to save point for each categories
    private Map<String, Integer> player1Score;
    private Map<String, Integer> player2Score;

    public GameState() {
        this.currentDices = new Dice[5];
        for (int i = 0; i < 5; i++) {
            currentDices[i] = new Dice();
        }
        this.rollsLeft = 3;
        this.currentPlayer = 1;
        this.gameOver = false;
        this.player1Score = new HashMap<>();
        this.player2Score = new HashMap<>();

        initializeScorecards();
    }

    private void initializeScorecards() {
        // Yahtzee kategorileri
        String[] categories = {
            "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes",
            "Three of a Kind", "Four of a Kind", "Full House",
            "Small Straight", "Large Straight", "Yahtzee", "Chance"
        };

        for (String category : categories) {
            player1Score.put(category, -1); // -1 means Didn't used
            player2Score.put(category, -1);
        }
    }

    /* GETTER and SETTER METHODS */
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Dice[] getCurrentDices() {
        return currentDices;
    }

    public int getRollsLeft() {
        return rollsLeft;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Map<String, Integer> getPlayer1Score() {
        return player1Score;
    }

    public Map<String, Integer> getPlayer2Score() {
        return player2Score;
    }

    public void setCurrentDices(Dice[] currentDices) {
        this.currentDices = currentDices;
    }

    public void setRollsLeft(int rollsLeft) {
        this.rollsLeft = rollsLeft;
    }

    public void setCurrentPlayer(int currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public void setPlayer1Score(Map<String, Integer> player1Score) {
        this.player1Score = player1Score;
    }

    public void setPlayer2Score(Map<String, Integer> player2Score) {
        this.player2Score = player2Score;
    }

}
