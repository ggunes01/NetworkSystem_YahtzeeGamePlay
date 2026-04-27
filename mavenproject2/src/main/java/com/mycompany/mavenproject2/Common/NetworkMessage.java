/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject2.Common;

import java.io.Serializable;

/**
 *
 * @author ggunes
 */
//This class is created to determine Message Format
public class NetworkMessage implements Serializable {
    public enum MessageType {
    ROLL_DICE,      // Dice Roll Request
    SAVE_SCORE,     // Point Saving
    CHAT,           // Chat Message
    GAME_UPDATE,    // Current Game
    ERROR,           // Error Messages
    CREATE_GAME,    //Request to create a new game
    JOIN_GAME, //Request to join an active game
}
    private MessageType type; // type of message
    private Object data ; // Sending data (Object to make flexible)
    private String sender ; // ID of sender

    public NetworkMessage(MessageType type, Object data, String sender) {
        this.type = type;
        this.data = data;
        this.sender = sender;
    }

    public MessageType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public String getSender() {
        return sender;
    }
    
    
    
}
