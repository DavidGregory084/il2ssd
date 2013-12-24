package uk.org.il2ssd;

import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * UI update functions and instantiation from fxml
 */
public class MainPresenter {

    @FXML
    Button connectButton;
    @FXML
    Button disconnectButton;
    @FXML
    ProgressIndicator progressIndicator;
    @FXML
    Button startStopButton;
    @FXML
    Button nextButton;
    @FXML
    TextField commandEntryField;
    @FXML
    TextArea consoleTextArea;
    @FXML
    ChoiceBox<String> missionModeChoice;
    @FXML
    TextField ipAddressField;
    @FXML
    TextField portField;
    @FXML
    MenuItem exitItem;
    @FXML
    MenuItem aboutItem;

    public TextField getIpAddressField() {
        return ipAddressField;
    }

    public TextField getPortField() {
        return portField;
    }

    public Button getConnectButton() {
        return connectButton;
    }

    public Button getDisconnectButton() {
        return disconnectButton;
    }

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public Button getStartStopButton() {
        return startStopButton;
    }

    public Button getNextButton() {
        return nextButton;
    }

    public TextField getCommandEntryField() {
        return commandEntryField;
    }

    public TextArea getConsoleTextArea() {
        return consoleTextArea;
    }

    public ChoiceBox<String> getMissionModeChoice() {
        return missionModeChoice;
    }

    public MenuItem getExitItem() {
        return exitItem;
    }

    public MenuItem getAboutItem() {
        return aboutItem;
    }
}
