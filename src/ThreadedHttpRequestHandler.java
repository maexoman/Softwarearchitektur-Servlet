import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ThreadedHttpRequestHandler implements Runnable {

    private static final Map<String, Surflet> MAPPER = new HashMap<>();
    {
        MAPPER.put("/", new IndexSuflet());
        MAPPER.put("/index.html", new IndexSuflet());
        MAPPER.put("/404.html", new Error404Surflet());
        MAPPER.put("/doSomething", new SomePostThingy());
        MAPPER.put("/sse", new EventSourceSurflet());
    }

    private Socket socket;

    public ThreadedHttpRequestHandler (Socket clientSocket) {
        this.socket = clientSocket;
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

                if (MAPPER.containsKey(request.getPath())) {
                    MAPPER.get(request.getPath()).handleRequest(request, response);
                } else {
                    MAPPER.get("/404.html").handleRequest(request, response);
                }

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
