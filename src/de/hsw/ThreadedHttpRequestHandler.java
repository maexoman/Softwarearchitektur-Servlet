package de.hsw;

import java.io.*;
import java.net.Socket;

public class ThreadedHttpRequestHandler implements Runnable {

    private SurfletMapper mapper;
    private Socket socket;

    public ThreadedHttpRequestHandler (Socket clientSocket, SurfletMapper mapper) {
        this.socket = clientSocket;
        this.mapper = mapper;
    }

    private String getClientAddress () {
        return String.format("%s:%d", this.socket.getInetAddress().getHostAddress(), this.socket.getPort());
    }

    @Override
    public void run() {

        System.out.println("[INFORMATION]: client connected at " + this.getClientAddress ());

        while (this.socket.isConnected() && this.socket.isClosed() == false) {
            try {

                HttpRequest request = new HttpRequest(this.socket.getInputStream());
                HttpResponse response = new HttpResponse(this.socket.getOutputStream());

                Surflet surflet = this.mapper.resolveSurflet(request.getMethod (), request.getPath());
                surflet.handleRequest(request, response);

                if (request.getHeader ("connection", "keep-alive").equalsIgnoreCase ("close")) {
                    break;
                }

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }

        System.out.println("[INFORMATION]: connection closed for client " + this.getClientAddress ());
        this.destroy();



    }

    private void destroy () {
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
