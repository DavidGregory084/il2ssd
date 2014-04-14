package uk.org.il2ssd.jfx;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * Instantiation from FXML
 */
public class ConsolePresenter {
    @FXML
    BorderPane consolePane;
    @FXML
    TextField commandEntryField;
    @FXML
    TextArea consoleTextArea;

    public BorderPane getConsolePane() {
        return consolePane;
    }

    public TextField getCommandEntryField() {
        return commandEntryField;
    }

    public TextArea getConsoleTextArea() {
        return consoleTextArea;
    }
}
