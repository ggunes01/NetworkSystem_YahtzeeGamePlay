/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.mavenproject2.Client;

import com.mycompany.mavenproject2.Common.NetworkMessage;

/**
 *
 * @author ggunes
 */
//This interface provides a easy way between GUI and Server
public interface INetworkListener {
    
    // It trigers when chat message is sent
    void onChatMessageReceived(NetworkMessage msg);
    
    // It trigers when a game situation is updated
    void onGameStatusUpdate(NetworkMessage msg);
    
    //It trigers when a connection error is occured
    void onErrorMessageReceived( NetworkMessage msg);
    
    //It trigers when a client wants to join a current game
    void onJoinMessageReceived(NetworkMessage msg);
    
    //It trigers when a client wants to create a new game
    void onCreateMessageReceived(NetworkMessage msg);
   
}
