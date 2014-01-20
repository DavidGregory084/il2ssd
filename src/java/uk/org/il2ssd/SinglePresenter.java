package uk.org.il2ssd;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * Instantiation from FXML
 */
public class SinglePresenter {
    @FXML
    BorderPane singleMisPane;
    @FXML
    Button chooseSingleMisButton;
    @FXML
    TextField singleMisPathField;
    @FXML
    Button remoteSelectButton;
    @FXML
    Label singleMisPathLabel;

    public Button getRemoteSelectButton() {
        return remoteSelectButton;
    }

    public Button getChooseSingleMisButton() {
        return chooseSingleMisButton;
    }

    public TextField getSingleMisPathField() {
        return singleMisPathField;
    }

    public Label getSingleMisPathLabel() {
        return singleMisPathLabel;
    }

    public BorderPane getSingleMisPane() {
        return singleMisPane;
    }

}
