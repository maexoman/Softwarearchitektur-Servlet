package de.hsw.http;

import com.google.gson.JsonElement;
import de.hsw.http.helper.ByteUtils;
import de.hsw.http.helper.HttpRequestBody;
import de.hsw.sessions.Cookie;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpResponse {

    // Hier ein paar gängige Byte-Sequencen, wie sie für die Beantwortung von Http-Anfragen benötigt werden.
    public static final byte[] HTTP_VERSION = "HTTP/1.1".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] SPACE = " ".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] CRLF = new byte[] {0x0D, 0x0A};
    private static final byte[] DEFAULT_STATUS_LINE = ("HTTP/1.1 200 OK" + CRLF).getBytes (StandardCharsets.US_ASCII);

    // Hier eine (hoffentlich) vollständige Liste an in RFC2616 (Sec. 6.1.1) definierten Status-Codes:
    private static final Map<Integer, byte[]> REASON_PHRASES = new HashMap<> ();
    static {
        REASON_PHRASES.put(100, "Continue".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(101, "Switching Protocols".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(102, "Processing".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(103, "Early Hints".getBytes(StandardCharsets.US_ASCII));

        REASON_PHRASES.put(200, "OK".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(201, "Created".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(202, "Accepted".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(203, "Non-Authoritative Information".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(204, "No Content".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(205, "Reset Content".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(206, "Partial Content".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(207, "Multi-Status".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(208, "Already Reported".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(226, "IM Used".getBytes(StandardCharsets.US_ASCII));

        REASON_PHRASES.put(300, "Multiple Choices".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(301, "Moved Permanently".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(302, "Found (Previously \"Moved Temporarily\")".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(303, "See Other".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(304, "Not Modified".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(305, "Use Proxy".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(306, "Switch Proxy".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(307, "Temporary Redirect".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(308, "Permanent Redirect".getBytes(StandardCharsets.US_ASCII));

        REASON_PHRASES.put(400, "Bad Request".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(401, "Unauthorized".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(402, "Payment Required".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(403, "Forbidden".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(404, "Not Found".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(405, "Method Not Allowed".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(406, "Not Acceptable".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(407, "Proxy Authentication Required".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(408, "Request Timeout".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(409, "Conflict".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(410, "Gone".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(411, "Length Required".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(412, "Precondition Failed".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(413, "Payload Too Large".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(414, "URI Too Long".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(415, "Unsupported Media Type".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(416, "Range Not Satisfiable".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(417, "Expectation Failed".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(418, "I'm a Teapot".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(421, "Misdirected Request".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(422, "Unprocessable Entity".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(423, "Locked".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(424, "Failed Dependency".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(425, "Too Early".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(426, "Upgrade Required".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(428, "Precondition Required".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(429, "Too Many Requests".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(431, "Request Header Fields Too Large".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(451, "Unavailable For Legal Reasons".getBytes(StandardCharsets.US_ASCII));

        REASON_PHRASES.put(500, "Internal de.hsw.server.Server Error".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(501, "Not Implemented".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(502, "Bad Gateway".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(503, "Service Unavailable".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(504, "Gateway Timeout".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(505, "HTTP Version Not Supported".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(506, "Variant Also Negotiates".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(507, "Insufficient Storage".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(508, "Loop Detected".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(510, "Not Extended".getBytes(StandardCharsets.US_ASCII));
        REASON_PHRASES.put(511, "Network Authentication Required".getBytes(StandardCharsets.US_ASCII));
    }

    // Der eigentliche Stream:
    private OutputStream outputStream;

    // Die 1. Zeile der Antwort.
    private byte[] statusLine = HttpResponse.DEFAULT_STATUS_LINE;

    // Alle Informationen zu den Antwort-Headern:
    private Map<String, String> headers = new HashMap<>();
    private boolean headersSent = false;

    // Alle Informationen zum Response-Body:
    private List<Byte> bodyBuffer = new ArrayList<>();
    private HttpRequestBody.ContentType contentType = HttpRequestBody.ContentType.TEXT_PLAIN;
    private Charset charset = StandardCharsets.UTF_8;

    // Alle Cookies:
    private Map<String, Cookie> cookies = new HashMap<>();

    /**
     * Hier wird die Http-Antwort bereits mit Standard-Werten befüllt.
     * @param outputStream Der Output-Stream des Sockets.
     */
    public HttpResponse (OutputStream outputStream) {
        this.outputStream = outputStream;
        this.headers.put("Content-Length", "0");
        this.headers.put("Connection", "keep-alive");
    }

    /**
     * Hiermit wird versucht zu einem Http-Status-Code (numerisch) den dazugehörigen Satz zu ermitteln.
     * @param statusCode Der numerische Http-Status-Code
     * @return Der Reason-Code.
     */
    private byte[] getReasonPhraseFromStatusCode (int statusCode) {
        // Wenn es zu diesem Status-Code keinen Satz gibt, hätte der Entwickler eine andere Aufrufwariante der
        // .status Methoden verwenden müssen. Also hauen wir ihm/ihr das um die Ohren.
        if (HttpResponse.REASON_PHRASES.containsKey (statusCode) == false) {
            throw new RuntimeException("Unnkown status code. Please provide a custom reason code.");
        }
        return HttpResponse.REASON_PHRASES.get (statusCode);
    }

    /**
     * Hiermit kann der Content-Type des Antwort-Bodys gesetzt werden.
     * @param contentType Der Content-Type, der gesetzt werden soll.
     * @return Dieses HttpResponse Objekt, damit method chaining verwendet werden kann.
     */
    public HttpResponse setContentType (HttpRequestBody.ContentType contentType) {
        if (this.headersSent == true) {
            System.out.println ("[WARNING]: headers have been sent already. Calling .setContentType has no effect.");
            return this;
        }
        this.contentType = contentType;
        return this;
    }

    /**
     * Hiermit kann die Response-Line (Status-Line) gesetzt werden.
     * @param statusCode Einer der oben definierten Status-Codes.
     * @return Dieses HttpResponse Objekt, damit method chaining verwendet werden kann.
     */
    public HttpResponse status (int statusCode) {
        return this.status (statusCode, this.getReasonPhraseFromStatusCode (statusCode));
    }

    /**
     * Hiermit kann die Response-Line (Status-Line) gesetzt werden.
     * @param statusCode Ein beliebiger Status-Code.
     * @param reasonPhrase Ein beliebiger Status-Satz.
     * @return Dieses HttpResponse Objekt, damit method chaining verwendet werden kann.
     */
    public HttpResponse status (int statusCode, String reasonPhrase) {
        return this.status(statusCode, reasonPhrase.getBytes (StandardCharsets.US_ASCII));
    }

    /**
     * Hiermit kann die Response-Line (Status-Line) gesetzt werden.
     * @param statusCode Ein beliebiger Status-Code.
     * @param reasonPhrase Ein beliebiger Status-Satz.
     * @return Dieses HttpResponse Objekt, damit method chaining verwendet werden kann.
     */
    public HttpResponse status (int statusCode, byte[] reasonPhrase) {
        if (this.headersSent == true) {
            System.out.println ("[WARNING]: headers have been sent already. Calling .status has no effect.");
            return this;
        }
        this.statusLine = ByteUtils.concatByteArrays (
                HTTP_VERSION,
                SPACE,
                Integer.toString (statusCode).getBytes(StandardCharsets.US_ASCII),
                SPACE,
                reasonPhrase
        );
        return this;
    }

    /**
     * Hiermit kann der Antwort ein Header angefügt werden.
     * @param name Der Name des Headers.
     * @param value Der Wert des Headers.
     * @return Dieses HttpResponse Objekt, damit method chaining verwendet werden kann.
     */
    public HttpResponse addHeader (String name, String value) {
        if (this.headersSent == true) {
            System.out.println ("[WARNING]: headers have been sent already. Calling .addHeader has no effect.");
            return this;
        }
        this.headers.put (name, value);
        return this;
    }

    /**
     * Hiermit kann der Antwort ein Cookie hinzugefügt werden.
     * @param cookie Der hinzuzufügende Cookie.
     * @return Dieses HttpResponse Objekt, damit method chaining verwendet werden kann.
     */
    public HttpResponse addCookie (Cookie cookie) {
        if (this.headersSent == true) {
            System.out.println ("[WARNING]: headers have been sent already. Calling .addCookie has no effect.");
            return this;
        }
        this.cookies.put (cookie.getName(), cookie);
        return this;
    }

    /**
     * Hiermit kann ein Cookie Client Seitig gelöscht werden,
     * indem der Ablaufzeitpunkt mit dem Set-Cookie Header in die Vergangenheit gesetzt wird.
     * @param name Der Name des zu löschenden Cookies.
     * @return Dieses HttpResponse Objekt, damit method chaining verwendet werden kann.
     */
    public HttpResponse removeCookie (String name) {

        if (this.headersSent == true) {
            System.out.println ("[WARNING]: headers have been sent already. Calling .removeCookie has no effect.");
            return this;
        }

        Cookie deleteCookie = new Cookie (name, "deleted");
        deleteCookie.setExpiration (new Date (0L));
        this.cookies.put (name, deleteCookie);
        return this;
    }

    /**
     * Hiermit wird ein US-ASCII Header (als String) direkt in den Outputstream geschrieben werden.
     * @param message Der Header (als String), der in den Outputstream geschrieben werden soll.
     */
    private void writeHeader (String message) {
        this.writeHeader (message.getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * Hiermit wird ein US-ASCII Header direkt in den Outputstream geschrieben werden.
     * @param bytes Der Header, der in den Outputstream geschrieben werden soll.
     */
    private void writeHeader (byte[] bytes) {
        try {
            this.outputStream.write (ByteUtils.concatByteArrays (bytes, CRLF));
            this.outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hiermit kann ein UTF-8 String direkt in den Outputstream geschrieben werden.
     * @param message Der String, der in den Outputstream geschrieben werden soll.
     * @return Dieses HttpResponse Objekt, damit method chaining verwendet werden kann.
     */
    public HttpResponse write (String message) {
        return this.write (message, StandardCharsets.UTF_8);
    }

    /**
     * Hiermit kann ein String direkt in den Outputstream geschrieben werden.
     * @param message Der String, der hinzugefügt werden soll.
     * @param charset Das Encoding des Strings.
     * @return Dieses HttpResponse Objekt, damit method chaining verwendet werden kann.
     */
    public HttpResponse write (String message, Charset charset) {
        return this.write (message.getBytes (charset));
    }

    /**
     * Hiermit können rohe Bytes in den Outputstream geschrieben werden.
     * @param bytes Das Array der rohen Bytes, die in den Outputstream geschrieben werden sollen.
     * @return Dieses HttpResponse Objekt, damit method chaining verwendet werden kann.
     */
    public HttpResponse write (byte[] bytes) {
        if (this.headersSent == false) {
            System.out.println ("[WARNING]: headers have not been sent. Calling .write has no effect.");
            return this;
        }
        try {
            this.outputStream.write (bytes);
            this.outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Hiermit kann der interne Buffer in den Outputstream geschrieben werden.
     * Außerdem wird hier die Antwort geschloßen.
     */
    public void send () {
        this.send (ByteUtils.byteArrayFromDynamicBuffer (this.bodyBuffer));
    }

    /**
     * Hiermit kann ein UTF-8 String in den Outputstream geschrieben werden.
     * Außerdem wird hier die Antwort geschloßen.
     * @param message Der String, der in den Outputstream geschrieben werden soll.
     */
    public void send (String message) {
        this.send (message, StandardCharsets.UTF_8);
    }

    /**
     * Hiermit kann ein String in den Outputstream geschrieben werden.
     * Außerdem wird hier die Antwort geschloßen.
     * @param message Der String, der in den Outputstream geschrieben werden soll.
     * @param charset Das Encoding des Strings.
     */
    public void send (String message, Charset charset) {
        this.send (message.getBytes (charset));
    }

    /**
     * Hiermit können rohe Bytes in den Outputstream geschrieben werden.
     * Außerdem wird hier die Antwort geschloßen.
     * @param bytes Die rohen Bytes, die in den Outputstream geschrieben werden sollen.
     */
    public void send (byte[] bytes) {
        if (this.headersSent == false) {
            this.addHeader ("Content-Length", Integer.toString (bytes.length));
            this.sendStatusLineAndHeaders();
        }
        this.write (bytes);
    }

    /**
     * Hiermit kann ein UTF-8 String zum internen Body-Buffer hinzugefügt werden.
     * @param message Der String, der hinzugefügt werden soll.
     * @return Dieses HttpResponse Objekt, damit method chaining verwendet werden kann.
     */
    public HttpResponse append (String message) {
        return this.append (message, StandardCharsets.UTF_8);
    }

    /**
     * Hiermit kann ein String zum internen Body-Buffer hinzugefügt werden.
     * @param message Der String, der hinzugefügt werden soll.
     * @param charset Das Encoding des Strings.
     * @return Dieses HttpResponse Objekt, damit method chaining verwendet werden kann.
     */
    public HttpResponse append (String message, Charset charset) {
        return this.append (message.getBytes (charset));
    }

    /**
     * Hiermit können rohe Bytes zum internen Body-Buffer hinzugefügt werden.
     * @param bytes Das Array der rohen Bytes, die hinzugefügt werden sollen.
     * @return Dieses HttpResponse Objekt, damit method chaining verwendet werden kann.
     */
    public HttpResponse append (byte[] bytes) {
        for (int index = 0; index < bytes.length; index += 1) {
            this.bodyBuffer.add ((Byte) bytes [index]);
        }
        return this;
    }

    /**
     * Hiermit werden alle Cookies dieser Antwort in den Stream geschrieben.
     */
    private void writeCookies () {
        this.cookies
            .values()
            .stream()
            .forEach(cookie -> {
                HttpResponse.this.writeHeader ("Set-Cookie: " + cookie.toString());
            });
    }

    /**
     * Hiermit wird der Wert für den Content-Type-Header erstellt.
     * @return Der Wert, der in den Content-Type-Header soll.
     */
    private String resolveContentTypeAndCharset () {
        return this.contentType.toString() + "; charset=" + this.charset.toString();
    }

    /**
     * Hiermit wird die Status-Zeile und die einzelnen Header (inkl. Set-Cookie-Header) gesendet.
     * @return Dieses HttpResponse Objekt, damit method chaining verwendet werden kann.
     */
    public HttpResponse sendStatusLineAndHeaders () {

        // Wenn die Status-Zeile und die Header bereits gesendet wurde, soll nichts weiter geschehen.
        if (this.headersSent == true) {
            System.out.println ("[WARNING]: headers have been sent already. Calling .sendStatusLineAndHeaders has no effect.");
            return this;
        }

        // Schriebe zunächst die Status-Zeile.
        this.writeHeader (this.statusLine);

        // Wenn es noch keinen Content-Type header gibt, dann soll dieser Hier gesetzt werden.
        if (this.headers.containsKey("Content-Type") == false) {
            this.headers.put("Content-Type", this.resolveContentTypeAndCharset ());
        }

        // Schreibe jeden Header:
        this.headers.keySet().forEach(key -> {
            String value = HttpResponse.this.headers.get (key);
            HttpResponse.this.writeHeader (key + ": " + value);
        });

        // Schreibe auch die Set-Cookie-Header:
        this.writeCookies ();

        // Schließe den Header-Block mit einer leeren Zeile ab.
        this.writeHeader ("");

        // Versuche die Bytes zu flushen.
        // Hier wird der Wert von "headerSent" auf true gesetzt,
        // um alle weiteren Schreib versuche eines Headers/Cookies zu unterbinden.
        try {
            this.outputStream.flush();
            this.headersSent = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Hiermit wird die Antwort auf eine JSON Antwort gesetzt.
     * Dies bedeutet, dass der dazugehörige Header erstellt wird.
     * Das JSON-Objekt an sich wird zu einem String umgewandelt und die Antwort abgeschickt.
     * @param json Das zu sendene JSON-Objekt.
     */
    public void json (JsonElement json) {
        this.setContentType (HttpRequestBody.ContentType.APPLICATION_JSON);
        this.send (json.toString ());
    }

}
