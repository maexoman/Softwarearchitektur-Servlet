package de.hsw.examples.surflets;

import de.hsw.Surflet;
import de.hsw.http.HttpRequest;
import de.hsw.http.HttpResponse;

public class EventSourceSurflet implements Surflet {

    @Override
    public void handleRequest(HttpRequest request, HttpResponse response) throws Exception {
        response.status(200);
        response.addHeader ("Content-Type", "text/event-stream");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Content-Length", "chunked");
        response.sendStatusLineAndHeaders ();

        for (int counter = 1; counter <= 100; counter += 1) {
            response.send("data: {\"counter\":" + Integer.toString(counter) + "}\n\n");
            Thread.sleep(1000);
        }
        return;
    }

}
