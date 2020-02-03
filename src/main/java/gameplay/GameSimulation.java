package gameplay;

import core.Team;

public class GameSimulation {
    Team homeTeam;
    Team awayTeam;

    public GameSimulation(Team home, Team away) {
        setHomeTeam(home);
        setAwayTeam(away);
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public Team simulateGame() {
        Team winningTeam = getHomeTeam().getOverallTeamRating() > getAwayTeam().getOverallTeamRating() ? getHomeTeam()
                : getAwayTeam();
        Team losingTeam = getHomeTeam() == winningTeam ? getHomeTeam() : getAwayTeam();
        System.out.println("Winning Team: " + winningTeam.getName() + " Losing Team: " + losingTeam.getName());
        return winningTeam;
    }
}
