package uk.org.il2ssd.jfx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * Instantiation from FXML
 */
public class DCGPresenter {
    @FXML
    BorderPane dcgMisPane;
    @FXML
    Label dcgMisPathLabel;
    @FXML
    Label dcgExePathLabel;
    @FXML
    Button dcgExePathSelectButton;

    public BorderPane getDcgMisPane() {
        return dcgMisPane;
    }

    public Button getDcgExePathSelectButton() {
        return dcgExePathSelectButton;
    }

    public Label getDcgMisPathLabel() {
        return dcgMisPathLabel;
    }

    public Label getDcgExePathLabel() {
        return dcgExePathLabel;
    }

}
