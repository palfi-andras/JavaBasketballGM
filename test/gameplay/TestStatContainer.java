package gameplay;

import core.League;
import core.Utils;
import org.junit.Test;

public class TestStatContainer {

    @Test
    public void testPayerStatContainer() {
        StatContainer<PlayerStat, Double> playerStatContainer = new StatContainer<>();
        // Add 50 historical values of each stat
        for (int i = 0; i < 50; i++) {
            for (PlayerStat stat : PlayerStat.values())
                playerStatContainer.updateStat(stat,
                        Utils.round(League.getInstance().getRandomDouble(1.0, 25.0), 2));
        }
        System.out.println(playerStatContainer.toString());
    }

    @Test
    public void testTeamStatContainer() {
        StatContainer<TeamStat, Integer> teamStatIntegerStatContainer = new StatContainer<>();
        // Add 50 historical values of each stat
        for (int i = 0; i < 50; i++) {
            for (TeamStat stat : TeamStat.values())
                teamStatIntegerStatContainer.updateStat(stat, League.getInstance().getRandomInteger(25));
        }
        System.out.println(teamStatIntegerStatContainer.toString());
    }
}
