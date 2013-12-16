package com.dgregory.il2ssd.presentation.main;

import com.dgregory.il2ssd.business.config.Config;
import com.dgregory.il2ssd.business.icons.AwesomeIcons;
import com.dgregory.il2ssd.business.server.Command;
import com.dgregory.il2ssd.business.server.Connection;
import com.dgregory.il2ssd.business.server.ConsoleService;
import com.dgregory.il2ssd.business.server.Mission;
import com.dgregory.il2ssd.business.text.Parser;
import com.dgregory.il2ssd.business.text.ParserService;
import com.dgregory.il2ssd.presentation.config.server.ServerConfigPresenter;
import com.dgregory.il2ssd.presentation.config.server.ServerConfigView;
import com.dgregory.il2ssd.presentation.console.ConsolePresenter;
import com.dgregory.il2ssd.presentation.console.ConsoleView;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.apache.commons.exec.*;

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
    Tab serverConfigTab;
    @FXML
    Pane serverConfigPane;
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
    ServerConfigPresenter serverConfigPresenter;
    @Inject
    ParserService parserService;
    @Inject
    ConsoleService consoleService;
    @Inject
    ConsoleView consoleView;
    @Inject
    ServerConfigView serverConfigView;
    @Inject
    Connection connection;
    @Inject
    Command command;
    BooleanBinding serverTasksRunning;
    BooleanBinding readyToLoad;
    BooleanBinding readyForDCG;
    BooleanProperty connectDisconnecting = new SimpleBooleanProperty();
    BooleanProperty loadUnloading = new SimpleBooleanProperty();
    BooleanProperty missionGenerating = new SimpleBooleanProperty();

    @Override
    @Inject
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.consolePresenter = (ConsolePresenter) consoleView.getPresenter();
        this.serverConfigPresenter = (ServerConfigPresenter) serverConfigView.getPresenter();
        consolePane.getChildren().add(consoleView.getView());
        serverConfigPane.getChildren().add(serverConfigView.getView());
        consoleService.setConsolePresenter(consolePresenter);

        consolePresenter.enableConnected(false);

        serverTasksRunning = connectDisconnecting.or(loadUnloading.or(missionGenerating));
        readyToLoad = serverConfigPresenter.missionConfigured.and(connection.connectedProperty().and(loadUnloading.not()));
        readyForDCG = serverConfigPresenter.dcgConfigured.and(readyToLoad);

        connection.connectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    enableConnected(true);
                } else {
                    enableConnected(false);
                }
            }
        });

        readyToLoad.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    enableLoad(true);
                } else enableLoad(false);
            }
        });

        readyForDCG.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    enableNext(true);
                } else {
                    enableNext(false);
                }
            }
        });

        Mission.missionRunningProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                loadUnloading.set(false);
                if (newValue) {
                    enableRunning(true);
                } else {
                    enableRunning(false);
                }
            }
        });

        serverTasksRunning.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    progressIndicator.setVisible(true);
                } else {
                    progressIndicator.setVisible(false);
                }
            }
        });

    }

    public ConsolePresenter getConsolePresenter() {
        return consolePresenter;
    }

    public ServerConfigPresenter getServerConfigPresenter() {
        return serverConfigPresenter;
    }

    public void exitItemAction() {
        if (connection.getConnected()) {
            disconnectButtonAction();
        }
        serverConfigPresenter.updateConfig();
        Platform.exit();
    }

    @Inject
    public void connectButtonAction() {
        connectDisconnecting.set(true);
        connection.connect();
        if (connection.getConnected()) {
            initRunning();
            parserService.reset();
            parserService.restart();
            consoleService.reset();
            consoleService.restart();
            command.sendCommand("server");
        } else connectDisconnecting.set(false);
    }

    @Inject
    public void disconnectButtonAction() {

        if (connection.getConnected()) {
            connectDisconnecting.set(true);
            parserService.cancel();
            parserService.reset();
            consoleService.cancel();
            consoleService.reset();
            connection.disconnect();
            if (connection.getConnected()) {
                connectDisconnecting.set(false);
            }
        }

    }

    @Inject
    public void startStopButtonAction() {
        loadUnloading.set(true);
        startStopButton.setDisable(true);

        if (Mission.getMissionRunning()) {
            command.endMission();
            command.askMission();
        } else {

            if (Config.getRemoteMode()) {
                command.loadMission(Config.getRemotePath());
            }

            if (Config.getDcgMode()) {

                if (serverConfigPresenter.getDcgMission().equals("")) {
                    generateDcgMission();
                    startStopButtonAction();
                } else {
                    command.loadMission(serverConfigPresenter.getDcgMission());
                }
            }

            if (!Config.getDcgMode() && !Config.getRemoteMode()) {
                command.loadMission(serverConfigPresenter.resolveMissionPath());
            }
        }
    }

    public void nextButtonAction() {
        generateDcgMission();
        if (Mission.getMissionRunning()) {
            command.endMission();
            command.askMission();
        }
        while (Mission.getMissionRunning()) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Wait interrupted.");
            }
        }
        startStopButtonAction();
    }

    @Inject
    public void enableConnected(Boolean enable) {
        connectDisconnecting.set(false);
        if (enable) {
            connectButton.setDisable(true);
            disconnectButton.setDisable(false);
            consolePresenter.enableConnected(true);
        } else {
            connectButton.setDisable(false);
            disconnectButton.setDisable(true);
            consolePresenter.enableConnected(false);
        }
    }

    public void enableRunning(Boolean enable) {
        loadUnloading.set(false);
        if (enable) {
            startStopButton.setText(AwesomeIcons.ICON_STOP + " Stop");
        } else {
            startStopButton.setText(AwesomeIcons.ICON_PLAY + " Start");
        }
    }

    public void enableNext(Boolean enable) {
        if (enable) {
            nextButton.setDisable(false);
        } else {
            nextButton.setDisable(true);
        }
    }

    public void enableLoad(Boolean enable) {
        if (enable) {
            startStopButton.setDisable(false);
        } else {
            startStopButton.setDisable(true);
        }
    }

    @Inject
    public void initRunning() {
        String loadedMessage = "";
        command.askMission();
        try {
            loadedMessage = connection.getInput().readLine();
        } catch (IOException e) {
            System.out.println("Couldn't read status message.");
        }
        Parser.parseMissionLine(loadedMessage);
    }

    public DefaultExecuteResultHandler execDcgCommand(String dcgPath) {

        CommandLine dcgCommand = new CommandLine(dcgPath);
        dcgCommand.addArgument("/netdogfight");
        Executor executor = new DefaultExecutor();
        ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        executor.setWatchdog(watchdog);

        try {
            executor.execute(dcgCommand, resultHandler);
        } catch (IOException e) {
            e.printStackTrace();
            loadUnloading.set(false);
        }

        return resultHandler;

    }

    public void generateDcgMission() {
        DefaultExecuteResultHandler generated;
        loadUnloading.set(true);
        generated = execDcgCommand(Config.getDcgPath());
        try {
            generated.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            loadUnloading.set(false);
        }
    }
}
