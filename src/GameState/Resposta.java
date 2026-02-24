package GameState;

public class Resposta implements java.io.Serializable{
    private final int resposta;
    private long time;
    private int pontos;
    private final String username;

    public Resposta(int resposta, long time, String username) {
        this.resposta = resposta;
        this.time = time;
        this.username=username;
        pontos=0;
    }

    public int getResposta() {
        return resposta;
    }
    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time=time;
    }
    public int getPontos() {
        return pontos;
    }
    public void setPontos(int pontos) {
        this.pontos=pontos;
    }
    public String getUsername() {return username;
    }
    @Override
    public String toString() {
        return "Resposta=" + resposta + ", Tempo=" + time;
    }
}