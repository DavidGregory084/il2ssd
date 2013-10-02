package com.dgregory.il2ssd.presentation.main;

import com.dgregory.il2ssd.business.config.Config;
import com.dgregory.il2ssd.business.icons.AwesomeIcons;
import com.dgregory.il2ssd.business.server.Command;
import com.dgregory.il2ssd.business.server.Connection;
import com.dgregory.il2ssd.business.server.ConsoleService;
import com.dgregory.il2ssd.business.server.Mission;
import com.dgregory.il2ssd.business.text.Parser;
import com.dgregory.il2ssd.presentation.config.main.MainConfigPresenter;
import com.dgregory.il2ssd.presentation.config.main.MainConfigView;
import com.dgregory.il2ssd.presentation.console.ConsolePresenter;
import com.dgregory.il2ssd.presentation.console.ConsoleView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.io.IOException;
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
        consoleService.setConsolePresenter(consolePresenter);

        this.enableConnected(false);
        consolePresenter.enableConnected(false);

        connection.connectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    enableConnected(true);
                    consolePresenter.enableConnected(true);
                } else {
                    enableConnected(false);
                    consolePresenter.enableConnected(false);
                }
            }
        });

        Mission.missionRunningProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    enableRunning(true);
                } else {
                    enableRunning(false);
                }
            }
        });


    }

    public ConsolePresenter getConsolePresenter() {
        return consolePresenter;
    }

    public MainConfigPresenter getMainConfigPresenter() {
        return mainConfigPresenter;
    }

    public void exitItemAction() {
        disconnectButtonAction();
        mainConfigPresenter.updateConfig();
        Platform.exit();
    }

    @Inject
    public void connectButtonAction() {
        connection.connect();
        initRunning();
        consoleService.reset();
        consoleService.restart();
        command.sendCommand("server");
    }

    @Inject
    public void disconnectButtonAction() {
        if (connection.getConnected()) {
            consoleService.cancel();
            consoleService.reset();
            connection.disconnect();
        }
    }

    @Inject
    public void startStopButtonAction() {
        if (Mission.getMissionRunning()) {
            command.endMission();
            command.askMission();
        } else {
            if (Config.getRemoteMode()) {
                command.loadMission(Config.getRemotePath());
            } else {
                command.loadMission(mainConfigPresenter.resolveMissionPath());
            }
        }


    }

    public void nextButtonAction() {
        startStopButtonAction();
    }

    public void enableConnected(Boolean enable) {
        if (enable) {
            connectButton.setDisable(true);
            disconnectButton.setDisable(false);
            startStopButton.setDisable(false);
        } else {
            connectButton.setDisable(false);
            disconnectButton.setDisable(true);
            startStopButton.setDisable(true);
        }
    }

    public void enableRunning(Boolean enable) {
        if (enable) {
            startStopButton.setText(AwesomeIcons.ICON_STOP + " Stop");
        } else {
            startStopButton.setText(AwesomeIcons.ICON_PLAY + " Start");

        }

    }

    public void initRunning() {
        String loadedMessage = "";
        command.askMission();
        try {
            loadedMessage = connection.getInput().readLine();
        } catch (IOException e) {
            System.out.println("Couldn't read status message.");
        }
        if (Parser.getLoaded(loadedMessage).equals("load")) {
            Mission.setMissionRunning(true);
        }
    }
}
