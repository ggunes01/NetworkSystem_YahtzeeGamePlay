/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject2.Frames;

import com.mycompany.mavenproject2.Client.INetworkListener;
import com.mycompany.mavenproject2.Common.NetworkMessage;

/**
 *
 * @author ggunes
 */
public abstract class NetworkAdapter implements INetworkListener {

    @Override
    public void onChatMessageReceived(NetworkMessage msg) {
     }

    @Override
    public void onGameStatusUpdate(NetworkMessage msg) {
      }

    @Override
    public void onErrorMessageReceived(NetworkMessage msg) {
    }

    @Override
    public void onJoinMessageReceived(NetworkMessage msg) {
    }

    @Override
    public void onCreateMessageReceived(NetworkMessage msg) {
    }
    
    
}
