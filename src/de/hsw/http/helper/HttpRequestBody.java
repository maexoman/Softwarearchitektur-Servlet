package de.hsw.http.helper;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import de.hsw.errors.ConnectionClosedException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpRequestBody {

    public static final String STR_EMPTY = "";

    /**
     * Hiermit kann der Anfragen-Body initialisiert werden.
     * Hierfür werden die Header einer Anfrage gelesen, um die Länge des Inhalts und ggf. seinen Typ zu erfahren.
     * @param headers Die Header der Http-Anfrage.
     * @return Gibt einen Wrapper für den eigentlichen HTTP-Anfragen-Body zurück.
     */
    public static HttpRequestBody init (Map<String, String> headers) {

        int size = 0;
        HttpRequestBody result;

        if (headers.containsKey ("content-length")) {
            try {
                size = Integer.parseInt (headers.get ("content-length"));
            } catch (RuntimeException e) {
                System.out.println("[ERROR]: es ist ein Fehler dabei aufgetreten den Content-Length Header zu parsen.");
            }
        }

        result = new HttpRequestBody (size);

        if (headers.containsKey("content-type")) {
            result.parseContentTypeAndCharset (headers.get ("content-type"));
        }

        return result;
    }

    /**
     * In dieser Enum sind alle vom Server Unterstützten Content-Types vorhanden.
     */
    public enum ContentType {

        UNSUPPORTED ("unsupported"),
        TEXT_PLAIN ("text/plain"),
        TEXT_HTML ("text/html"),
        APPLICATION_JSON ("application/json"),
        APPLICATION_X_WWW_FORM_URLENCODED ("application/x-www-form-urlencoded");

        private final String toStringValue;
        ContentType (String toStringValue) {
            this.toStringValue = toStringValue;
        }
        @Override public String toString () {
            return this.toStringValue;
        }
    }

    private int size;
    private byte[] data = null;
    private ContentType contentType = ContentType.TEXT_PLAIN;
    private Charset contentCharset = StandardCharsets.ISO_8859_1;
    private String stringRepresentation = null;

    /**
     * Hiermit kann der Request-Body mit einer gewissen größe erstellt werden.
     * Dieser Konstruktor ist jeodch private, da das Parsing des Bodys durch die Header einer Http-Anfrage initialisiert
     * werden muss.
     * @param size Die Anzahl an Bytes, die der Anfragen-Body haben sollte (laut Content-Length Header)
     */
    private HttpRequestBody (int size) {
        this.size = size;
    }

    /**
     * Hiermit soll der Content-Type aus dem gleichnamigen Header geparsed werden.
     * @param typeAndSubtype Die Angabe zum Content-Type im format <type>/<subtype>.
     * @return Der geparste Content-Type.
     */
    private ContentType parseContentType (String typeAndSubtype) {
        switch (typeAndSubtype) {
            case "application/json": return ContentType.APPLICATION_JSON;
            case "application/x-www-form-urlencoded": return ContentType.APPLICATION_X_WWW_FORM_URLENCODED;
            case "text/plain": return ContentType.TEXT_PLAIN;
            case "text/html": return ContentType.TEXT_HTML;
            default: return ContentType.UNSUPPORTED;
        }
    }

    /**
     * Hiermit kann der Charset aus einem Content-Type Header geparsed werden.
     * Wird kein Charset angegeben erwarten wir ISO-8859-1 (siehe RFC 2616).
     * @param parameter Der Parameter des Content-Types.
     * @return Der Charset, der für die Kodierung der Strings verwendet werden soll.
     */
    private Charset parseContentCharset (String parameter) {

        final String CHARSET_PREFIX = "charset=";
        String charsetCode;

        if (parameter.startsWith (CHARSET_PREFIX) == false) {
            return StandardCharsets.ISO_8859_1;
        }

        charsetCode = parameter.substring(CHARSET_PREFIX.length());

        switch (charsetCode) {
            case "utf-8": return StandardCharsets.UTF_8;
            case "utf-16": return StandardCharsets.UTF_16;
            case "us-ascii": return StandardCharsets.US_ASCII;
            default: return StandardCharsets.ISO_8859_1;
        }
    }

    /**
     * Hiermit kann der Content-Type Header geparsed werden.
     * Intern wird der Header zerlegt in den tatsächlichen Content-Type und in die einzelnen Parameter.
     * @param contentTypeHeaderValue Die Zeile des Content-Type Headers.
     */
    public void parseContentTypeAndCharset (String contentTypeHeaderValue) {
        final int TYPE_AND_SUBTYPE = 0;
        final int PARAMETER = 1;

        String[] mediaType = contentTypeHeaderValue.split(";");
        String typeSubtype;
        String paramter;

        // Es sollte mindestens ein Element, also der tatsächliche Content-Type vorhanden sein.
        // Ist dem nicht so, wird auf den Standard zurückgegriffen.
        if (mediaType.length < 1) {
            this.contentType = ContentType.TEXT_PLAIN;
            this.contentCharset = StandardCharsets.ISO_8859_1;
            return;
        }

        // Andernfalls soll der tatsächliche Content-Type geparsed werden:
        typeSubtype = mediaType [TYPE_AND_SUBTYPE].trim().toLowerCase();
        this.contentType = this.parseContentType (typeSubtype);

        // Wenn auch Parameter vorhanden sind, dann sollen diese Geparsed werden,
        // da hier ggf. der Charset enthalten ist.
        if (mediaType.length == 2) {
            paramter = mediaType [PARAMETER].trim().toLowerCase();
            this.contentCharset = this.parseContentCharset (paramter);
        }
    }

    /**
     * Diese Funktion ist dafür verantwortlich, die eigentlichen Bytes aus dem Stream zu lesen.
     * @param reader
     * @throws ConnectionClosedException
     */
    public void fill (UnsafeHttpInputStreamReader reader) throws ConnectionClosedException {
        this.data = reader.readBytes (this.size);
        this.stringRepresentation = null;
    }

    /**
     * Hiermit kann geschaut werden, ob der Http-Anfragen-Body leer ist.
     * @return "True", wenn der Http-Anfragen-Body leer ist. Andernfalls "False".
     */
    public boolean isEmpty () {
        return (this.size == 0 || this.data == null || this.data.length == 0);
    }

    /**
     * Hier wird die Länge, also die Anzahl an Bytes, des Http-Anfragen-Bodys zurückgegeben.
     * @return Die Länge des Http-Anfragen-Bodys.
     */
    public int size () {
        return this.size;
    }

    /**
     * Hier wird der ermittelte Content-Type des Http-Anfragen-Bodys zurückgegeben.
     * ContentType.UNSUPPORTED falls er nicht nativ vom Server unterstützt wird.
     * @return Der ermittelte Content-Type des Http-Anfragen-Body.
     */
    public ContentType getContentType () {
        return this.contentType;
    }

    /**
     * Hiermit kann der Inhalt des Http-Anfragen-Bodys als String angefordert werden.
     * @return Der Inhalt des Http-Anfragen-Bodys.
     */
    public String asText () {

        // Die String representation wird "lazy" berechnet.
        // Wenn sie hier vorhanden ist, wurde sie bereits "berechnet".
        // Kann also einfach zurückgegeben werden.
        if (this.stringRepresentation != null) {
            return this.stringRepresentation;
        }

        // Hier Angekommen wird das Byte-Array unter Berücksichtigung des Charsets zu einem String umgewandelt.
        if (this.isEmpty()) {
            this.stringRepresentation = STR_EMPTY;
        } else {
            this.stringRepresentation = new String(this.data, this.contentCharset);
        }

        return this.stringRepresentation;
    }

    /**
     * Hiermit kann der Inhalt des Http-Anfragen-Bodys als Form-Data interpretiert und angefordert werden.
     * @return Der Inhalt des Http-Anfragen-Bodys.
     */
    public Map<String, String> asFormData () {
        return UrlDecodingUtils.parseXWwwFormUrlencoded (this.asText ());
    }

    /**
     * Hiermit kann der Inhalt des Http-Anfragen-Bodys als JSON-Element (von GSON) angefordert werden.
     * @return Der Inhalt des Http-Anfragen-Bodys.
     */
    public JsonElement asJson () {
        if (this.isEmpty() || this.contentType != ContentType.APPLICATION_JSON) {
            return null;
        }
        return new Gson().fromJson(this.asText(), JsonElement.class);
    }

    /**
     * Hiermit kann der Inhalt des Http-Anfragen-Bodys im Raw-Format, also die gelesenen Bytes, angefordert werden.
     * @return Die gelesenen Bytes.
     */
    public byte[] raw () {
        return this.data;
    }
}
