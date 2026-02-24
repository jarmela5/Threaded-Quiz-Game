package GameState;

public class CountDownLatch {
    private int count;
    private final int bonusCount;  // Número de jogadores que recebem bonificação
    private final int waitPeriod;  // Timeout em segundos
    private boolean timeoutOccurred = false;
    private long startTime;
    private int answered;

    public CountDownLatch(int count, int bonusCount, int waitPeriod) {
        this.count = count;
        this.bonusCount = bonusCount;
        this.waitPeriod = waitPeriod;
        this.startTime = System.currentTimeMillis();
        this.answered=0;

    }

    // Decrementa count e retorna fator de pontuação baseado na ordem e tempo
    public synchronized int countDown() {
        int factor = 1; // valor padrão

        answered++;
         // primeiros jogadores
        if( answered<=bonusCount){
            factor = 2; // bonus x2
        }
        count--;
        if(count <= 0) {
            notifyAll();
        }

        int maxPoints=30;

        return factor;
    }

    // Bloqueia até todos responderem ou timeout
    public synchronized void await() throws InterruptedException {
        while(count > 0 && !timeoutOccurred) {
            wait();
        }


    }
    public synchronized void signalTimeout() {
            if (count > 0) {
                timeoutOccurred = true;
                notifyAll();
            }

    }

    public boolean hasTimeoutOccurred() {
        return timeoutOccurred;
    }
    public boolean allResponded() {

        return count <= 0;
    }
    public void resetTimeout() {
        timeoutOccurred=false;
    }
}

