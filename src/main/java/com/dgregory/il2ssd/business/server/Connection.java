package com.dgregory.il2ssd.business.server;


import com.dgregory.il2ssd.business.config.Config;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * 23/09/13 20:46
 * il2ssd
 */
public class Connection {

    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;
    private BooleanProperty connected = new SimpleBooleanProperty();

    public void connect() {

        try {
            socket = new Socket(Config.getIpAddress(), Integer.decode(Config.getPort()));
            output = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
            setConnected(true);
            if (socket.isConnected()) {
                System.out.println("Connected to server " + Config.getIpAddress() + " on port " + Config.getPort());
            }
        } catch (IOException e) {
            setConnected(false);
            System.out.println("Failed to connect to socket.");
            e.printStackTrace();
        }

    }

    public void disconnect() {

        try {
            output.flush();
            output.close();
            input.close();
            socket.close();
            setConnected(false);
        } catch (IOException e) {
            System.out.println("Failed to close socket.");
            e.printStackTrace();
        }
    }

    public PrintWriter getOutput() {
        return output;
    }

    public BufferedReader getInput() {
        return input;
    }

    public Boolean getConnected() {
        return connected.get();
    }

    public void setConnected(Boolean connected) {
        this.connected.set(connected);
    }

    public BooleanProperty connectedProperty() {
        return connected;
    }
}
