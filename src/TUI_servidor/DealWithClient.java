package TUI_servidor;

// package TUI_servidor;

import GUI_cliente.GUI_cliente_placar;
import GameState.*;
import perguntas.Question;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;







public class DealWithClient implements Runnable {

    private final Socket clientSocket;
    private final Servidor servidor;
    private GameState game;


    //private volatile long tempoRestante = 30; // segundos
    private volatile boolean responderam = false; // sinaliza se o jogador respondeu
    private boolean acabou = false;




    public DealWithClient(Socket socket, Servidor servidor) {
        this.clientSocket = socket;
        this.servidor = servidor;

    }

    @Override
    public void run() {

        try {
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

            // Receber dados iniciais do cliente
            String[] dados= (String[]) in.readObject();
            String codigoJogo=dados[0];
            String codigoEquipa=dados[1];
            String nomeUtilizador =dados[2];

            //validar ssala
            GameState game = servidor.getGameState(codigoJogo);
            if (game == null) {
                out.writeObject("Sala não encontrada");
                out.flush();
                clientSocket.close();
                return;
            }
            this.game = game;

            // Validar username único
            if (game.getPlayers().containsKey(nomeUtilizador)) {
                out.writeObject("Username já em uso");
                out.flush();
                clientSocket.close();
                return;
            }


            out.writeObject("OK");
            out.flush();

            // Adiciona jogador se ainda não existir
            Player jogador = new Player(nomeUtilizador,clientSocket);

            game.addPlayer(jogador);
            if(game.existsTeam(codigoEquipa)){
            	if(game.TeamFull(codigoEquipa)) {
            		game.removePlayer(jogador);
            		out.writeObject("Equipa já está cheia");
                    out.flush();
                    clientSocket.close();
                    return;
            	}
                    game.addPlayerToTeam(codigoEquipa, jogador);
            }else {
            	
                game.addTeam(new Team(codigoEquipa));
                game.addPlayerToTeam(codigoEquipa, jogador);
            }

           game.awaitAllPlayers();
            //enviar primeira pergunta
            Object quest = game.getCurrentQuestion();
            out.writeObject(quest);

            out.flush();




            //  THREAD DO TIMER

            Thread timerThread = new Thread(() -> {
                try {
                    executarTimer(out, codigoEquipa);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            timerThread.start();

            // Loop principal de comunicação
            while (!acabou) {




                Object recebido = in.readObject();


                if(recebido instanceof String msg && msg.equals("Placar")){

                    PlacarMensagem placa =game.listarplacar();
                    out.writeObject(placa);
                    out.flush();


                } else if (recebido instanceof String msg && msg.equals("timeout")) {

                    if(!responderam)
                        avancarperguntaDC(out,nomeUtilizador);

                }else if (recebido instanceof Resposta resposta) {

                    int currentRound = game.getCurrentQuestionIndex();
                    boolean isTeamRound = (currentRound % 2 == 1);
                    if (!isTeamRound) {
                        int facto = game.countDownIndividual();
                        int pontos =  (game.getCurrentQuestion().getPoints() * facto);
                        resposta.setPontos(pontos);
                        resposta.setTime(game.getTempoRestante());

                        game.reciveQuestion(codigoEquipa, resposta);
                        responderam = true;

                        // Aguarda fim da ronda ou timeout

                        game.awaitIndividuo(currentRound);

                    } else {
                        resposta.setTime(game.getTempoRestante()+1);
                        responderam = true;

                        game.reciveQuestionTeam(codigoEquipa, resposta);


                    }

                    avancarperguntaDC(out,nomeUtilizador);


                }

            }

        }catch(Exception e) {
            e.printStackTrace();
        }

    }


    private synchronized void executarTimer(ObjectOutputStream out, String codigoEquipa) throws IOException, InterruptedException {
        while (!acabou) {

            if( !responderam) {
                out.writeObject(game.getTempoRestante());
                out.flush();
                Thread.sleep(1000);
            }
        }
    }



    private void avancarperguntaDC(ObjectOutputStream out, String nomeUtilizador) throws IOException, InterruptedException {

        game.gameStopTime();


        game.AwaitForNextRound();


        PlacarMensagem placa =game.listarplacar();
        out.writeObject(placa);
        out.flush();


        game.gameStartTime();



        if (game.getCurrentQuestionIndex() >= game.getQuestionsSize()) {
            out.writeObject("FIM");
            out.flush();


            new GUI_cliente_placar(game.listarplacar(), null);
            acabou = true;
            game.setAcabou();
            clientSocket.close();
            return;
        }



        Question q = game.getCurrentQuestion();
        out.writeObject(q);
        out.flush();
        responderam = false;


    }





}