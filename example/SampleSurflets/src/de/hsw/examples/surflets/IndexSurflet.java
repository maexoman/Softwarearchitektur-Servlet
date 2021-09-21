package de.hsw.examples.surflets;


import de.hsw.*;

public class IndexSurflet implements Surflet {
    @Override
    public void handleRequest(HttpRequest request, HttpResponse response) throws Exception {

        Session session = SessionManager.getInstance ().loadOrCreateSession(request, response);
        if (session.has ("name") == false) {
            session.put ("name", request.getParameter ("name", "John Doe"));
        }
        String name = session.get ("name");

        response.setContentType(HttpRequestBody.ContentType.TEXT_HTML);

        response.append("<html>\n");

        response.append("<head>");
        response.append("</head>\n");

        response.append("<body>\n");
        response.append("<form action=\"/index.html\" method=\"get\">\n");

        response.append("<label for=\"fname\">First name:</label>\n");
        response.append("<input type=\"text\" id=\"fname\" name=\"name\"><br><br>\n");
        response.append("<input type=\"submit\" value=\"Submit\">\n");

        response.append("</form>\n");
        response.append("<h1>Hallo " + name + "</h1>\n");
        response.append("</body>\n");


        response.append("</html>");
        response.send();
    }
}
