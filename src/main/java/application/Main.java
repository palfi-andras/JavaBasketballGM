package application;


import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.ShellFactory;
import core.League;
import core.Utils;

import java.io.IOException;

/**
 * The Main class of the JavaBasketballGM is a controller using the Cliche shell. Each command prefaced here with @Command
 * is a valid command in the program shell
 */
public class Main {


    /**
     * Start the console shell for the program.
     */
    public static void main(String[] args) throws IOException {
        ShellFactory.createConsoleShell("cmd", "JavaBasketballGM", new Main()).commandLoop();
    }

    /**
     * Command to load a league file
     *
     * @param filePath path to JSON file of previous league instance
     */
    @Command(description = "Load a previously saved league")
    public void loadLeague(@Param(name = "filePath", description = "File path to the league.json file to load")
                                   String filePath) {
        Utils.loadLeague(filePath);
    }

    /**
     * Save the currently loaded league to a JSON file. Will complain if no league is loaded.
     */
    @Command(description = "Save this current league")
    public void saveLeague() {
        if (!League.loaded()) {
            System.out.println("Error: no league loaded. Either use 'll' to load a previous one or 'nl' to " +
                    "create a new one.");
            return;
        }
        Utils.saveLeague(League.getInstance());
    }

    /**
     * Create a new league object. Will complain if a league is already loaded.
     */
    @Command(description = "Create a new league")
    public void newLeague() {
        if (League.loaded()) {
            System.out.println("Warning: A league is already loaded!");
            return;
        }
        League.getInstance();
    }

    /**
     * Perform an automated draft of the free agents amongst the teams in the league
     */
    @Command(description = "Perform an automated draft of all players in the league")
    public void performAutomatedDraft() {
        League.getInstance().automatedDraft();
    }

    /**
     * Command to setup a round robin tournament amongst the teams
     */
    @Command(description = "Setup a round robin tournament between the teams")
    public void setupRoundRobin() {
        League.getInstance().setupRoundRobinTournament();
    }

    /**
     * Command to simulate a round robin tournament
     */
    @Command(description = "Simulate Round Robin Tournament")
    public void simulateRoundRobin() {
        League.getInstance().simulateRoundRobinTournament();
    }

    /**
     * Command to list all of the teams in the league and their IDs
     */
    @Command(description = "List all teams")
    public void listTeams() {
        if (!League.loaded()) {
            System.out.println("ERROR: No League is loaded!");
            return;
        }
        League.getInstance().printAllTeams();
    }
}
