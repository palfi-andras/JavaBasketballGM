package application;


import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.ShellFactory;
import core.League;
import core.Utils;

import java.io.IOException;

//public class Main extends Application {
public class Main {
    //@Override
    //public void start(Stage primaryStage) throws Exception{
    //    Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
    //    primaryStage.setTitle("Hello World");
    //    primaryStage.setScene(new Scene(root, 300, 275));
    //    primaryStage.show();
    //}

    public static void main(String[] args) throws IOException {
        // launch(args);
        ShellFactory.createConsoleShell("cmd", "JavaBasketballGM", new Main()).commandLoop();
    }

    @Command(description = "Load a previously saved league")
    public void loadLeague(@Param(name = "filePath", description = "File path to the league.json file to load")
                                   String filePath) {
        Utils.loadLeague(filePath);
    }

    @Command(description = "Save this current league")
    public void saveLeague(@Param(name = "filePath", description = "Name of the file to save the league json file to")
                                   String filePath) {
        if (!League.loaded()) {
            System.out.println("Error: no league loaded. Either use 'll' to load a previous one or 'nl' to " +
                    "create a new one.");
            return;
        }
        Utils.saveLeague(League.getInstance());
    }

    @Command(description = "Create a new league")
    public void newLeague() {
        if (League.loaded()) {
            System.out.println("Warning: A league is already loaded!");
            return;
        }
        League.getInstance();
    }

    @Command(description = "Perform an automated draft of all players in the league")
    public void performAutomatedDraft() {
        League.getInstance().automatedDraft();
    }

    @Command(description = "Setup a round robin tournament between the teams")
    public void setupRoundRobin() {
        League.getInstance().setupRoundRobinTournament();
    }

    @Command(description = "Simulate Round Robin Tournament")
    public void simulateRoundRobin() {
        League.getInstance().simulateRoundRobinTournament();
    }

    @Command(description = "List all teams")
    public void listTeams() {
        if (!League.loaded()) {
            System.out.println("ERROR: No League is loaded!");
            return;
        }
        League.getInstance().printAllTeams();
    }
}
