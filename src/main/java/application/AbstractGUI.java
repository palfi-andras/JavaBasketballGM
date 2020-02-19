package application;

import javafx.scene.layout.BorderPane;

/**
 * CS -622
 * AbstractGUI.java
 * <p>
 * * The AbstractGUI class defines the common GUI format for all of the screens and views within JavaBasketballGM. It
 * * defines that each subscreen of the program must use a BorderPane for its content window
 *
 * @author Andras Palfi apalfi@bu.edu
 * @version 1.0
 */
class AbstractGUI {
    // One BorderPane per screen
    private final BorderPane rootPane;

    AbstractGUI() {
        this.rootPane = new BorderPane();
    }

    BorderPane getRootPane() {
        return this.rootPane;
    }

}
