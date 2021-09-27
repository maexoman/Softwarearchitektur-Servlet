package de.hsw.examples.surflets;

import de.hsw.Surflet;
import de.hsw.http.HttpRequest;
import de.hsw.http.HttpResponse;
import de.hsw.http.helper.HttpRequestBody;
import de.hsw.sessions.Session;
import de.hsw.sessions.SessionManager;

import java.util.List;
import java.util.stream.Collectors;

public class GetTasks implements Surflet {
    @Override
    public void handleRequest(HttpRequest request, HttpResponse response) throws Exception {

        Session session = SessionManager.getInstance().loadOrCreateSession(request, response);
        if (session.isUnsafe()) {
            response.status(401).setContentType(HttpRequestBody.ContentType.APPLICATION_JSON).append("{\"success\": false, \"message\": \"Unauthorized\"}").send();
            return;
        }

        response.status(201).setContentType(HttpRequestBody.ContentType.APPLICATION_JSON);

        List<TodoItem> items = session.get ("todos");

        response.append("{\"success\": true, \"message\": \"Created\", \"tasks\": [");

        response.append (items.stream().map(item -> "{\"id\": \"" + item.getId()  + "\", \"task\": \"" + item.getTask() + "\", \"completed\": " + (item.isCompleted() ? "true" : "false") + "}").collect(Collectors.joining(", ")));

        response.append("]}");
        response.send();
    }
}
