package application;


import core.League;
import core.Utils;

//public class Main extends Application {
public class Main {
    //@Override
    //public void start(Stage primaryStage) throws Exception{
    //    Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
    //    primaryStage.setTitle("Hello World");
    //    primaryStage.setScene(new Scene(root, 300, 275));
    //    primaryStage.show();
    //}


    public static void main(String[] args) {
        // launch(args);
        League l = Utils.loadLeague("./resources/league1.json");
        System.out.println();
    }
}
