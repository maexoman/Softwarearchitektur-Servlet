package de.hsw.http;

import de.hsw.http.helper.HttpRequestBody;
import de.hsw.http.helper.UnsafeHttpInputStreamReader;
import de.hsw.errors.ConnectionClosedException;
import de.hsw.errors.InvalidHttpRequestException;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpRequest {

    public static final String STR_EMPTY = "";

    // Hier eine Auflistung aller, durch den Server verstandenen HTTP-Methoden.
    private static final List<String> SUPPORTED_REQUEST_METHODS = Arrays.asList ("GET", "POST", "DELETE");

    // Hier wird noch "ALL" hinzugefügt. Dies ist eigentlich nur für den Surflet-Mapper relevant.
    private static final List<String> SUPPORTED_REQUEST_METHODS_INCLUDE_ALL = Stream.concat(
        SUPPORTED_REQUEST_METHODS.stream(),
        Arrays.asList ("ALL").stream()
    ).collect(Collectors.toList());

    // Hiermit können die Unterstützten Methoden gelesen werden.
    public static List<String> getSupportedRequestMethods () {
        return HttpRequest.getSupportedRequestMethods(false);
    }
    public static List<String> getSupportedRequestMethods (boolean includingAll) {
        if (includingAll == true) {
            return HttpRequest.SUPPORTED_REQUEST_METHODS_INCLUDE_ALL;
        }
        return HttpRequest.SUPPORTED_REQUEST_METHODS;
    }

    /**
     * In dieser Enum sind alle vom Server Unterstützten HTTP-Methoden vorhanden.
     */
    public enum RequestMethod {
        GET ("GET"),
        POST ("POST"),
        DELETE ("DELETE");
        private String stringValue;
        RequestMethod (String stringValue) {
            this.stringValue = stringValue;
        }
        @Override public String toString() {
            return this.stringValue;
        }
    }

    // Hier die Inhalte der sogenannten Request-Line:
    private RequestMethod method;
    private String path;

    // Hier alles Weitere (Header, Query-Parameter, Anfragen-Body):
    private Map<String, String> queryParameter = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private HttpRequestBody body;

    /**
     * Hiermit kann ein neuer Request aus einem Stream gelesen werden.
     * @param inputStream Der Input-Stream des Sockets.
     * @throws ConnectionClosedException
     * @throws InvalidHttpRequestException
     */
    public HttpRequest (InputStream inputStream)
            throws ConnectionClosedException, InvalidHttpRequestException
    {
        this (new UnsafeHttpInputStreamReader(inputStream));
    }

    /**
     * Hier wird das eigentliche Lesen des Requests aus dem Stream getan.
     * @param reader Der HttpInputStreamReader.
     * @throws ConnectionClosedException
     * @throws InvalidHttpRequestException
     */
    public HttpRequest (UnsafeHttpInputStreamReader reader)
            throws ConnectionClosedException, InvalidHttpRequestException
    {
        // Parse zunächst die Request-Line:
        this.setRequestLine (reader.readLineAsUSASCII());

        // Lese so lange neue Header-Lines, bis eine komplett leere Zeile zurück kommt.
        while (true) {
            String headerLine = reader.readLineAsUSASCII ();
            if (headerLine.trim().equalsIgnoreCase(STR_EMPTY)) {
                break;
            }
            this.parseAndAddHeader (headerLine);
        }

        // Versuche dann den Body zu parsen.
        this.body = HttpRequestBody.init (this.headers);
        this.body.fill (reader);
    }

    /**
     * Hiermit kann versucht werden eine Methode aus der Request-Line in String form,
     * in eine aus der Enum oben umzuwandeln.
     * @param method Die Methode aus der Request-Line.
     * @return Die Methode in Enum Form.
     * @throws InvalidHttpRequestException
     */
    private RequestMethod resolveRequestMethod (String method) throws InvalidHttpRequestException {
        switch (method.toUpperCase().trim ()) {
            case "GET":
                return RequestMethod.GET;
            case "POST":
                return RequestMethod.POST;
            case "DELETE":
                return RequestMethod.DELETE;
            default:
                throw new InvalidHttpRequestException ("Request-Method not Supported");
        }
    }

    /**
     * Hiermit können Special-Characters, die bei der Übertragung in URL-Form umkodiert werden,
     * wieder zurück kodiert werden.
     * @param line Die Url.
     * @return Die zurück kodierte Url.
     * ! Wichtig: Diese Liste ist nicht vollständig.
     */
    private String decodeUrlEncoding (String line) {
        return line.replace("%20", " ").replace("+", " ");
    }

    /**
     * Hiermit kann ein Pfad geparsed werden, sodass der eigentliche Pfad + Query-Parameter daraus entstehen.
     * @throws InvalidHttpRequestException
     * ! Wichtig: Es werden nur relative Pfade unterstützt.
     */
    private void parsePath () throws InvalidHttpRequestException {
        // Wir unterstützen nur relative Pfade!
        if (this.path.startsWith("/")) {
            String path = this.path;

            // Suche ein Fragezeichen.
            int questionmarkIndex = this.path.indexOf('?');
            if (questionmarkIndex > -1) {

                // Wenn eins gefunden wird, dann soll der String an dieser Stelle zerschnitten werden.
                // Der vordere Teil ist dann der eigentliche Pfad.
                // Alles weitere sind Query-Paramter.
                path = this.path.substring (0, questionmarkIndex);
                String queryString = this.path.substring (questionmarkIndex + 1);

                // Der Query-String könnte dabei mehrere Parameter enthalten.
                // Diese wären durch ein "&" getrennt. Also wird an den &s gesplittet:
                String[] queryParts = queryString.split("&");

                // Gehe nun die einzelnen Teile durch, und zerlege diese in Key-Value-Paare.
                for (int i = 0; i < queryParts.length; i += 1) {
                    String q = queryParts [i].trim();

                    if (q.length() == 0) {
                        continue;
                    }

                    // Suche nach dem "="
                    int equalIndex = q.indexOf("=");

                    // Gibt es kein "=", so gibt es auch kein Key-Value-Paar.
                    // Es kann also weiter gesprungen werden.
                    if (equalIndex < 0) {
                        continue;
                    }

                    // Zerschneide in Key und Value.
                    String key = q.substring (0, equalIndex).trim ();
                    String value = q.substring (equalIndex + 1).trim ();

                    if (key.length() == 0) {
                        continue;
                    }

                    // Dekodiere das Url-Safe-Kodierte Wertepaar und speichere es bei den Query-Parametern.
                    this.queryParameter.put (
                        this.decodeUrlEncoding (key),
                        this.decodeUrlEncoding (value)
                    );

                }
            }

            // Dekodiere den Url-Safe-Kodierten Pfad.
            this.path = this.decodeUrlEncoding (path);
            return;
        }
        throw new InvalidHttpRequestException ("Absolut Paths and * not implemented");
    }

    /**
     * Hiermit kann die sogenannte Request-Line geparsed und so dem Objekt hinzugefügt werden.
     * @param requestLine Die Request-Line aus RFC2616 Sec. 5.1
     * @throws InvalidHttpRequestException
     */
    public void setRequestLine (String requestLine) throws InvalidHttpRequestException {

        final int HTTP_VERB = 0;
        final int HTTP_PATH = 1;

        // Aufbau der Request-Zeile:
        // <HTTP-VERB> <PFAD> <VERSION>
        // Die Version ist uns egal. Wir gehen von 1.1 aus.

        // Hier wird ein split auf Leerzeichen durchgeführt, da die Leerzeichen die Abschnitte voneinander trennen.
        String[] requestLineParts = requestLine.split("\\s+");

        // Es sollten drei Teile entstehen. In dem 1. Element (Index 0) ist das Verb enthalten.
        // Im 2. (Index 1) der gewünschte Pfad, und im 3. (Index 2) die vom Browser verwendete HTTP-Version.
        // Ist dies nicht der Fall, so ist die Anfrage auf jeden Fall ungültig.
        if (requestLineParts.length < 3) {
            throw new InvalidHttpRequestException ("Invalid Request-Line");
        }

        // Andernfalls kann versucht werden die Methode, sowie den Pfad zu parsen.
        this.method = this.resolveRequestMethod (requestLineParts [HTTP_VERB]);
        this.path = requestLineParts [HTTP_PATH];
        this.parsePath();
    }

    /**
     * Hiermit kann eine Header Zeile geparsed und so dem Objekt hinzugefügt werden.
     * @param line Die aus dem Stream gelesene Header-Zeile.
     */
    public void parseAndAddHeader (String line) {

        // Eine Header-Line ist folgendermaßen definiert:
        // <Header-Name>: <Header-Value>
        // Hier wird jedoch, abweichend vom RFC 2616, kein Header über mehr als eine Zeile berücksichtigt.

        String key;
        String value;

        // Finde also zunächst den 1. Doppelpunkt.
        // Wenn keiner gefunden werden kann, soll ein Fehler geworfen werden.
        int firstColonIndex = line.indexOf(':');
        if (firstColonIndex < 0) {
            System.out.println("[ERROR]: The Header Line \"" + line + "\" is malformatted and is discarded");
            return;
        }

        // Hole den Key und den Wert mithilfe des Indexes und der substring Funktion des Strings.
        // Der Key ist dabei case-insensitive (RFC 2616 Section 4.2).
        key   = line.substring(0, firstColonIndex).trim().toLowerCase();
        value = line.substring (firstColonIndex + 1).trim();

        this.headers.put(key, value);
    }

    /**
     * Hiermit kann die Http-Methode dieses Aufrufs abgefragt werden.
     * @return Die Http-Methode dieses Aufrufs.
     */
    public RequestMethod getMethod () {
        return this.method;
    }

    /**
     * Hiermit kann das Ziel (der Pfad) dieses Aufrufs abgefragt werden.
     * @return Das Ziel (der Pfad) dieses Aufrufs.
     */
    public String getPath () {
        return this.path;
    }

    /**
     * Hiermit kann der Anfrage-Body dieses Aufrufs abgefragt werden.
     * @return Der Anfrage-Body dieses Aufrufs.
     */
    public HttpRequestBody getBody () {
        return this.body;
    }

    /**
     * Hier kann geprüft werden, ob ein Wert für einen bestimmten Header vorliegt.
     * @param header Der Name des Headers.
     * @return "True", wenn ein Wert vorhanden ist. Andernfalls "False".
     */
    public boolean hasHeader (String header) {
        return this.headers.containsKey (header);
    }

    /**
     * Hiermit kann ein Header gelesen werden.
     * @param header Der Name des Headers.
     * @return Der Wert des Headers, wenn es einen unter diesem Namen gibt. Sonst "null".
     */
    public String getHeader (String header) {
        if (this.hasHeader (header) == false) {
            return null;
        }
        return this.headers.get (header);
    }

    /**
     * Hiermit kann ein Header gelesen werden.
     * Gibt es keinen Wert zu diesem Header, so wird der mitgegebene Standardwert zurückgegeben.
     * @param header Der Name des Headers.
     * @param defaultValue Der Standertwert, der zurückgegeben werden soll, wenn der Header == null.
     * @return Der Wert des Headers, wenn es einen unter diesem Namen gibt. Sonst der mitgegebene Standardwert.
     */
    public String getHeader (String header, String defaultValue) {
        String value = this.getHeader(header);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    /**
     * Hier kann geprüft werden, ob ein Wert für einen bestimmten Query-Parameter vorliegt.
     * @param parameter Der Name des Parameters.
     * @return "True", wenn ein Wert vorhanden ist. Andernfalls "False".
     */
    public boolean hasParameter (String parameter) {
        return this.queryParameter.containsKey (parameter);
    }
    /**
     * Hiermit kann ein Query Parameter gelesen werden.
     * @param parameter Der Name des Parameters.
     * @return Der Wert des Parameters, wenn es einen unter diesem Namen gibt. Sonst "null".
     */
    public String getParameter (String parameter) {
        if (this.hasParameter (parameter) == false) {
            return null;
        }
        return this.queryParameter.get (parameter);
    }

    /**
     * Hiermit kann ein Query Parameter gelesen werden.
     * Gibt es keinen Wert zu diesem Parameter, so wird der mitgegebene Standardwert zurückgegeben.
     * @param paramter Der Name des Parameters.
     * @param defaultValue Der Standertwert, der zurückgegeben werden soll, wenn der Parameter == null.
     * @return Der Wert des Parameters, wenn es einen unter diesem Namen gibt. Sonst der mitgegebene Standardwert.
     */
    public String getParameter (String paramter, String defaultValue) {
        String value = this.getParameter (paramter);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
}
