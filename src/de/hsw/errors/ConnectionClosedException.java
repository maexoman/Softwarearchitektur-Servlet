package de.hsw.errors;

/**
 * Dies ist ein eigener Fehler, damit im Catch-Block des Servers darauf geschaut werden kann.
 * Hiermit kann signalisiert werden, dass der Client die Verbindung geschlossen hat (was ihm nach RFC2616 zusteht).
 * Da dies ohne vorwahnung geschehen kann, wäre eine einfache IOException,
 * wie sie sonst geworfen werden würde nicht hilfreich.
 */
public class ConnectionClosedException extends Exception {}