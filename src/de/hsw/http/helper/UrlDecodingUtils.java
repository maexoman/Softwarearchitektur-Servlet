package de.hsw.http.helper;

import java.util.HashMap;
import java.util.Map;

public class UrlDecodingUtils {

    /**
     * Hiermit können Special-Characters, die bei der Übertragung in URL-Form umkodiert werden,
     * wieder zurück kodiert werden.
     * @param line Die Url.
     * @return Die zurück kodierte Url.
     * ! Wichtig: Diese Liste ist nicht vollständig.
     */
    public static String decodeUrlEncoding (String line) {
        return line.replace("%20", " ").replace("+", " ");
    }

    /**
     * Hiermit kann ein Query String bzw. ein x-www-form-urlencoded String geparsed werden.
     * @param queryString Der Query-/bzw x-www-form-urlencodedte String.
     * @return
     */
    public static Map<String, String> parseXWwwFormUrlencoded (String queryString) {
        Map<String, String> result = new HashMap<>();

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
            result.put (
                    UrlDecodingUtils.decodeUrlEncoding (key),
                    UrlDecodingUtils.decodeUrlEncoding (value)
            );

        }
        return result;
    }

    /**
     * Hiermit kann geprüft werden, ob in einer Url überhaupt ein Query String enthalten ist.
     * @param url
     * @return
     */
    public static boolean hasQueryString (String url) {
        return url.contains("?");
    }

    /**
     * Hiermit kann aus einer Url der Query-String, also der String nach dem "?" einer Url gelesen werden.
     * @param url Die gesammte Url/der gesammte Pfad.
     * @return Der Query String.
     */
    public static String getQueryString (String url) {
        // Suche einem Fragezeichen.
        int questionmarkIndex = url.indexOf('?');
        if (questionmarkIndex < 0) { return ""; }
        if (questionmarkIndex + 1 >= url.length()) { return ""; }

        // Wenn eins gefunden wird, dann soll der String an dieser Stelle zerschnitten werden.
        // Der vordere Teil ist dann der eigentliche Pfad.
        // Alles weitere sind Query-Paramter.
        return url.substring (questionmarkIndex + 1).trim ();
    }

    /**
     * Hiermit kann der Query String einer Url entfernt werden.
     * @param url Die gesammte Url/der gesammte Pfad.
     * @return Die URL ohne dem Query String.
     */
    public static String trimQueryString (String url) {
        // Suche einem Fragezeichen.
        int questionmarkIndex = url.indexOf('?');
        if (questionmarkIndex < 0) { return UrlDecodingUtils.decodeUrlEncoding (url); }
        return UrlDecodingUtils.decodeUrlEncoding (url.substring (0, questionmarkIndex));
    }

}
