package uk.org.il2ssd;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import java.util.Collection;

/**
 * Instantiation from FXML
 */
public class CyclePresenter {
    @FXML
    BorderPane cycleMisPane;
    @FXML
    TableView<CycleMission> cycleMissionTable;
    @FXML
    TableColumn<CycleMission, Integer> cycleIndexColumn;
    @FXML
    TableColumn<CycleMission, String> cycleMissionColumn;
    @FXML
    TableColumn<CycleMission, String> cycleTimerColumn;
    @FXML
    Button missionUpButton;
    @FXML
    Button missionDeleteButton;
    @FXML
    Button missionDownButton;
    @FXML
    TextField cycleMisPathField;
    @FXML
    Button addMissionButton;
    @FXML
    Button chooseCycleMisButton;

    public TableColumn<CycleMission, Integer> getCycleIndexColumn() {
        return cycleIndexColumn;
    }

    public TableView<CycleMission> getCycleMissionTable() {
        return cycleMissionTable;
    }

    public TableColumn<CycleMission, String> getCycleMissionColumn() {
        return cycleMissionColumn;
    }

    public TableColumn<CycleMission, String> getCycleTimerColumn() {
        return cycleTimerColumn;
    }

    public Button getMissionUpButton() {
        return missionUpButton;
    }

    public Button getMissionDeleteButton() {
        return missionDeleteButton;
    }

    public Button getMissionDownButton() {
        return missionDownButton;
    }

    public TextField getCycleMisPathField() {
        return cycleMisPathField;
    }

    public Button getAddMissionButton() {
        return addMissionButton;
    }

    public Button getChooseCycleMisButton() {
        return chooseCycleMisButton;
    }

    public BorderPane getCycleMisPane() {
        return cycleMisPane;
    }

}
