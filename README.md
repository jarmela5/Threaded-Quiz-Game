# Threaded-Quiz-Game

A real-time multiplayer quiz game system where teams compete to answer questions correctly.

## Features

- **Centralized server** with a text-based interface (TUI)
- **GUI clients** for real-time participation
- **Team system** – Play in teams
- **Dynamic questions** loaded from JSON
- **Real-time scoreboard** synchronized across clients
- **Client-server architecture** with efficient threading

## Technologies

- **Java 17**
- **Maven** for dependency management
- **Gson** for JSON parsing
- **Custom Thread Pool** for connection management

## Project Structure

The code is organized to separate network logic from the user interface:

```text
src/
├── GameState/ # Match state logic (Synchronization and Scoring)
├── GUI_cliente/ # Graphical interface (Swing/JavaFX) for players
├── TUI_servidor/ # Text-based interface (Console) for server monitoring
└── perguntas/ # Module for loading and managing the quiz database
```

## How to Play

To run the project, you need to start the Server first and then connect the Clients.

### 1. Start the Server
Run the server application to manage connections and rooms:

```bash
java MainServer.java
```
- Follow the instructions shown in the TUI (Text User Interface).
- Use the TUI to create and manage game rooms (salas).

### 2. Start the Clients
For each player, run the client application:
```bash
java ClienteEntrada.java localhost 8008 idSala idTeam idPlayer
```
Arguments explanation:
- localhost – Server address
- 8008 – Server port
- idSala – Game room ID
- idTeam – Team ID
- idPlayer – Player ID

Each client will connect to the server and participate in the quiz in real time.


## Authors

- José Jarmela (122663)

- João Daniel (122670)

