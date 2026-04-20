/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject2.Common.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author ggunes
 */
public class NetworkManager {
    private Socket socket ;
    private ObjectOutputStream out ;
    private ObjectInputStream in ;
    private boolean isRunning = false ;
    
    public void connect(String ip , int port) throws IOException{
    this.socket = new Socket(ip , port);
    this.out = new ObjectOutputStream(socket.getOutputStream());
    this.in = new ObjectInputStream(socket.getInputStream());
    this.isRunning = true ;
    
    }
    
    
    
}
