package com.dgregory.il2ssd;

import com.airhacks.afterburner.injection.InjectionProvider;
import com.dgregory.il2ssd.presentation.main.MainPresenter;
import com.dgregory.il2ssd.presentation.main.MainView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * 23/09/13 00:44
 * il2ssd
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        final MainView appView = new MainView();
        Scene scene = new Scene(appView.getView());
        stage.setTitle("IL-2 Simple Server Daemon");
        stage.setResizable(false);
        Font.loadFont(App.class.getResource("fonts/fontawesome-webfont.ttf").toExternalForm(), 12);
        final String uri = getClass().getResource("app.css").toExternalForm();
        scene.getStylesheets().add(uri);
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                windowEvent.consume();
                MainPresenter mainPresenter = (MainPresenter) appView.getPresenter();
                mainPresenter.disconnectButtonAction();
                mainPresenter.getMainConfigPresenter().updateConfig();
                Platform.exit();
            }
        });

    }

    @Override
    public void stop() throws Exception {
        InjectionProvider.forgetAll();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
