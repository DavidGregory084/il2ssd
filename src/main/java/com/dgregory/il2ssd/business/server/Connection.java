package com.dgregory.il2ssd.business.server;


import com.dgregory.il2ssd.business.config.Config;

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
    private Boolean connected = false;

    public void connect() {

        try {
            socket = new Socket(Config.getIpAddress(), Integer.decode(Config.getPort()));
            output = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
            connected = true;
            if (socket.isConnected()) {
                System.out.println("Connected to server " + Config.getIpAddress() + " on port " + Config.getPort());
            }
        } catch (IOException e) {
            connected = false;
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
            connected = false;
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
        return connected;
    }
}
