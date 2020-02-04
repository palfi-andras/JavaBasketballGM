package core;

import org.junit.Test;

public class TestLeague {

    public TestLeague() {
        League.getInstance();
        League.getInstance().automatedDraft();
    }

    /**
     * Tests whether the league singleton is loaded or not
     */
    @Test
    public void testLeagueIsLoaded() {
        assert League.loaded();
    }

    /**
     * Validates the league initialization process. We should have the required amount of teams and players.
     * Also each player should have their required attributes set
     */
    @Test
    public void testLeagueIsInitialized() {
        assert League.getInstance().getNumTeams() == League.NUM_TEAMS && League.getInstance().getNumPlayers() == League.NUM_PLAYERS;
        for (AbstractEntity entity : League.getInstance().getEntities()) {
            if (entity instanceof Player) {
                for (PlayerAttributes attribute : PlayerAttributes.values())
                    assert (entity.entityAttributeExists(attribute.toString()));
            }
            if (entity instanceof Team) {
                for (TeamAttributes attribute : TeamAttributes.values())
                    assert entity.entityAttributeExists(attribute.toString());
            }
        }
    }

}
