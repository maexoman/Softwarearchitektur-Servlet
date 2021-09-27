package de.hsw.examples.surflets;

import de.hsw.Surflet;
import de.hsw.http.HttpRequest;
import de.hsw.http.HttpResponse;
import de.hsw.http.helper.HttpRequestBody;
import de.hsw.sessions.Session;
import de.hsw.sessions.SessionManager;

import java.util.List;

public class CreateTask implements Surflet {
    @Override
    public void handleRequest(HttpRequest request, HttpResponse response) throws Exception {

        HttpRequestBody data = request.getBody();

        // Für dieses Surflet ist nur ein String als Input erlaubt.
        // Dementsprechend wird alles andere abgewiesen.
        if (data.getContentType() != HttpRequestBody.ContentType.TEXT_PLAIN) {
            response.status(422).setContentType(HttpRequestBody.ContentType.APPLICATION_JSON).append("{\"success\": false, \"message\": \"ValidationError\"}").send();
            return;
        }

        Session session = SessionManager.getInstance().loadOrCreateSession(request, response);
        if (session.isUnsafe()) {
            response.status(401).setContentType(HttpRequestBody.ContentType.APPLICATION_JSON).append("{\"success\": false, \"message\": \"Unauthorized\"}").send();
            return;
        }

        List<TodoItem> items = session.get ("todos");
        TodoItem item = new TodoItem (data.asText ().trim ());
        items.add (item);

        response.status(201).setContentType(HttpRequestBody.ContentType.APPLICATION_JSON).append("{\"success\": true, \"message\": \"Created\", \"task\": {\"id\": \"" + item.getId()  + "\", \"task\": \"" + item.getTask() + "\", \"completed\": " + (item.isCompleted() ? "true" : "false") + "}}").send();

    }
}
