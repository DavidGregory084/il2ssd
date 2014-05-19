package uk.org.il2ssd.jfx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

/**
 * Instantiation from FXML
 */
public class BansPresenter {
    @FXML
    BorderPane bansPane;
    @FXML
    TableView bansTable;
    @FXML
    TableColumn banTypeColumn;
    @FXML
    TableColumn banValueColumn;
    @FXML
    Button getBansButton;
    @FXML
    Button removeBanButton;
    @FXML
    Button clearBansButton;

    public BorderPane getBansPane() {
        return bansPane;
    }

    public TableView getBansTable() {
        return bansTable;
    }

    public TableColumn getBanTypeColumn() {
        return banTypeColumn;
    }

    public TableColumn getBanValueColumn() {
        return banValueColumn;
    }

    public Button getGetBansButton() {
        return getBansButton;
    }

    public Button getRemoveBanButton() {
        return removeBanButton;
    }

    public Button getClearBansButton() {
        return clearBansButton;
    }
}