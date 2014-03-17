package uk.org.il2ssd.jfx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * UI update functions and instantiation from fxml
 */
public class MainPresenter {

    @FXML
    Button connectButton;
    @FXML
    Button disconnectButton;
    @FXML
    StackPane progressStack;
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
    BorderPane missionPane;
    @FXML
    ChoiceBox<String> missionModeChoice;
    @FXML
    Region missionBarSpring;
    @FXML
    Button missionLoadButton;
    @FXML
    TextField ipAddressField;
    @FXML
    TextField portField;
    @FXML
    Label serverPathLabel;
    @FXML
    Button serverPathButton;
    @FXML
    Button getDifficultyButton;
    @FXML
    Button setDifficultyButton;
    @FXML
    TableView<DifficultySetting> difficultyTable;
    @FXML
    TableColumn<DifficultySetting, String> diffSettingColumn;
    @FXML
    TableColumn<DifficultySetting, String> diffValueColumn;
    @FXML
    MenuItem exitItem;
    @FXML
    MenuItem aboutItem;

    public BorderPane getMissionPane() {
        return missionPane;
    }

    public Label getServerPathLabel() {
        return serverPathLabel;
    }

    public Button getServerPathButton() {
        return serverPathButton;
    }

    public Button getMissionLoadButton() {
        return missionLoadButton;
    }

    public StackPane getProgressStack() {
        return progressStack;
    }

    public Region getMissionBarSpring() {
        return missionBarSpring;
    }

    public TableColumn getDiffSettingColumn() {
        return diffSettingColumn;
    }

    public TableColumn getDiffValueColumn() {
        return diffValueColumn;
    }

    public TableView<DifficultySetting> getDifficultyTable() {
        return difficultyTable;
    }

    public Button getGetDifficultyButton() {
        return getDifficultyButton;
    }

    public Button getSetDifficultyButton() {
        return setDifficultyButton;
    }

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