package core;

import gameplay.GameSimulation;
import javafx.concurrent.Task;

import java.util.AbstractMap;
import java.util.Map;

/**
 * Class to execute a game simulation at a high level. Will return the thread that ran the game and how long it took.
 */
public class GameRunner extends Task<Map.Entry<Thread, Double>> {

    private GameSimulation gs;
    private Thread thread;
    private Double runTime;

    public GameRunner(GameSimulation g) {
        this.gs = g;
    }

    @Override
    protected Map.Entry<Thread, Double> call() throws Exception {
        if (gs.gameIsOver()) {
            assert thread != null && runTime != null;
            return new AbstractMap.SimpleEntry<>(thread, runTime);
        }
        double startTime = System.currentTimeMillis();
        gs.simulateGame();
        runTime = System.currentTimeMillis() - startTime;
        thread = Thread.currentThread();
        return new AbstractMap.SimpleEntry<>(thread, runTime);
    }
}
