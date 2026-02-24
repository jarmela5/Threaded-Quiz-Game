package TUI_servidor;

import GameState.GameState;


public class CustomThreadPool {
    private static final int MAX_ACTIVE_GAMES = 5; // Limite de jogos ativos

    private int activeGames = 0; // Número de jogos atualmente ativos
    private final Object lock = new Object(); // Lock para sincronização


    public void acquireGameSlot(String gameCode) throws InterruptedException {
        synchronized (lock) {
            // Espera enquanto o limite de jogos ativos for atingido
            while (activeGames >= MAX_ACTIVE_GAMES) {
                
                lock.wait();
            }


            activeGames++;
          
        }
    }


    public void releaseGameSlot(String gameCode) {
        synchronized (lock) {
            if (activeGames > 0) {
                activeGames--;
               
                lock.notifyAll(); // Notifica todos os jogos em espera
            }
        }
    }


    public synchronized int getActiveGames() {
        return activeGames;
    }


    public int getMaxActiveGames() {
        return MAX_ACTIVE_GAMES;
    }
}

