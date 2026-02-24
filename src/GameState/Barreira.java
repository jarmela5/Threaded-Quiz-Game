package GameState;

import java.util.concurrent.locks.*;

public class Barreira {

    private final int total;
    private int count;

    private final Runnable action;
    private boolean broken = false;   // indica se já abriu
    private boolean actionExecuted = false; // garante que action.run() só corre uma vez

    private final Lock lock = new ReentrantLock();
    private final Condition arrivedAllPlayers = lock.newCondition();

    public Barreira(int total, Runnable action) {
        this.total = total;
        this.count = total;
        this.action = action;
    }

    public void await(long timeoutSeconds) throws InterruptedException {
        lock.lock();
        try {

            if (broken) {
                return; // já abriu antes
            }

            count--;

            // último a chegar: abre a barreira normalmente
            if (count == 0) {
                openBarrier(); // abre e executa a ação


            }else {

                //ainda faltam jogadores, então espera
                long nanos = timeoutSeconds * 1_000_000_000L;

                while (!broken && count > 0 && nanos > 0) {

                    nanos = arrivedAllPlayers.awaitNanos(nanos);

                }


            }

        } finally {
            lock.unlock();
        }
    }

    private void openBarrier() {
        broken = true;

        if (!actionExecuted && action != null) {
            actionExecuted = true;
            action.run();
        }

        arrivedAllPlayers.signalAll();
    }

    public boolean isBroken() {
        lock.lock();
        try {
            return broken;
        } finally {
            lock.unlock();
        }
    }

    public void forceOpen() {
        lock.lock();
        try {
            if (!broken) {
                openBarrier();   // abre sem mexer no count nem fazer await()
            }
        } finally {
            lock.unlock();
        }
    }

    public int getCount(){
        return count;
    }

}