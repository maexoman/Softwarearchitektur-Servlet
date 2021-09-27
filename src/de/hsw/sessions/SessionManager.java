package de.hsw.sessions;

import de.hsw.http.HttpRequest;
import de.hsw.http.HttpResponse;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {

    // Der Session-Manager ist ein Singleton. Dementsprechend bauen wir hier ein Objekt.
    private static SessionManager sessionManager = null;
    public static SessionManager getInstance () {
        if (SessionManager.sessionManager == null) {
            SessionManager.sessionManager = new SessionManager();
        }
        return SessionManager.sessionManager;
    }

    /**
     * Nun ein paar Hilfsfunktionen:
     */

    /**
     * Hiermit wird ein Byte-Array in einen Hex-String umgewandelt.
     * @param bytes Das Byte-Array.
     * @return Die Hex-Representation als String des Byte-Arrays.
     */
    private String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder (bytes.length * 2);
        for (byte b : bytes) {
            sb.append (String.format ("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Hiermit wird eine zufällige Session-Id generiert.
     * @return Eine zufällige Id oder "AAAA", wenn ein Fehler aufkam.
     */
    private String randomId () {
        try {
            MessageDigest hasher = MessageDigest.getInstance("SHA-256");
            hasher.update (UUID.randomUUID ().toString ().getBytes ("UTF-8"));
            return this.byteArrayToHex (hasher.digest());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return "AAAA";
        }
    }

    // In dem Manager wird immer eine Id-Session Verbindung hergestellt.
    private Map<String, Session> sessions = new HashMap<>();

    // Der Konstruktor ist hier private, damit der Manager nur mit "getInstance" als Singleton erstellt werden kann.
    private SessionManager () {}


    /**
     * Hiermit wird eine neue Session erstellt.
     * @param response Die Http-Antwort in die der JSESSIONID Cookie eingepflanzt werden soll.
     * @return Die neu erstellte Session.
     */
    private Session createSession (HttpResponse response) {

        String id = this.randomId();
        Session session = new Session (id);

        // Wenn die Id = "AAAA", dann ist dies keine eindeutige Id und demensprechend nicht "safe-to-use"
        if (id.equalsIgnoreCase("AAAA")) {
            session.setUnsafe (true);
        }

        // Speichere die neue Session im Session-Manager und pflanze den Cookie in die Http-Antwort ein.
        this.sessions.put (id, session);
        response.addCookie (new Cookie("JSESSIONID", id));

        return session;
    }

    /**
     * Hiermit kann der Client überprüfen, ob eine Session im Manager enthalten ist.
     * @param sessionId Die Id der gesuchten Session
     * @return "True", wenn eine Session vorhanden ist, "False" wenn nicht.
     */
    public boolean hasSession (String sessionId) {
        return this.sessions.containsKey (sessionId);
    }

    /**
     * Hiermit kann eine Surflet Instanz eine Session mithilfe des HttpRequests laden, bzw. eine neue Anlegen und
     * an den Client in Form eines Cookies übergeben.
     * @param request Der Http-Request des Browsers.
     * @param response Das Http-Antwort Objekt, dass am Ende an den Browser übermittelt wird.
     * @return Die geladene bzw. neu erstellte Session.
     */
    public Session loadOrCreateSession (HttpRequest request, HttpResponse response) {

        // Wenn kein Cookie vorliegt, muss eine neue Session erstellt werden.
        if (request.hasHeader("cookie") == false) {
            return this.createSession (response);
        }

        // Versuche den Cookie-Header zu parsen und hole anschließend nur den "JSESSIONID" Cookie herraus.
        Cookie cookie = Cookie
                            .fromCookieHeader (request.getHeader ("cookie", ""))
                            .stream()
                            .filter(c -> c.getName ().equalsIgnoreCase("JSESSIONID"))
                            .findFirst()
                            .orElse(Cookie.UNSAFE_COOKIE);

        // Wenn dem Server keine Session mit der im Cookie enthaltenen Id bekannt ist,
        // soll ein neuer Cookie erstellt, und der alte damit überschrieben werden.
        if (
                cookie.getValue().equalsIgnoreCase("AAAA") ||
                this.hasSession (cookie.getValue()) == false
        ) {
            return this.createSession (response);
        }

        // Hier angekommen gibt es eine Session mit dieser Id.
        // Sie soll hier also zurückgegeben werden:
        return this.sessions.get (cookie.getValue());
    }

    /**
     * Hiermit kann eine aktive Session zerstört werden.
     * @param session Die Session, die es zu zerstören gilt.
     * @param response Die Http-Antwort mit der der Cookie auch im Browser gelöscht werden soll.
     */
    public void kill (Session session, HttpResponse response) {
        response.removeCookie ("JSESSIONID");
        this.sessions.remove (session.getId ());
    }
}
