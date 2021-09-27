package de.hsw.examples.surflets;


import de.hsw.Surflet;
import de.hsw.http.HttpRequest;
import de.hsw.http.HttpResponse;
import de.hsw.http.helper.HttpRequestBody;
import de.hsw.sessions.Session;
import de.hsw.sessions.SessionManager;

import java.util.ArrayList;
import java.util.Map;

public class RegisterSurflet implements Surflet {
    @Override
    public void handleRequest(HttpRequest request, HttpResponse response) throws Exception {

        HttpRequestBody data = request.getBody();

        // Für dieses Surflet sind nur Form Daten erlaubt.
        // Dementsprechend wird alles andere abgewiesen.
        if (data.getContentType() != HttpRequestBody.ContentType.APPLICATION_X_WWW_FORM_URLENCODED) {
            response.status(422).send();
            return;
        }

        Map<String, String> formData = data.asFormData ();

        // Wenn in den Form-Daten kein Nutzername enthalten ist,
        // so wird die Anfrage hier abgewiesen.
        if (formData.containsKey("username") == false) {
            response.status(422).send();
            return;
        }

        Session session = SessionManager.getInstance().loadOrCreateSession(request, response);
        session.put ("username", formData.get("username").trim ());
        session.put ("todos", new ArrayList<TodoItem>(10));
        response.status(301).addHeader("Location", "/index.html").send();
    }
}
