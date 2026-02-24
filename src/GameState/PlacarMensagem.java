package GameState;

import java.io.Serializable;
import java.util.Map;

public class PlacarMensagem implements Serializable {
    private Map<String,Integer> placar;

    private int tipo;
    private Map<String,Integer> placarAntigo;

    public PlacarMensagem(Map<String,Integer> placar,Map<String,Integer> pontosAntigos, int tipo) {
        this.placar = placar;
        this.placarAntigo=pontosAntigos;
        this.tipo = tipo;
    }

    public Map<String, Integer> getPlacar() {
        return placar;
    }

    public void setPlacar(Map<String, Integer> placar) {
        this.placar = placar;
    }

    public Map<String, Integer> getPlacarAntigo() {
        return placarAntigo;
    }

    public void setPlacarAntigo(Map<String, Integer> placarAntigo) {
        this.placarAntigo = placarAntigo;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }


}
