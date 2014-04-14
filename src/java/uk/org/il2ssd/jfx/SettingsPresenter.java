package uk.org.il2ssd.jfx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

/**
 * Instantiation from FXML.
 */
public class SettingsPresenter {
    @FXML
    BorderPane settingsPane;
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

    public BorderPane getSettingsPane() {
        return settingsPane;
    }

    public Label getServerPathLabel() {
        return serverPathLabel;
    }

    public Button getServerPathButton() {
        return serverPathButton;
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
}
