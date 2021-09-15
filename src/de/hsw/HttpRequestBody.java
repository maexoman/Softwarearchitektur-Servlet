package de.hsw;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class HttpRequestBody {
    public static final String STR_EMPTY = "";
    public enum ContentType {

        TEXT_PLAIN ("text/plain"),
        TEXT_HTML ("text/html"),
        APPLICATION_JSON ("application/json");

        private final String toStringValue;
        ContentType (String toStringValue) {
            this.toStringValue = toStringValue;
        }

        @Override
        public String toString () {
            return this.toStringValue;
        }
    }

    private int size;
    private byte[] data = null;
    private ContentType contentType = ContentType.TEXT_PLAIN;
    private Charset contentCharset = StandardCharsets.ISO_8859_1;

    private String stringRepresentation = null;

    private HttpRequestBody (int size) {
        this.size = size;
    }

    public static HttpRequestBody init (Map<String, String> headers) throws Exception {

        int size = 0;
        HttpRequestBody result;

        if (headers.containsKey ("content-length")) {
            size = Integer.parseInt (headers.get ("content-length"));
        }

        result = new HttpRequestBody (size);

        if (headers.containsKey("content-type")) {
            result.parseContentTypeAndCharset (headers.get ("content-type"));
        }

        return result;
    }

    public boolean isEmpty () {
        return (this.size == 0 || this.data == null || this.data.length == 0);
    }

    public void fill (UnsafeHttpInputStreamReader reader) throws Exception {
        this.data = reader.readBytes (this.size);
        this.stringRepresentation = null;
    }

    private ContentType parseContentType (String typeAndSubtype) throws Exception {
        switch (typeAndSubtype) {
            case "application/json": return ContentType.APPLICATION_JSON;
            case "text/plain": return ContentType.TEXT_PLAIN;
            case "text/html": return ContentType.TEXT_HTML;
            default: throw new Exception ("Media-Type not supported");
        }
    }

    private Charset parseContentCharset (String parameter) throws Exception {

        final String CHARSET_PREFIX = "charset=";
        String charsetCode;

        if (parameter.startsWith (CHARSET_PREFIX) == false) {
            throw new Exception ("Charset not supported");
        }

        charsetCode = parameter.substring(CHARSET_PREFIX.length());

        switch (charsetCode) {
            case "utf-8": return StandardCharsets.UTF_8;
            case "utf-16": return StandardCharsets.UTF_16;
            case "iso-8859-1": return StandardCharsets.ISO_8859_1;
            case "us-ascii": return StandardCharsets.US_ASCII;
            default: throw new Exception ("Charset not supported");
        }
    }

    public void parseContentTypeAndCharset (String contentTypeHeaderValue) throws Exception {
        final int TYPE_AND_SUBTYPE = 0;
        final int PARAMETER = 1;

        String[] mediaType = contentTypeHeaderValue.split(";");
        String typeSubtype;
        String paramter;

        if (mediaType.length < 1) {
            throw new Exception("Media-Type not supported");
        }

        typeSubtype = mediaType [TYPE_AND_SUBTYPE].trim().toLowerCase();
        this.contentType = this.parseContentType (typeSubtype);

        if (mediaType.length == 2) {
            paramter = mediaType [PARAMETER].trim().toLowerCase();
            this.contentCharset = this.parseContentCharset (paramter);
        }
    }

    public int size () {
        return this.size;
    }

    public ContentType getContentType () {
        return this.contentType;
    }

    public String asText () {
        if (this.stringRepresentation != null) {
            return this.stringRepresentation;
        }

        if (this.isEmpty()) {
            this.stringRepresentation = STR_EMPTY;
        } else {
            this.stringRepresentation = new String(this.data, this.contentCharset);
        }

        return this.stringRepresentation;
    }

    public JsonElement asJson () {
        if (this.isEmpty() || this.contentType != ContentType.APPLICATION_JSON) {
            return null;
        }
        return new Gson().fromJson(this.asText(), JsonElement.class);
    }

    public byte[] raw () {
        return this.data;
    }

    @Override
    public String toString() {
        return "de.hsw.HttpRequestBody{" +
                    "data=" + Arrays.toString(data) +
                '}';
    }
}
