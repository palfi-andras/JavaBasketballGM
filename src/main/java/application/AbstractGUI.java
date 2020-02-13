package application;

import javafx.scene.layout.BorderPane;

public class AbstractGUI {
    private final BorderPane rootPane;

    AbstractGUI() {
        this.rootPane = new BorderPane();
    }

    BorderPane getRootPane() {
        return this.rootPane;
    }

}
