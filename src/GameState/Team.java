package GameState;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private final String TeamId;
    private final List<Player> players;
    private int score;
    private int pontosUltimaPergunta;

    public Team(String TeamId) {
        this.TeamId = TeamId;
        this.players = new ArrayList<>();
        pontosUltimaPergunta=0;
    }

    public Team(String TeamId, Player[] players) {
        this.TeamId = TeamId;
        this.players = new ArrayList<>();
        for (Player player : players) {
            this.players.add(player);
        }
    }

    public int getPontosUltimaPergunta() {
        return pontosUltimaPergunta;
    }
    public void addPontosUltimaPergunta(int pontosUltimaPergunta) {
        this.pontosUltimaPergunta += pontosUltimaPergunta;
    }
    public void resetPontosUltimaPergunta(){
        this.pontosUltimaPergunta=0;
    }
    public String getTeamId() {
        return TeamId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getNumPlayers() {
        return players.size();
    }

    public void addPlayer(Player player) {
        players.add(player);
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
    @Override
    public String toString() {
        return "Equipe=" + TeamId + ", Jogadores=" + players;
    }

}
