package de.hsw.server;

import de.hsw.server.configs.SurfletMapper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

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

        // Lese die Web.XML und hole daraus die Surflet implementationen.
        SurfletMapper surfletMapper = SurfletMapper.loadFromFile("web.xml");

        // Probiere zunächst das ServerSocket zu erstellen:
        try {
            this.socket = new ServerSocket(port);
            serverSocketActive = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Prüfe, ob das ServerSocket erfolgreich gestartet wurde
        if (serverSocketActive == false) { return; }

        System.out.println("[INFORMATION]: server listening on port 8080");

        while (this.isRunning) {
            try {
                // Warte bis ein Client sich verbindet und shiebe ihn dann in einen neuen Thread.
                Socket client = this.socket.accept();
                new Thread (
                        new ThreadedHttpRequestHandler(client, surfletMapper)
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
}
