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
    ToolBar toolBar;
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
    Button cycleStartStopButton;
    @FXML
    Button dcgStartStopButton;
    @FXML
    Button cycleNextButton;
    @FXML
    Button dcgNextButton;
    @FXML
    Tab consoleTab;
    @FXML
    Tab settingsTab;
    @FXML
    BorderPane missionPane;
    @FXML
    ChoiceBox<String> missionModeChoice;
    @FXML
    Region missionBarSpring;
    @FXML
    Button missionLoadButton;
    @FXML
    MenuItem exitItem;
    @FXML
    MenuItem aboutItem;

    public Button getDcgNextButton() {
        return dcgNextButton;
    }

    public Button getDcgStartStopButton() {
        return dcgStartStopButton;
    }

    public ToolBar getToolBar() {
        return toolBar;
    }

    public Button getCycleStartStopButton() {
        return cycleStartStopButton;
    }

    public BorderPane getMissionPane() {
        return missionPane;
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

    public Button getCycleNextButton() {
        return cycleNextButton;
    }

    public Tab getConsoleTab() {
        return consoleTab;
    }

    public Tab getSettingsTab() {
        return settingsTab;
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
