package uk.org.il2ssd.jfx;

import javafx.animation.FadeTransition;
import javafx.application.Preloader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Preloader for Il-2 Simple Server Daemon
 */
public class Il2SsdPreloader extends Preloader {
    Scene scene;
    ProgressBar progressBar;
    StackPane topStack;
    Label title;
    StackPane centerStack;
    Stage stage;
    StackPane bottomStack;
    Label status;

    private Scene createScene() {
        topStack = new StackPane();
        title = new Label("Il-2 Simple Server Daemon");
        title.setFont(Font.font("System", FontWeight.BOLD, 15.0));
        topStack.getChildren().addAll(title);
        topStack.setPadding(new Insets(10.0));
        centerStack = new StackPane();
        progressBar = new ProgressBar();
        progressBar.setProgress(-1.0);
        centerStack.getChildren().addAll(progressBar);
        bottomStack = new StackPane();
        status = new Label("Initialising...");
        bottomStack.getChildren().addAll(status);
        bottomStack.setPadding(new Insets(10.0));
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topStack);
        borderPane.setCenter(centerStack);
        borderPane.setBottom(bottomStack);
        borderPane.getStyleClass().add("preloader-border");
        return new Scene(borderPane, 300, 150);
    }

    public void start(Stage stage) throws Exception {
        this.stage = stage;
        scene = createScene();
        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("preloader.css").toExternalForm());
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification event) {
        if (event.getType() == StateChangeNotification.Type.BEFORE_START) {
            EventHandler<ActionEvent> fadeHandler = new EventHandler<ActionEvent>() {
                public void handle(ActionEvent actionEvent) {
                    stage.hide();
                }
            };
            FadeTransition fadeTransition = new FadeTransition(
                    Duration.seconds(1.0), stage.getScene().getRoot()
            );
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
            fadeTransition.setOnFinished(fadeHandler);
            fadeTransition.play();
        }
    }
}
