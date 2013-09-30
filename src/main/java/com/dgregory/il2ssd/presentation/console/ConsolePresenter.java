package com.dgregory.il2ssd.presentation.console;

import com.dgregory.il2ssd.business.server.Command;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * 23/09/13 01:11
 * il2ssd
 */
public class ConsolePresenter implements Initializable {

    @FXML
    TextArea consoleTextArea;
    @FXML
    TextField commandEntryField;
    @Inject
    Command command;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.enableControl(false);
    }

    public void outputText(String text) {
        consoleTextArea.appendText(text);
    }

    public void enableControl(Boolean enable) {
        if (enable) {
            commandEntryField.setDisable(false);
            commandEntryField.clear();
            consoleTextArea.clear();
        } else {
            commandEntryField.setDisable(true);
            commandEntryField.clear();
            consoleTextArea.setText("<disconnected>");
        }
    }

    @Inject
    public void enterCommand(KeyEvent keyEvent) {
        if (keyEvent.getCode().getName().equals("Enter")) {
            if (commandEntryField.getText().equals("clear")) {
                consoleTextArea.clear();
                commandEntryField.clear();
            } else {
                command.sendCommand(commandEntryField.getText());
                commandEntryField.clear();
            }
        }

    }


}
