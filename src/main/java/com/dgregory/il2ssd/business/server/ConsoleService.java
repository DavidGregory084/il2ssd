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
    Boolean cancelled;
    ObjectProperty<ConsolePresenter> consolePresenter = new SimpleObjectProperty<>();

    public void setConsolePresenter(ConsolePresenter consolePresenter) {
        this.consolePresenter.set(consolePresenter);
    }

    @Inject
    public Task<Void> createTask() {

        cancelled = false;

        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (connection.getConnected() && !cancelled) {
                    try {
                        String text = connection.getInput().readLine();
                        List<String> lines = Arrays.asList(text.split("\\n"));
                        for (String item : lines) {
                            final String outputLine = Parser.cleanText(item);
                            if (item != null && !item.startsWith("<consoleN>"))
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        consolePresenter.get().outputText(outputLine);
                                        Parser.addParseLine(outputLine);
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

    @Override
    protected void cancelled() {
        super.cancelled();
        cancelled = true;
    }

}
