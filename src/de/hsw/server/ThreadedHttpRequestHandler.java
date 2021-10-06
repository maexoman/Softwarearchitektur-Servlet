package de.hsw.server;

import de.hsw.errors.ConnectionClosedException;
import de.hsw.errors.InvalidHttpRequestException;
import de.hsw.http.HttpRequest;
import de.hsw.http.HttpResponse;
import de.hsw.Surflet;
import de.hsw.server.configs.SurfletMapper;

import java.io.*;
import java.net.Socket;

public class ThreadedHttpRequestHandler implements Runnable {

    private Socket socket;
    private String clientAddress;

    /**
     * Hiermit wird ein Client-Handler für einen Thread erstellt.
     * @param clientSocket
     */
    public ThreadedHttpRequestHandler (Socket clientSocket) {
        this.socket = clientSocket;
        this.clientAddress = String.format (
            "%s:%d",
            this.socket.getInetAddress ().getHostAddress (),
            this.socket.getPort()
        );
    }

    /**
     * Hier wird die Adresse des Clients im <ipv4/6>:<port> Format zurückgegeben.
     * @return Die Adresse des Clients im <ipv4/6>:<port> Format.
     */
    private String getClientAddress () {
        return this.clientAddress;
    }

    /**
     * Hiermit kann geprüft werdne, ob die HTTP-Anfrage den Header "Connection" auf "close" gesetzt hat.
     * @param request Die Http-Anfrage.
     * @return "True", wenn der Client "close" enthält. Andernfalls "False".
     */
    private boolean isConnectionHeaderClose (HttpRequest request) {
        return request.getHeader("connection", "keep-alive").equalsIgnoreCase ("close");
    }

    /**
     * Hier ist die Funktion, die vom Thread aufgerufen wird.
     */
    @Override
    public void run() {

        System.out.println("[INFORMATION]: client connected at " + this.getClientAddress ());

        // Solange der Client verbunden ist, kann ein HTTP-Request gelesen werden.
        // Da in HTTP/1.1 der Client mehrere Anfragen lesen kann,
        // werden hier die Anfragen nach und nach gelesen und abgearbeitet.
        while (this.socket.isConnected() && this.socket.isClosed() == false) {
            try {

                // Versuche den Http-Request zu parsen, und erstelle die Http-Antwort:
                HttpRequest request = new HttpRequest(this.socket.getInputStream());
                HttpResponse response = new HttpResponse(this.socket.getOutputStream());

                // Hole das Surflet aus dem Mapper und rufe dies mit dem Request und der Antwort auf:
                Surflet surflet = SurfletMapper.getInstance ().resolveSurflet(request.getMethod (), request.getPath());
                surflet.handleRequest(request, response);

                // Wenn in dem Request der Header "connection" enthalten ist, und dort "close" drin steht.
                // Dann soll das Socket nach der Bearbeitung geschlossen werden:
                if ( this.isConnectionHeaderClose (request) == true) {
                    break;
                }

            } catch (ConnectionClosedException | InvalidHttpRequestException e) {
                break;
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }

        System.out.println("[INFORMATION]: connection closed for client " + this.getClientAddress ());
        this.destroy();

    }

    /**
     * Hiermit kann versucht werden das Socket zu schließen.
     */
    private void destroy () {
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
