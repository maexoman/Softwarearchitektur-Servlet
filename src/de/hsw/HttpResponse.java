package de.hsw;

import com.google.gson.JsonElement;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpResponse {

    public static final byte[] HTTP_VERSION = "HTTP/1.1".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] SPACE = " ".getBytes(StandardCharsets.US_ASCII);
    public static final byte[] CRLF = new byte[] {0x0D, 0x0A};
    private static final byte[] DEFAULT_STATUS_LINE = ("HTTP/1.1 200 OK" + CRLF).getBytes (StandardCharsets.US_ASCII);

    private static final Map<Integer, byte[]> REASON_PHRASES = new HashMap<> ();
    {
        // Hier eine (hoffentlich) vollständige Liste an in RFC2616 (Sec. 6.1.1) definierten Status-Codes:
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

        REASON_PHRASES.put(500, "Internal de.hsw.Server Error".getBytes(StandardCharsets.US_ASCII));
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

    private OutputStream outputStream;
    private Map<String, String> headers = new HashMap<>();

    private byte[] statusLine = HttpResponse.DEFAULT_STATUS_LINE;

    private boolean headersSent = false;

    private List<Byte> bodyBuffer = new ArrayList<>();
    private HttpRequestBody.ContentType contentType = HttpRequestBody.ContentType.TEXT_PLAIN;
    private Charset charset = StandardCharsets.UTF_8;

    public HttpResponse (OutputStream outputStream) {
        this.outputStream = outputStream;

        this.headers.put("Content-Length", "0");
        this.headers.put("Connection", "keep-alive");
    }

    private byte[] getReasonPhraseFromStatusCode (int statusCode) throws Exception {
        if (HttpResponse.REASON_PHRASES.containsKey (statusCode) == false) {
            throw new Exception("Unnkown status code. Please provide a custom reason code.");
        }
        return HttpResponse.REASON_PHRASES.get (statusCode);
    }

    public void json (JsonElement json) {
        this.setContentType (HttpRequestBody.ContentType.APPLICATION_JSON);
        this.send (json.toString ());
    }

    public HttpResponse setContentType (HttpRequestBody.ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    public HttpResponse status (int statusCode) throws Exception {
        return this.status (statusCode, this.getReasonPhraseFromStatusCode (statusCode));
    }

    public HttpResponse status (int statusCode, String reasonPhrase) throws Exception {
        return this.status(statusCode, reasonPhrase.getBytes (StandardCharsets.US_ASCII));
    }

    public HttpResponse status (int statusCode, byte[] reasonPhrase) throws Exception {
        this.statusLine = this.concatByteArrays(
                HTTP_VERSION,
                SPACE,
                Integer.toString (statusCode).getBytes(StandardCharsets.US_ASCII),
                SPACE,
                reasonPhrase,
                CRLF

        );
        return this;
    }

    public HttpResponse addHeader (String name, String value) {

        if (this.headersSent == true) {
            System.out.println("[ERROR]: headers already sent.");
            return this;
        }

        this.headers.put (name, value);
        return this;
    }

    private String resolveContentTypeAndCharset () {
        return this.contentType.toString() + "; charset=" + this.charset.toString();
    }

    public HttpResponse sendStatusLineAndHeaders () {

        this.write (this.statusLine);

        if (this.headers.containsKey("Content-Type") == false) {
            this.headers.put("Content-Type", this.resolveContentTypeAndCharset ());
        }

        this.headers.keySet().forEach(key -> {
            String value = HttpResponse.this.headers.get (key);
            HttpResponse.this.write (key + ": " + value, StandardCharsets.US_ASCII);
            HttpResponse.this.write (CRLF);
        });
        this.write (CRLF);
        try {
            this.outputStream.flush();
            this.headersSent = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public HttpResponse write (String message) {
        return this.write (message, StandardCharsets.UTF_8);
    }

    public HttpResponse write (String message, Charset charset) {
        return this.write (message.getBytes (charset));
    }

    public HttpResponse write (byte[] bytes) {
        try {
            this.outputStream.write (bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }


    // TODO: auslagern in eine Utils Klasse
    private byte[] getByteArrayFromBuffer (List<Byte> buffer) {
        int bufferSize = buffer.size();
        Iterator<Byte> iterator = buffer.iterator();
        byte[] result = new byte[bufferSize];
        int index = 0;
        while (iterator.hasNext()) {
            result [index] = iterator.next();
            index += 1;
        }
        return result;
    }

    public HttpResponse append (String message) {
        return this.append (message, StandardCharsets.UTF_8);
    }

    public HttpResponse append (String message, Charset charset) {
        return this.append (message.getBytes (charset));
    }

    public HttpResponse append (byte[] bytes) {
        for (int index = 0; index < bytes.length; index += 1) {
            this.bodyBuffer.add ((Byte) bytes [index]);
        }
        return this;
    }

    public void send () {
        this.send (this.getByteArrayFromBuffer (this.bodyBuffer));
    }

    public void send (String message) {
        this.send (message, StandardCharsets.UTF_8);
    }

    public void send (String message, Charset charset) {
        this.send (message.getBytes (charset));
    }

    public void send (byte[] bytes) {

        if (this.headersSent == false) {
            this.addHeader ("Content-Length", Integer.toString (bytes.length));
            this.sendStatusLineAndHeaders();
        }

        this.write (bytes);
        this.flush();
    }

    public void flush () {
        try {
            this.outputStream.flush ();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] concatByteArrays (byte[]... arrays) {

        byte[] result;
        int byteSize = 0;
        int pos = 0;
        for (int index = 0; index < arrays.length; index += 1) {
            byteSize += arrays [index].length;
        }

        result = new byte [byteSize];

        for (int index = 0; index < arrays.length; index += 1) {
            byte[] oneArray = arrays [index];
            for (int arrayIndex = 0; arrayIndex < oneArray.length; arrayIndex += 1) {
                result [pos] = oneArray [arrayIndex];
                pos += 1;
            }
        }


        return result;
    }


    @Override
    public String toString() {
        return "de.hsw.HttpResponse{" +
                "headers=" + headers +
                ", statusLine=" + Arrays.toString(statusLine) +
                ", headersSent=" + headersSent +
                ", bodyBuffer=" + bodyBuffer +
                ", contentType=" + contentType +
                ", charset=" + charset +
                '}';
    }
}
