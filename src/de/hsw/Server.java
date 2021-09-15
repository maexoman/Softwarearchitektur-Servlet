package de.hsw;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket socket;
    private int port;
    private boolean isStartable = false;
    private boolean isRunning = true;

    public Server () {
        this (8080);
    }

    public Server (int port) {
        this.port = port;
        try {
            this.socket = new ServerSocket(port);
            this.isStartable = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isStartable() {
        return this.isStartable;
    }

    public void start () {
        if (this.isStartable() == false) {
            return;
        }

        SurfletMapper surfletMapper = SurfletMapper.loadFromFile("web.xml");

        // TODO: while "run" wäre wahrscheinlich hier sinnvoller. Dann kann irgendwo "false" gesetzt werden.
        while (this.isRunning) {
            try {
                Socket client = this.socket.accept();
                new Thread(new ThreadedHttpRequestHandler (client, surfletMapper)).start ();
            } catch (IOException e) {
                System.out.println("[ERROR]: some client tried connecting but caused the following error:");
                e.printStackTrace();
            }
        }

        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
