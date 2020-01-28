package application;

import core.AbstractEntity;
import core.League;

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
        for (AbstractEntity entity : League.getInstance().getEntities())
            System.out.println(entity.toString());
        League.getInstance();
    }
}
