package de.hsw.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {

    private ServerSocket socket;
    private int port;
    private boolean isRunning = true;

    public Server () {
        this (8080);
    }

    /**
     * Hiermit wird der neue Server erstellt und automatisch das ServerSocket erstellt.
     * @param port
     */
    public Server (int port) {
        this.port = port;
    }

    /**
     * Hiermit kann ein Server gestartet werden.
     */
    public void start () {

        boolean serverSocketActive = false;

        // Probiere zunächst das ServerSocket zu erstellen:
        try {
            this.socket = new ServerSocket (this.port);
            serverSocketActive = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Prüfe, ob das ServerSocket erfolgreich gestartet wurde
        if (serverSocketActive == false) {
            System.out.println("[ERROR]: something went wrong opening a socket.");
            return;
        }

        System.out.println("[INFORMATION]: server listening on port " + this.port);

        while (this.isRunning) {
            try {
                // Warte bis ein Client sich verbindet und shiebe ihn dann in einen neuen Thread.
                Socket client = this.socket.accept();
                new Thread (
                        new ThreadedHttpRequestHandler(client)
                ).start ();
            } catch (IOException e) {
                System.out.println("[ERROR]: some client tried connecting but caused the following error:");
                e.printStackTrace();
            }
        }

        // Versuche den Server Socket zu schließen:
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        this.start();
    }
}
