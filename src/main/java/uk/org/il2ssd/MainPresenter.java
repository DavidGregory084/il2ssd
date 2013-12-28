package uk.org.il2ssd;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * UI update functions and instantiation from fxml
 */
public class MainPresenter implements Initializable {

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

    ObservableList<DifficultySetting> difficultyData = FXCollections.observableArrayList();

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

    public ObservableList<DifficultySetting> getDifficultyData() {
        return difficultyData;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set up the table data
        diffSettingColumn.setCellValueFactory(
                new PropertyValueFactory<DifficultySetting, String>("setting")
        );
        diffValueColumn.setCellValueFactory(
                new PropertyValueFactory<DifficultySetting, String>("value")
        );
        diffValueColumn.setCellFactory(TextFieldTableCell.<DifficultySetting>forTableColumn());
        diffValueColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<DifficultySetting, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<DifficultySetting, String> t) {
                if (t.getNewValue().equals("0") || t.getNewValue().equals("1")) {
                    difficultyData.get(t.getTablePosition().getRow()).setValue(t.getNewValue());
                } else {
                    t.consume();
                    difficultyTable.setItems(difficultyData);
                }
            }
        });
        difficultyData = FXCollections.observableArrayList();
        difficultyTable.setItems(difficultyData);
        difficultyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
