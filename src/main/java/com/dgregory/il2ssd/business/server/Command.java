package com.dgregory.il2ssd.business.server;

import javax.inject.Inject;

/**
 * 23/09/13 20:50
 * il2ssd
 */
public class Command {

    @Inject
    Connection connection;

    @Inject
    public void sendCommand(String line) {
        connection.getOutput().println(line);
        connection.getOutput().flush();
        if (connection.getOutput().checkError()) {
            System.out.println("Not sent.");
        }
    }

    public void loadMission(String mission) {
        sendCommand("mission LOAD " + mission);
    }

    public void endMission() {
        sendCommand("mission DESTROY");
    }

    public void askMission() {
        sendCommand("mission");
    }

}
