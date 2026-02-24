package GameState;

import java.net.Socket;

public class Player {
    private final String username;
    private int score;

    private final Socket socket;

    public Player(String username, Socket socket) {
        this.username = username;

        this.socket=socket;
    }

    public Socket getSocket() {
        return socket;
    }
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public String getUsername() {
        return username;
    }


    @Override
    public String toString() {
        return "Player=" + username + ", Pontos=" + score;
    }


}
