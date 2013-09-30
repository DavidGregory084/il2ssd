package com.dgregory.il2ssd.presentation.main;

import com.dgregory.il2ssd.business.config.Config;
import com.dgregory.il2ssd.business.server.Command;
import com.dgregory.il2ssd.business.server.Connection;
import com.dgregory.il2ssd.business.server.ConsoleService;
import com.dgregory.il2ssd.presentation.config.main.MainConfigPresenter;
import com.dgregory.il2ssd.presentation.config.main.MainConfigView;
import com.dgregory.il2ssd.presentation.console.ConsolePresenter;
import com.dgregory.il2ssd.presentation.console.ConsoleView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * 23/09/13 01:11
 * il2ssd
 */
public class MainPresenter implements Initializable {

    @FXML
    BorderPane mainPane;
    @FXML
    Menu fileMenu;
    @FXML
    MenuItem exitItem;
    @FXML
    Menu helpMenu;
    @FXML
    MenuItem aboutItem;
    @FXML
    TabPane tabPane;
    @FXML
    Tab consoleTab;
    @FXML
    Pane consolePane;
    @FXML
    Tab configTab;
    @FXML
    Pane configPane;
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
    @Inject
    ConsolePresenter consolePresenter;
    @Inject
    MainConfigPresenter mainConfigPresenter;
    @Inject
    ConsoleService consoleService;
    @Inject
    ConsoleView consoleView;
    @Inject
    MainConfigView mainConfigView;
    @Inject
    Connection connection;
    @Inject
    Command command;

    @Override
    @Inject
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.consolePresenter = (ConsolePresenter) consoleView.getPresenter();
        this.mainConfigPresenter = (MainConfigPresenter) mainConfigView.getPresenter();
        consolePane.getChildren().add(consoleView.getView());
        configPane.getChildren().add(mainConfigView.getView());
        this.enableControl(false);
    }

    public ConsolePresenter getConsolePresenter() {
        return consolePresenter;
    }

    public MainConfigPresenter getMainConfigPresenter() {
        return mainConfigPresenter;
    }

    public void exitItemAction() {
        mainConfigPresenter.updateConfig();
        Platform.exit();
    }

    @Inject
    public void connectButtonAction() {
        if (!connection.getConnected()) {
            mainConfigPresenter.updateConfig();
            connection.connect();
            if (connection.getConnected()) {
                this.enableControl(true);
                consolePresenter.enableControl(true);
                consoleService.setConsolePresenterObjectProperty(consolePresenter);
                consoleService.start();
                command.sendCommand("server");
            }
        }

    }

    @Inject
    public void disconnectButtonAction() {
        if (connection.getConnected()) {
            consoleService.cancel();
            consoleService.reset();
            connection.disconnect();
            if (!connection.getConnected()) {
                this.enableControl(false);
                consolePresenter.enableControl(false);
                consoleService.reset();
            }
        }

    }

    @Inject
    public void startStopButtonAction() {
        mainConfigPresenter.updateConfig();
        if (Config.getRemoteMode()) {
            command.loadMission(Config.getRemotePath());
        } else {
            command.loadMission(mainConfigPresenter.resolveMissionPath());
        }

    }

    public void nextButtonAction() {
        startStopButtonAction();
    }

    public void enableControl(Boolean enable) {
        if (enable) {
            connectButton.disableProperty().set(true);
            disconnectButton.disableProperty().set(false);
            startStopButton.disableProperty().set(false);
            nextButton.disableProperty().set(false);
        } else {
            connectButton.disableProperty().set(false);
            disconnectButton.disableProperty().set(true);
            startStopButton.disableProperty().set(true);
            nextButton.disableProperty().set(true);
        }
    }
}
