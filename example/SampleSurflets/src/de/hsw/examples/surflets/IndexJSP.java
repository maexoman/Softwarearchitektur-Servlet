package de.hsw.examples.surflets;

import de.hsw.Surflet;
import de.hsw.http.HttpRequest;
import de.hsw.http.HttpResponse;
import de.hsw.http.helper.HttpRequestBody;
import de.hsw.sessions.Session;
import de.hsw.sessions.SessionManager;

public class IndexJSP implements Surflet {
    @Override
    public void handleRequest(HttpRequest request, HttpResponse response) throws Exception {

        Session session = SessionManager.getInstance().loadOrCreateSession(request, response);
        if (session.isUnsafe() || session.has("username") == false || session.get("username").equals("")) {
            response.status(301).addHeader("Location", "/signin.html").send();
            return;
        }


        response.setContentType(HttpRequestBody.ContentType.TEXT_HTML);
        response.append("<!DOCTYPE html>");
        response.append("<html lang=\"en\">");
        response.append("<head>");
            response.append("<meta charset=\"UTF-8\">");
            response.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
            response.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            response.append("<title>ToDo's von " + session.get("username") + "</title>");
            response.append("<link rel=\"stylesheet\" href=\"main.css\">");
            response.append("<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta2/css/all.min.css\" integrity=\"sha512-YWzhKL2whUzgiheMoBFwW8CKV4qpHQAEuvilg9FAn5VJUDwKZZxkJNuGM4XkWuk94WCrrwslk8yWNGmY1EduTA==\" crossorigin=\"anonymous\" referrerpolicy=\"no-referrer\" />");
            response.append("<script src=\"app.js\" defer></script>");
        response.append("</head>");
        response.append("<body>");
            response.append("<header><div><h1>Hallo " + session.get("username") + ",</h1><h2>hier sind deine ToDo's</h2></div></header>");
            response.append("<main>");
            response.append("<ul id=\"tasks\"></ul>");
            response.append("<button onclick=\"addNew()\"><i class=\"fas fa-plus\"></i></button>");
            response.append("</main>");
        response.append("</body>");
        response.append("</html>");

        response.send();
    }
}
