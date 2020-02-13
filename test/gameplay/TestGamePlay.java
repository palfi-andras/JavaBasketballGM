package gameplay;

import core.League;
import org.junit.Test;

public class TestGamePlay {

    @Test
    public void testGamePlay() {
        League.getInstance().automatedDraft();
        League.getInstance().setupRoundRobinTournament();
        League.getInstance().simulateRoundRobinTournament();
        League.getInstance();
    }
}
