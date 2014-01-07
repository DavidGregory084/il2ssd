package uk.org.il2ssd;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Instantiation from FXML
 */
public class CyclePresenter implements Initializable {
    @FXML
    BorderPane cycleMisPane;
    @FXML
    TableView<CycleMission> cycleMissionTable;
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
    ObservableList<CycleMission> cycleData = FXCollections.observableArrayList();

    public TableView<CycleMission> getCycleMissionTable() {
        return cycleMissionTable;
    }

    public TableColumn<CycleMission, String> getCycleMissionColumn() {
        return cycleMissionColumn;
    }

    public TableColumn<CycleMission, String> getCycleTimerColumn() {
        return cycleTimerColumn;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cycleMissionColumn.setCellValueFactory(
                new PropertyValueFactory<CycleMission, String>("mission")
        );

        cycleTimerColumn.setCellValueFactory(
                new PropertyValueFactory<CycleMission, String>("timer")
        );

        cycleTimerColumn.setCellFactory(TextFieldTableCell.<CycleMission>forTableColumn());
        cycleData = FXCollections.observableArrayList();
        cycleMissionTable.setItems(cycleData);
        cycleMissionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        cycleTimerColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<CycleMission, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<CycleMission, String> t) {
                try {
                    Integer test = Integer.decode(t.getNewValue());
                    t.getTableView().getItems().get(
                            t.getTablePosition().getRow()
                    ).setTimer(t.getNewValue());
                } catch (NumberFormatException e) {
                    t.getTableColumn().setVisible(false);
                    t.getTableColumn().setVisible(true);
                }
            }
        });
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

    public ObservableList<CycleMission> getCycleData() {
        return cycleData;
    }

    public BorderPane getCycleMisPane() {
        return cycleMisPane;
    }

}
