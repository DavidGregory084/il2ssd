package com.dgregory.il2ssd.presentation.config.main;

import com.dgregory.il2ssd.business.config.Config;
import com.dgregory.il2ssd.business.icons.AwesomeIcons;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

/**
 * 23/09/13 01:11
 * il2ssd
 */
public class MainConfigPresenter implements Initializable {

    @FXML
    TextField ipAddressField;
    @FXML
    TextField portField;
    @FXML
    Button serverPathButton;
    @FXML
    Label serverPathLabel;
    @FXML
    Button missionPathButton;
    @FXML
    Label missionPathLabel;
    @FXML
    Button dcgPathButton;
    @FXML
    Label dcgPathLabel;
    @FXML
    ToggleButton remoteModeToggle;
    @FXML
    TextField remotePathText;
    @FXML
    Label remotePathLabel;
    @FXML
    Label remotePathDesc;
    @FXML
    Label dcgSettingsLabel;
    @FXML
    Separator dcgSettingsSeparator;
    @FXML
    ToggleButton dcgToggle;
    @FXML
    Label dcgModeLabel;

    FileChooser serverChooser = new FileChooser();
    FileChooser missionChooser = new FileChooser();
    FileChooser dcgChooser = new FileChooser();

    public void initialize(URL url, ResourceBundle resourceBundle) {

        Config.loadConfiguration();
        initControls();
        initServerChooser();
        initMissionChooser();
        initDcgChooser();

        ipAddressField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (!newValue) {
                    updateConfig();
                }
            }
        });

        portField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (!newValue) {
                    updateConfig();
                }
            }
        });

        serverPathLabel.textProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                updateConfig();
            }
        });

        missionPathLabel.textProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                updateConfig();
            }
        });

        dcgPathLabel.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                updateConfig();
                enableDcgControls(Config.getDcgMode());

            }
        });

        remoteModeToggle.selectedProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                updateConfig();
                enableRemoteControls(Config.getRemoteMode());
            }
        });

        remotePathText.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (!newValue) {
                    updateConfig();
                }
            }
        });

        dcgToggle.selectedProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                updateConfig();
            }
        });
    }

    public void initControls() {

        try {
            ipAddressField.setText(Config.getIpAddress());
            portField.setText(Config.getPort());
            remotePathText.setText(Config.getRemotePath());
            if (!Config.getServerPath().equals("")) {
                serverPathLabel.setText(Config.getServerPath());
            }
            if (!Config.getMissionPath().equals("")) {
                missionPathLabel.setText(Config.getMissionPath());
            }
            if (!Config.getDcgPath().equals("")) {
                dcgPathLabel.setText(Config.getDcgPath());
            }
            remoteModeToggle.setSelected(Config.getRemoteMode());
            enableRemoteControls(Config.getRemoteMode());
            enableDcgControls(Config.getDcgMode());
            dcgToggle.setSelected(Config.getDcgMode());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    public void initServerChooser() {
        serverChooser.setTitle("Choose IL-2 Server Executable");
        serverChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("IL-2 Server (il2server.exe)", "il2server.exe")
        );
    }

    public void initMissionChooser() {
        missionChooser.setTitle("Choose IL-2 Mission File");
        missionChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("IL-2 Mission (*.mis)", "*.mis")
        );
    }

    public void initDcgChooser() {
        dcgChooser.setTitle("Choose DCG Executable");
        dcgChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("DCG Executable (il2dcg.exe)", "il2dcg.exe")
        );
    }

    public void enableRemoteControls(Boolean enable) {
        if (enable) {
            remoteModeToggle.setText(AwesomeIcons.ICON_OK);
            serverPathButton.setDisable(true);
            missionPathButton.setDisable(true);
            remotePathLabel.setVisible(true);
            remotePathText.setVisible(true);
            remotePathDesc.setVisible(true);
        } else {
            remoteModeToggle.setText(AwesomeIcons.ICON_REMOVE);
            serverPathButton.setDisable(false);
            missionPathButton.setDisable(false);
            remotePathLabel.setVisible(false);
            remotePathText.setVisible(false);
            remotePathDesc.setVisible(false);
        }
    }

    public void enableDcgControls(Boolean enable) {
        if (enable) {
            dcgSettingsLabel.setVisible(true);
            dcgSettingsSeparator.setVisible(true);
            dcgToggle.setVisible(true);
            dcgModeLabel.setVisible(true);
        } else {
            dcgToggle.setSelected(false);
            updateConfig();
            dcgSettingsLabel.setVisible(false);
            dcgSettingsSeparator.setVisible(false);
            dcgToggle.setVisible(false);
            dcgModeLabel.setVisible(false);
        }
    }

    public void updateConfig() {
        Config.setIpAddress(ipAddressField.getText());
        Config.setPort(portField.getText());
        Config.setRemotePath(remotePathText.getText());
        Config.setServerPath(serverPathLabel.getText());
        Config.setMissionPath(missionPathLabel.getText());
        Config.setDcgPath(dcgPathLabel.getText());
        Config.setRemoteMode(remoteModeToggle.isSelected());
        Config.setDcgMode(dcgToggle.isSelected());
        Config.saveConfiguration();
    }

    public String resolveMissionPath() {
        Path serverPath;
        Path missionPath;
        Path missionsRoot;
        Path missionLoadPath;

        serverPath = Paths.get(Config.getServerPath());
        missionPath = Paths.get(Config.getMissionPath());

        missionsRoot = serverPath.getParent().resolve("Missions");
        missionLoadPath = missionPath.relativize(missionsRoot).normalize();
        System.out.println(missionLoadPath.toString());
        return missionLoadPath.toString();
    }

    public void serverPathButtonAction() {
        String serverPath;
        File file = serverChooser.showOpenDialog(new Stage());
        if (file != null) {
            serverPath = file.toString();
            Config.setServerPath(serverPath);
            serverPathLabel.setText(serverPath);
            missionPathButton.setDisable(false);
        } else {
            serverPathLabel.setText("<no server .exe selected>");
            missionPathButton.setDisable(true);
        }

    }

    public void missionPathButtonAction() {
        String missionPath;
        File file = missionChooser.showOpenDialog(new Stage());
        if (file != null) {
            missionPath = file.toString();
            Config.setMissionPath(missionPath);
            missionPathLabel.setText(missionPath);
        } else {
            missionPathLabel.setText("<no .mis file selected>");
        }

    }

    public void dcgPathButtonAction() {
        String dcgPath;
        File file = dcgChooser.showOpenDialog(new Stage());
        if (file != null) {
            dcgPath = file.toString();
            Config.setDcgPath(dcgPath);
            dcgPathLabel.setText(dcgPath);
        } else {
            dcgPathLabel.setText("<no dcg .exe selected>");
        }

    }

}
