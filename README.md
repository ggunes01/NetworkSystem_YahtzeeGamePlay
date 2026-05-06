# Network System Design Project: Multiplayer Yahtzee Game

This project is a multiplayer Yahtzee game developed in Java using a client-server architecture. The main goal of the project is to demonstrate socket programming, real-time communication, and basic network system design through an interactive desktop game.

The game allows two players to connect to a central server and play Yahtzee against each other in real time. The server manages the communication between clients, controls the game flow, and keeps the players synchronized during the session. The client side includes a graphical user interface built with Java Swing, allowing users to roll dice, select score categories, view their scores, and follow the opponent’s progress.

The project is also designed to be deployed on an AWS EC2 instance, so clients can connect to a remote server instead of running everything locally. This makes the application closer to a real network-based multiplayer system.

## Features

- Multiplayer gameplay for two players
- Client-server architecture
- Real-time communication using Java sockets
- Graphical user interface built with Java Swing
- Dice rolling and Yahtzee score calculation
- Score tracking for both players
- Replayable game sessions
- Server deployment support on AWS EC2
- Clear separation between client and server logic

## Technologies Used

- Java
- Java Swing
- Socket Programming
- TCP/IP Communication
- Client-Server Architecture
- AWS EC2
- Object-Oriented Programming

## Project Architecture

The system consists of one server and two clients. The server listens for incoming client connections and manages the communication between players. Each client connects to the server through sockets and sends player actions such as rolling dice, selecting score categories, and ending turns.

The server is responsible for:

- Accepting client connections
- Managing two-player sessions
- Sending game updates to both clients
- Synchronizing turns between players
- Keeping the game state consistent

The client is responsible for:

- Displaying the game interface
- Sending player actions to the server
- Receiving updates from the server
- Showing scores, dice values, and turn information

## Purpose of the Project

This project was created as part of a Network System Design course. It helped us understand how network-based applications work, especially how clients and servers communicate using sockets. By developing a multiplayer game, we practiced important concepts such as TCP communication, message exchange, synchronization, and remote server deployment.

## AWS EC2 Usage

The server side of the application can be hosted on an AWS EC2 virtual machine. This allows players to connect to the game server from different devices over the internet. Using AWS EC2 made the project more realistic because the server does not need to run on the same local machine as the clients.

## Future Improvements

- Support for more than two players
- Login and user account system
- Match history database
- Improved user interface design
- Better error handling for disconnected clients
- Online lobby system
- Chat feature between players