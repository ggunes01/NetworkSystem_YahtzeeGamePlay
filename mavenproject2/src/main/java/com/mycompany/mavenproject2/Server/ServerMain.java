/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject2.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author ggunes
 */
public class ServerMain {
    
    private static final int PORT = 5000;
    
    // To check all handlers that are connected
    public static List<ClientHandler> allClients = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        System.out.println("Yahtzee SERVER is running");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listens in port  " + PORT );
        

            while (true) {
                // When a new client is connected, generate a new socket
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection accepted: " + clientSocket.getInetAddress().getHostAddress());

                // For each client, create a separate Handler
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                allClients.add(clientHandler);
                
                // Thread runs
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            
        }
    }
    
}
