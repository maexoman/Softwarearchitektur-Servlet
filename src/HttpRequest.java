import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    public static final String STR_EMPTY = "";

    private RequestMethod method;
    private String path;
    private String version;

    private Map<String, String> queryParameter = new HashMap<>();



    private Map<String, String> headers = new HashMap<>();
    private HttpRequestBody body;


    public enum RequestMethod {
        GET,
        POST,
        DELETE
    }

    private RequestMethod resolveRequestMethod (String method) throws Exception {
        switch (method.toUpperCase()) {
            case "GET":
                return RequestMethod.GET;
            case "POST":
                return RequestMethod.POST;
            case "DELETE":
                return RequestMethod.DELETE;
            default:
                throw new Exception("Method not Supported");
        }
    }

    public void parseAndAddHeader (String line) throws Exception {

        // Eine Header-Line ist folgendermaßen definiert:
        // <Header-Name>: <Header-Value>
        // Hier wird jedoch, abweichend vom RFC 2616, kein Header über mehr als eine Zeile berücksichtigt.

        String key;
        String value;

        // Finde also zunächst den 1. Doppelpunkt.
        // Wenn keiner gefunden werden kann, soll ein Fehler geworfen werden.
        int firstColonIndex = line.indexOf(':');
        if (firstColonIndex < 0) {
            throw new Exception("Header Malformatted");
        }

        // Hole den Key und den Wert mithilfe des Indexes und der substring Funktion des Strings.
        // Der Key ist dabei case-insensitive (RFC 2616 Section 4.2).
        key   = line.substring(0, firstColonIndex).trim().toLowerCase();
        value = line.substring (firstColonIndex + 1).trim();

        this.headers.put(key, value);
    }

    public void setRequestLine (String requestLine) throws Exception {

        final int HTTP_VERB = 0;
        final int HTTP_PATH = 1;
        final int HTTP_VERSION = 2;

        // Aufbau der Request-Zeile:
        // <HTTP-VERB> <PFAD> <VERSION>

        // Hier wird ein split auf Leerzeichen durchgeführt, da die Leerzeichen die Abschnitte voneinander trennen.
        String[] requestLineParts = requestLine.split("\\s+");

        // Es sollten drei Teile entstehen. In dem 1. Element (Index 0) ist das Verb enthalten.
        // Im 2. (Index 1) der gewünschte Pfad, und im 3. (Index 2) die vom Browser verwendete HTTP-Version.
        if (requestLineParts.length < 3) {
            throw new Exception("Request Line Malformatted");
        }

        this.method = this.resolveRequestMethod (requestLineParts [HTTP_VERB]);
        this.path = this.decodeUrlEncoding (requestLineParts [HTTP_PATH]);
        this.version = requestLineParts [HTTP_VERSION];

        this.parsePath();

    }

    // Definitiv nicht vollständig!
    private String decodeUrlEncoding (String line) {
        return line.replace("%20", " ");
    }

    private void parsePath () throws Exception {
        if (this.path.startsWith("/")) {
            String path = this.path;

            int questionmarkIndex = this.path.indexOf('?');
            if (questionmarkIndex > -1) {

                path = this.path.substring(0, questionmarkIndex);
                String queryPart = this.path.substring(questionmarkIndex + 1);

                String[] queryParts = queryPart.split("&");

                for (int i = 0; i < queryParts.length; i += 1) {
                    String q = queryParts [i].trim();

                    if (q.length() == 0) {
                        continue;
                    }

                    int equalIndex = q.indexOf("=");

                    String key = q.substring (0, equalIndex).trim ();
                    String value = q.substring (equalIndex + 1).trim ();

                    if (key.length() == 0) {
                        continue;
                    }

                    this.queryParameter.put (key, value);

                }
            }
            this.path = path;
            return;
        }
        throw new Exception("Absolut Paths and * not implemented");
    }

    public String getHeader (String header, String defaultValue) {
        String value = this.getHeader(header);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    public String getHeader (String header) {
        if (this.headers.containsKey (header) == false) {
            return null;
        }
        return this.headers.get (header);
    }

    public HttpRequest (InputStream inputStream) throws Exception {
        this (new UnsafeHttpInputStreamReader (inputStream));
    }

    public HttpRequest (UnsafeHttpInputStreamReader reader) throws Exception {
        this.setRequestLine (reader.readLineAsUSASCII());

        while (true) {
            String headerLine = reader.readLineAsUSASCII ();
            if (headerLine.trim().equalsIgnoreCase(STR_EMPTY)) {
                break;
            }
            this.parseAndAddHeader (headerLine);
        }

        this.body = HttpRequestBody.init (this.headers);
        this.body.fill (reader);
    }

    public HttpRequestBody getBody () {
        return this.body;
    }

    public String getPath () {
        return this.path;
    }

    public String getParameter (String paramter, String defaultValue) {
        String value = this.getParameter (paramter);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    public String getParameter (String parameter) {
        if (this.queryParameter.containsKey(parameter) == false) {
            return null;
        }
        return this.queryParameter.get (parameter);
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                    "method=" + method +
                    ", path='" + path + '\'' +
                    ", version='" + version + '\'' +
                    ", headers=" + headers +
                    ", query=" + this.queryParameter +
                    ", body=" + this.body +
                '}';
    }
}
