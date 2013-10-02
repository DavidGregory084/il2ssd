package com.dgregory.il2ssd.business.text;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * 02/10/13 22:32
 * il2ssd
 */
public class ParserService extends Service<Void> {

    Boolean cancelled;

    public Task<Void> createTask() {

        cancelled = false;

        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (!cancelled) {
                    final String parseLine = Parser.pollParseLine();
                    if (parseLine != null) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Parser.parseLine(parseLine);
                            }
                        });
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
