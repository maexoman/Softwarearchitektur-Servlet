package de.hsw.surflets;

import de.hsw.http.HttpRequest;
import de.hsw.http.helper.HttpRequestBody;
import de.hsw.http.HttpResponse;
import de.hsw.Surflet;

/**
 * Hier ein kleines Error 404 Surflet.
 * Dies ist unser Standard-Surflet, welches angezeigt wird,
 * wenn kein Surflet für einen bestimmten Pfad gefunden werden kann.
 */
public class Error404Surflet implements Surflet {

    @Override
    public void handleRequest(HttpRequest request, HttpResponse response) throws Exception {
        response.setContentType(HttpRequestBody.ContentType.TEXT_HTML);
        response.status(404);
        response.append("<html>\n");
        response.append("<head>");
        response.append("</head>\n");
        response.append("<body>\n");
        response.append("<h1>404 - Der Pfad \"" + request.getPath() + "\" konnte nicht gefunden werden</h1>\n");
        response.append("</body>\n");
        response.append("</html>");
        response.send();
    }

}
