package GameState;


import TUI_servidor.CustomThreadPool;
import perguntas.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameState {

    //Configuração e Identificação
    private final String gameCode;              // Código único para identificar o jogo
    private final int maxTeams;
    private final int playersPerTeam;
    private boolean acabou = false; // Estado do jogo (À espera, A decorrer, Terminado)

    // Perguntas
    private final List<Question> allQuestions; // Lista de perguntas carregadas
    private int currentQuestionIndex = 0;      // Índice da pergunta atual


    // Participantes (Estruturas Thread-Safe)

    private final Map<String, Team> teams;   // Mapeia ID da Equipa -> Objeto Equipa
    private final Map<String, Player> players; // Mapeia Username do Jogador -> Objeto Jogador

    //  Gestão de Respostas da Ronda Atual


    private final Map<String, Resposta> currentRoundAnswers;
    private List<Resposta> respostasLis;

    private CountDownLatch latch;

    private int contadorJogapassou;
    private volatile int tempoRestante; // segundos
    private volatile boolean timerRunning = false;
    private Map<String, Barreira> teamBarriers;
    private boolean FinalizarTeam = false;


    private int stopTime;

    private int PlayersWaiting;

    private CustomThreadPool threadPool; // Referência à ThreadPool do servidor
    private boolean threadPoolSlotAcquired = false; // Flag para garantir que apenas uma thread adquire o slot
    private Thread timerThread;




    public GameState(String code, int maxTeams, int playersPerTeam, List<Question> questions) {
        this.gameCode = code;
        this.maxTeams = maxTeams;
        this.playersPerTeam = playersPerTeam;
        this.allQuestions = questions;


        this.teams=  new HashMap<>();

        this.players = new HashMap<>();

        this.currentRoundAnswers = new HashMap<>();

        this.respostasLis=new ArrayList<>();
        stopTime=0;
        PlayersWaiting=0;


    }




    public List<Question> getAllQuestions() {
        return allQuestions;
    }

    public int getQuestionsSize() {
        return allQuestions.size();
    }

    public Map<String, Resposta> getCurrentRoundAnswers() {
        return currentRoundAnswers;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public void setAcabou(){
        this.acabou=true;
        // Liberta o slot na ThreadPool quando o jogo termina
        if (threadPool != null && threadPoolSlotAcquired) {
            threadPool.releaseGameSlot(gameCode);
            threadPoolSlotAcquired = false;
            //System.out.println("Jogo " + gameCode + " terminou - slot libertado na ThreadPool");
        }
    }

    public Question getCurrentQuestion() {

        return allQuestions.get(currentQuestionIndex);
    }

    public Map<String, Player> getPlayers() {
        return players;
    }
    public boolean TeamFull(String codeTeam) {
       if(teams.get(codeTeam).getNumPlayers()==playersPerTeam) {
    	   return true;
       }else
    	   return false;
    }
    
    public void removePlayer(Player pla) {
    	players.remove(pla.getUsername());
    }



    public synchronized void reciveQuestion(String teamName ,Resposta resposta) {

        int opcao = resposta.getResposta();


        respostasLis.add(resposta);


        if (getCurrentQuestion().getCorrect() == opcao) {
            if (teams.containsKey(teamName)) {
                Team team = teams.get(teamName);
                int points = resposta.getPontos();
                team.addScore(points);
                team.addPontosUltimaPergunta(points);

            }

        }

        if (latch.allResponded()) {
            avancarPergunta();
            respostasLis.clear();
            startTeamQuestion(30);

        }
    }




    public  void reciveQuestionTeam(String teamName ,Resposta resposta) throws InterruptedException {
        respostasLis.add(resposta);
        FinalizarTeam = false;
        boolean isTeamRound = (currentQuestionIndex % 2 == 1);

        if (isTeamRound) {
            Barreira b =teamBarriers.get(teamName);

            if (b != null ) {

                b.await(tempoRestante);

            }

            // Se todas as equipas já terminaram, avançar para a próxima pergunta
            if (todasAsEquipasTerminaram() && !FinalizarTeam) {
                FinalizarTeam = true;
                avancarPergunta();
                startIndividualQuestion(2, 30);
            }

        }
    }

    private boolean todasAsEquipasTerminaram() {
        if (teamBarriers == null) return true;
        for (String teamId : teamBarriers.keySet()) {
            Barreira b = teamBarriers.get(teamId);
            if (b == null || !b.isBroken()) {
                return false;
            }
        }
        return true;
    }





    public int getTempoRestante() {
        return tempoRestante;
    }



    public PlacarMensagem listarplacar(){
        Map<String, Integer> placarParaGUI = new HashMap<>();
        Map<String, Integer> placarPontosEquipa = new HashMap<>();

        for (Team team : teams.values()) {
            placarParaGUI.put(team.getTeamId(), team.getScore());
            placarPontosEquipa.put(team.getTeamId(), team.getPontosUltimaPergunta());


        }


        return new PlacarMensagem(placarParaGUI,placarPontosEquipa,0);

    }



    public synchronized void addPlayer(Player player) {
        this.players.put(player.getUsername(), player);
        if(players.size()==allPlayers()){
            if (threadPool != null && !threadPoolSlotAcquired) {
                try {
                    threadPool.acquireGameSlot(gameCode);
                    threadPoolSlotAcquired = true;
                   // System.out.println("Jogo " + gameCode + " adquiriu slot na ThreadPool (todos os jogadores ligados)");
                } catch (InterruptedException e) {
                    System.err.println("Erro ao adquirir slot na ThreadPool para jogo " + gameCode + ": " + e.getMessage());
                }
            }

            startIndividualQuestion(2, 30);
            notifyAll();
            timerRunning = true;

            timerThread = new Thread(this::runTimer);
            timerThread.start();

        }
    }


    public void setThreadPool(CustomThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public synchronized void awaitAllPlayers() throws InterruptedException {
        while (players.size() < allPlayers()) {
            wait(); // espera até que todos os jogadores entrem
        }
    }

    public synchronized void AwaitForNextRound() throws InterruptedException {
        PlayersWaiting++;
        if(PlayersWaiting==allPlayers()) {
            PlayersWaiting=0;

            notifyAll();
        }
        else
            while (PlayersWaiting!=0) {
                wait(); // espera até que todos os jogadores entrem
            }

    }

    public synchronized void addPlayerToTeam(String teamId, Player player) {
        teams.get(teamId).addPlayer(player);
    }

    public synchronized void addTeam(Team team) {
        this.teams.put(team.getTeamId(), team);
    }

    public boolean existsTeam(String teamId) {
        return teams.containsKey(teamId);
    }



    // Perguntas individuais
    public void startIndividualQuestion(int bonusCount, int waitTime) {

        tempoRestante = waitTime;
        latch = new CountDownLatch(maxTeams*playersPerTeam, bonusCount, waitTime);
        latch.resetTimeout();

    }
    //perguntas de equipa
    public void startTeamQuestion(int waitTimeSeconds) {
        FinalizarTeam = false;
        tempoRestante = waitTimeSeconds;
        respostasLis.clear();

        // criar Mapa de barreiras, uma barreira por equipa
        teamBarriers = new HashMap<>();

        for (Team team : teams.values()) {
            int numPlayers = team.getPlayers().size();


            Barreira b = new Barreira(numPlayers, () -> {
                processarPontuacaoEquipa(team);

            });

            teamBarriers.put(team.getTeamId(), b);
        }



    }


    private void processarPontuacaoEquipa(Team team) {
        // Copiar respostas da ronda atual
        List<Resposta> respostasEquipa = new ArrayList<>();
        synchronized (this) { // sincroniza sobre o GameState para evitar concorrência com reciveQuestion
            for (Resposta r : respostasLis) {
                if (team.getPlayers().stream().anyMatch(p -> p.getUsername().equals(r.getUsername()))) {
                    respostasEquipa.add(r);

                }
            }
        }
        int pontos;
        // Caso extremo: ninguém respondeu antes do timeout
        if (respostasEquipa.isEmpty() ) {

            pontos=0;
        }else {

            Question q = getCurrentQuestion();

            boolean todosCertos = respostasEquipa.stream()
                    .allMatch(r -> r.getResposta() == q.getCorrect());


            if (todosCertos && respostasEquipa.size() == team.getPlayers().size()) {
                pontos = q.getPoints() * 2*team.getPlayers().size();
            } else {
                pontos = respostasEquipa.stream()
                        .filter(r -> r.getResposta() == q.getCorrect())
                        .mapToInt(r -> q.getPoints())
                        .max()
                        .orElse(0);

            }

        }
        team.addPontosUltimaPergunta(pontos);
        team.addScore(pontos);

    }



    // Timer centralizado
    private void runTimer() {
        try {
            while (!acabou) {

                if (timerRunning) {
                    Thread.sleep(1000);


                    tempoRestante--;


                    //  Verificar se estamos numa ronda INDIVIDUAL
                    boolean isTeamRound = (currentQuestionIndex % 2 == 1);

                    if (!isTeamRound) {
                        //  RONDA INDIVIDUAL
                        if (latch != null && !latch.allResponded() && !latch.hasTimeoutOccurred()) {

                            if (tempoRestante <= 0) {
                                // aciona timeout do latch
                                latch.signalTimeout();


                                advanceRoundTeam();
                            }
                        }
                    } else {

                        if (teamBarriers != null) {
                            if (tempoRestante <= 0) {
                                FinalizarTeam = false;

                                // "rebenta" todas as barreiras manualmente
                                for (Barreira b : teamBarriers.values()) {
                                    if (b != null && !b.isBroken()) {
                                        b.forceOpen(); // substituído await(0) por forceOpen()
                                    }
                                }


                                // quando TODAS estiverem abertas avança
                                if (todasAsEquipasTerminaram() && !FinalizarTeam ) {
                                    FinalizarTeam = true;

                                    advanceRoundIndividual();
                                }
                            }
                        }
                    }
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void gameStopTime(){
        stopTime++;
        if(stopTime==allPlayers()){

            timerRunning=false;
            stopTime=0;
        }

    }

    public synchronized void gameStartTime(){
        stopTime++;

        if(stopTime==allPlayers()){
            timerRunning=true;
            stopTime=0;
            for (Team team : teams.values()) {
                team.resetPontosUltimaPergunta();
            }
        }
    }

    private void advanceRoundIndividual() {
        avancarPergunta();
        respostasLis.clear();
        startIndividualQuestion(2, 30);
    }

    private void advanceRoundTeam() {
        avancarPergunta();
        respostasLis.clear();

        startTeamQuestion(30);
    }



    public  void startCountDownLatch(){
        //iniciar a primeira pergunta de estado
        contadorJogapassou++;
        if(contadorJogapassou==maxTeams*playersPerTeam) {
            startIndividualQuestion(2, 30);
            contadorJogapassou=0;
        }
    }

    public int countDownIndividual() {
        return latch.countDown();
    }

    public void awaitIndividuo(int round) throws InterruptedException {
        if(round==currentQuestionIndex)
            latch.await();
    }

    // Avança para próxima pergunta
    public void avancarPergunta() {
        currentQuestionIndex++;

    }



    public boolean rodadaTerminou() {
        return respostasLis.size() >= players.size() || (latch != null && latch.hasTimeoutOccurred());
    }

    public int allPlayers(){
        return maxTeams*playersPerTeam;
    }

    public void awaitEquipa(String teamId, int round) throws InterruptedException {
        if (round != currentQuestionIndex) return;

        Barreira b = teamBarriers.get(teamId);
        if (b != null) {
            b.await(tempoRestante);
        }
    }



    public void terminar() {
        System.out.println("Encerrando jogo da sala " + gameCode);

        // Libertar slot da ThreadPool
        if (threadPool != null) {
            threadPool.releaseGameSlot(gameCode);
        }


        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }


        // Fechar sockets dos jogadores
        for (Team t : teams.values()) {
            for (Player p : t.getPlayers()) {
                try {
                    if (p.getSocket() != null && !p.getSocket().isClosed()) {
                        p.getSocket().close();
                    }
                } catch (IOException ignored) {

                }
            }
        }

        System.out.println("Jogo da sala " + gameCode + " encerrado.");
    }



}