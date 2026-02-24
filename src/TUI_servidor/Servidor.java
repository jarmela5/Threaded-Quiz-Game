package TUI_servidor;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import perguntas.*;
import GameState.*;


public class Servidor implements Runnable{



    private final Map<String, GameState> salas = new HashMap<>();
    private final int port;
    private ServerSocket serverSocket;

    //ver perguntas
    private final List<Question> perguntas;

    // ThreadPool para limitar jogos ativos a 5
    private final CustomThreadPool threadPool;

    public Servidor(int port) {
        this.port = port;
        perguntas = jsonLoad.loadQuestions("src/quizzes.json");
        this.threadPool = new CustomThreadPool();
    }

    public CustomThreadPool getThreadPool() {
        return threadPool;
    }


    public String criarSala(int numEquipas, int numPerguntas,int numjogadores) {

        //verifica se há numero suficiciente de perguntas
        if (this.perguntas == null || this.perguntas.size() < numPerguntas) {
            System.out.println(" Erro: Número insuficiente de perguntas disponíveis.");
            return null;
        }

        List<Question> questions = selectQuestions(this.perguntas, numPerguntas);


        String codigo = gerarCodigoUnico();

        GameState gameState = new GameState(codigo,numEquipas,numjogadores,questions);
        gameState.setThreadPool(threadPool); // Define a referência à ThreadPool

        salas.put(codigo, gameState);

        System.out.println("Sala criada: " + codigo);
        return codigo;
    }

    public GameState getGameState(String codigoSala) {
       
        return salas.get(codigoSala);
    }

    private List<Question> selectQuestions(List<Question> perguntas, int numPerguntas) {
        List<Question> questions = new ArrayList<>(perguntas);

        //Baralha a ordem da lista
        Collections.shuffle(questions);

        // Garante que o número de perguntas pedidas não excede o número disponível.
        int questionsToSelect = Math.min(numPerguntas, questions.size());

        // Devolve uma sub-lista com as N primeiras perguntas baralhadas.
        return questions.subList(0, questionsToSelect);
    }

    public List<String> listarSalas() {
        List<String> lista = new ArrayList<>();
        for (var entry : salas.entrySet()) {
            lista.add("Sala " + entry.getKey() + " - " + entry.getValue());
        }
        return lista;
    }

    public GameState iniciarSala(String codigo) {
        GameState sala = salas.get(codigo);
        if (sala == null) {
            System.out.println(" Sala não encontrada.");
            return null;
        }

        return sala;
    }

    public void removerSala(String codigo) {
        if (!salas.containsKey(codigo)) {
            System.out.println("Sala não existe:" + codigo);
        }
        salas.remove(codigo);
        System.out.println("Sala removida: " + codigo);
    }


   public void shutdown() {
       try {
           // Fechar todos os GameStates
           for (GameState game : salas.values()) {
               game.terminar();
           }
           salas.clear();

           // Fechar server socket
           if (serverSocket != null && !serverSocket.isClosed()) {
               serverSocket.close();
           }

           System.out.println("Servidor encerrado corretamente.");
       } catch (IOException e) {
           System.err.println("Erro no shutdown: " + e.getMessage());
       }
   }



    private String gerarCodigoUnico() {
        return "S" + (int)(Math.random() * 9000 + 1000);
    }




    // Metodo para iniciar o servidor (chamado em MainServidor)
    public void iniciar() {
        new Thread(this).start();
    }


    @Override
    public void run() {
        try {
            // Abre o ServerSocket na porta fornecida
            serverSocket = new ServerSocket(port);
            System.out.println("Servidor iniciado na porta: " + port);

            // Loop principal para aceitar conexões de clientes
            while (!serverSocket.isClosed()) {
                try {
                    // espera que um cliente se ligue
                    Socket clientSocket = serverSocket.accept();


                    // ler os argumentos de ligação
                    new Thread(new DealWithClient(clientSocket, this)).start();
                }catch (IOException e) {

                    if (serverSocket.isClosed()) {
                        System.out.println("Servidor foi fechado.");
                    } else {
                        System.err.println("Erro ao aceitar cliente: " + e.getMessage());
                    }
                    break; // SAIR DO LOOP
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar ou executar o servidor: " + e.getMessage());
        }
    }
}

