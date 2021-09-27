package de.hsw.surflets;

import de.hsw.Surflet;
import de.hsw.http.HttpRequest;
import de.hsw.http.HttpResponse;
import de.hsw.http.helper.HttpRequestBody;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticFileSurflet implements Surflet {

    private String getContentTypeFromExtension (String extension) {
        switch (extension.trim ().toLowerCase()) {
            // Data/Code
            case "js": return "application/javascript";
            case "xml": return "application/xml";
            case "json": return "application/json";
            case "pdf": return "application/pdf";

            // WebPages
            case "html": return "text/html";
            case "css": return "text/css";

            // Images
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "jpg":
            case "jpeg":
                return "image/jpeg";

            default: return "application/octet-stream";
        }
    }

    @Override
    public void handleRequest(HttpRequest request, HttpResponse response) throws Exception {

        String path = request.getPath();

        if (path.endsWith ("/")) {
            path += "index.html";
        }

        try {
            int periodIndex = path.lastIndexOf (".");
            if (periodIndex < 0) {
                throw new Exception ("No Extension found");
            }
            String extension = path.substring (periodIndex + 1);
            byte[] bytes = Files.readAllBytes (Paths.get ("www" + path));
            response.setContentType (this.getContentTypeFromExtension (extension));
            response.send(bytes);

        // Wenn die Datei nicht gelesen werden konnte, soll hier der 404 Error angezeigt werden:
        } catch (Exception e) {
            e.printStackTrace();
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
}
