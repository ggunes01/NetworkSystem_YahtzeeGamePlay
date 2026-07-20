   Multiplayer Yahtzee Game — Network System Design Project
A fully functional, real-time multiplayer Yahtzee game built with Java, featuring a custom TCP/IP client-server architecture, multi-threaded connection handling, and a Java Swing graphical interface.

 About the Project
This project was developed as a Network System Design course project at Fatih Sultan Mehmet Foundation University. The goal was to design and implement a networked multiplayer game from scratch — without relying on any game framework — to demonstrate real-world understanding of socket programming, concurrent server design, and network message protocols.
Two players can connect to a shared server, create or join game rooms using unique room IDs, and play a complete game of Yahtzee in real time over a TCP connection.

 Features

 Real-time multiplayer over TCP/IP sockets
 Room-based matchmaking — create a room and share the ID with a friend to join
 Full Yahtzee ruleset — all 13 scoring categories including bonuses
 In-game chat between connected players
 Turn management handled entirely on the server side
 Dice hold mechanic — keep specific dice between rolls
 Auto-reconnect support on unexpected disconnection
 Configurable server via system properties or environment variables
 Graceful disconnect handling — notifies the remaining player when someone leaves


 Technologies Used
TechnologyPurposeJava 25Core language (with preview features enabled)Java Sockets (TCP/IP)Client-server communicationJava Object SerializationSending GameState and NetworkMessage objects over the networkJava MultithreadingEach client connection runs on a dedicated threadConcurrentHashMapThread-safe storage for active game sessionsJava SwingGraphical user interfaceApache MavenBuild and dependency managementNetBeans AbsoluteLayoutUI form layout

 Network Protocol
Communication between client and server uses a custom message protocol built on top of Java Object Serialization. Every message is wrapped in a NetworkMessage object that contains:

MessageType — the action being performed
data — payload (e.g. a GameState, a room ID, a chat string)
sender — identifier of the message origin

Message TypeDirectionDescriptionCREATE_GAMEClient → ServerRequest to create a new roomCREATE_GAMEServer → ClientResponse with the generated room IDJOIN_GAMEClient → ServerRequest to join an existing room by IDJOIN_GAMEServer → BothBroadcast: game is startingGAME_UPDATEBothSync the full GameState to all playersSAVE_SCOREClient → ServerPlayer selects a scoring categoryCHATBothIn-game chat messageERRORServer → ClientRoom not found, room full, or player disconnected

 How to Run
Prerequisites

Java 25 (preview features must be enabled)
Apache Maven

Player 1 clicks "Create a New Game" and shares the generated room ID.
Player 2 clicks "Join the Game", enters the room ID, and connects.
The game starts automatically once both players are in the room.
