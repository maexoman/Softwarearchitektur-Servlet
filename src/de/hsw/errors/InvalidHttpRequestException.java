package de.hsw.errors;

/**
 * Dies ist ein eigener Fehler, damit im Catch-Block des Servers darauf geschaut werden kann.
 * Hiermit kann signalisiert werden, dass der Client eine fehlerhafte Http-Anfrage gestellt hat.
 * In diesem Fall soll der Server die Verbindung mit dem Client strikt beenden.
 */
public class InvalidHttpRequestException extends Exception {
    public InvalidHttpRequestException (String message) {
        super (message);
    }
}
