package de.hsw;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {

    private static SessionManager sessionManager = null;
    public static SessionManager getInstance () {
        if (SessionManager.sessionManager == null) {
            SessionManager.sessionManager = new SessionManager();
        }
        return SessionManager.sessionManager;
    }

    private Map<String, Session> sessions = new HashMap<>();

    private String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private String randomId () {
        try {
            UUID uuid = UUID.randomUUID();
            MessageDigest salt = MessageDigest.getInstance("SHA-256");
            salt.update(uuid.toString().getBytes("UTF-8"));
            System.out.println(salt);
            return this.byteArrayToHex (salt.digest());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return "AAAA";
        }
    }

    private Session createSession (HttpResponse response) {
        String id = this.randomId();
        Session session = new Session (id);
        if (id.equalsIgnoreCase("AAAA")) {
            session.setUnsafe (true);
        }
        this.sessions.put (id, session);
        response.addCookie (new Cookie ("JSESSIONID", id));
        return session;
    }

    public boolean hasSession (String sessionId) {
        return this.sessions.containsKey (sessionId);
    }

    public Session loadOrCreateSession (HttpRequest request, HttpResponse response) {

        if (request.hasHeader("cookie") == false) {
            return this.createSession (response);
        }

        Cookie cookie = Cookie
                            .fromCookieHeader (request.getHeader ("cookie", ""))
                            .stream()
                            .filter(c -> c.getName ().equalsIgnoreCase("JSESSIONID"))
                            .findFirst()
                            .orElse(Cookie.UNSAFE_COOKIE);

        if (this.hasSession (cookie.getValue()) == false) {
            return this.createSession (response);
        }

        return this.sessions.get (cookie.getValue());
    }

    public void kill (Session session) {
        this.sessions.remove (session.getId());
    }

}
