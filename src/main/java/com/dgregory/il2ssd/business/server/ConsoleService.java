package com.dgregory.il2ssd.business.server;

import com.dgregory.il2ssd.business.text.Parser;
import com.dgregory.il2ssd.presentation.console.ConsolePresenter;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 23/09/13 20:37
 * il2ssd
 */
public class ConsoleService extends Service<Void> {

    @Inject
    Connection connection;
    ObjectProperty<ConsolePresenter> consolePresenterObjectProperty = new SimpleObjectProperty<>();

    public void setConsolePresenterObjectProperty(ConsolePresenter consolePresenter) {
        consolePresenterObjectProperty.set(consolePresenter);
    }

    @Inject
    public Task<Void> createTask() {

        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (connection.getConnected()) {
                    if (isCancelled()) {
                        break;
                    }
                    try {
                        String text = connection.getInput().readLine();
                        List<String> lines = Arrays.asList(text.split("\\n"));
                        for (final String item : lines) {
                            if (item != null && !item.startsWith("<consoleN>"))
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        consolePresenterObjectProperty.get().outputText(Parser.cleanText(item));
                                    }
                                });
                        }
                    } catch (IOException e) {
                        System.out.println("Couldn't read line from socket.");
                    }
                }
                return null;
            }
        };

    }

}
