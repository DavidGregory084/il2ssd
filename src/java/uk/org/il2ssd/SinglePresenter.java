package uk.org.il2ssd;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

/**
 * Instantiation from FXML
 */
public class SinglePresenter {
    @FXML
    BorderPane singleMisPane;
    @FXML
    RadioButton localRadioButton;
    @FXML
    RadioButton remoteRadioButton;
    @FXML
    ToggleGroup singleMisGroup;
    @FXML
    Button chooseSingleMisButton;
    @FXML
    TextField singleMisPathField;
    @FXML
    Label singleMisPathLabel;

    public ToggleGroup getSingleMisGroup() {
        return singleMisGroup;
    }

    public RadioButton getLocalRadioButton() {
        return localRadioButton;
    }

    public RadioButton getRemoteRadioButton() {
        return remoteRadioButton;
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
