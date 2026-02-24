package TUI_servidor;

import java.util.HashMap;
import java.util.Map;
import GameState.*;
public class MainServidor {

    //porta do servidor
    private static final int PORT = 8008;


    public static void main(String[] args) {
        Servidor servidor = new Servidor(PORT);          // cria o servidor

        servidor.iniciar();
        ServidorTUI tui = new ServidorTUI(servidor); // cria a interface textual
        tui.run();                                   // inicia o menu
    }
}

