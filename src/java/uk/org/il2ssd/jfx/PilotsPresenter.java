package uk.org.il2ssd.jfx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * Instantiation from FXML
 */
public class PilotsPresenter {
    @FXML
    BorderPane pilotsPane;
    @FXML
    TableView<Pilot> pilotsTable;
    @FXML
    TableColumn<Pilot, String> pilotSocketColumn;
    @FXML
    TableColumn<Pilot, String> pilotNameColumn;
    @FXML
    TableColumn<Pilot, String> pilotScoreColumn;
    @FXML
    TableColumn<Pilot, String> pilotTeamColumn;
    @FXML
    Button kickButton;
    @FXML
    Button banButton;
    @FXML
    Button ipBanButton;
    @FXML
    TextField chatField;
    @FXML
    Button sendChatButton;

    public BorderPane getPilotsPane() {
        return pilotsPane;
    }

    public TableView<Pilot> getPilotsTable() {
        return pilotsTable;
    }

    public TableColumn<Pilot, String> getPilotSocketColumn() {
        return pilotSocketColumn;
    }

    public TableColumn<Pilot, String> getPilotNameColumn() {
        return pilotNameColumn;
    }

    public TableColumn<Pilot, String> getPilotScoreColumn() {
        return pilotScoreColumn;
    }

    public TableColumn<Pilot, String> getPilotTeamColumn() {
        return pilotTeamColumn;
    }

    public Button getKickButton() {
        return kickButton;
    }

    public Button getBanButton() {
        return banButton;
    }

    public Button getIpBanButton() {
        return ipBanButton;
    }

    public TextField getChatField() {
        return chatField;
    }

    public Button getSendChatButton() {
        return sendChatButton;
    }

}