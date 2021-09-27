package de.hsw.sessions;

import java.text.SimpleDateFormat;
import java.util.*;

public class Cookie {

    // Hier ein Standard-Cookie, der Verwendung findet, wenn keine Session mit einer gegebenen Id gibt.
    public static final Cookie UNSAFE_COOKIE = new Cookie("JSESSIONID", "AAAA");

    private String name;
    private String value;
    private boolean secure = false;
    private boolean httpOnly = false;
    private String sameSite = null;
    private Date expires = null;
    private int maxAge = -1;
    private String domain = null;
    private String path = null;


    /**
     * Hiermit wird ein Cookie erzeugt.
     * @param name Der Name des Cookies.
     * @param value Der Wert des Cookies.
     */
    public Cookie (String name, String value) {
        this.name = name;
        this.value = value;
    }

    // Dies sind die einzigen Werte, die nach der Erstellung vom Client zum Browser gesendet werden.
    // Demensprechend gibt es nur für sie getter.
    public String getName () {
        return this.name;
    }
    public String getValue () {
        return this.value;
    }

    // Für die Variablen "Secure", "HttpOnly", "SameSite", "Expiration", "MaxAge", "Domain" und "Path"
    // gibt es jeweils "nur" Setter, da diese nur für die Erstellung neuer Cookies und dem setzen im Set-Cookie Header
    // notwendig sind, und nicht immer vom Browser mitgeliefert werden.
    // Dementsprechend spielen diese Attribute nur bei der Erstellung eine Rolle und brauchen daher keine Getter.
    public void setName (String name) {
        this.name = name;
    }
    public void setSecure (boolean secure) {
        this.secure = secure;
    }
    public void setHttpOnly (boolean httpOnly) {
        this.httpOnly = httpOnly;
    }
    public void setSameSite (String sameSite) {
        this.sameSite = sameSite;
    }
    public void setExpiration (Date expires) {
        this.expires = expires;
    }
    public void setMaxAge (int maxAge) {
        this.maxAge = maxAge;
    }
    public void setDomain (String domain) {
        this.domain = domain;
    }
    public void setPath (String path) {
        this.path = path;
    }

    /**
     * Hiermit kann die Cookie Header-Zeile geparsed werden.
     * @param cookieHeader Die Header-Zeile in der die Cookies gesetzt wurden.
     * @return Eine Liste an allen Cookies, die in der Header-Zeile aufgelistet werden.
     */
    public static List<Cookie> fromCookieHeader (String cookieHeader) {
        String[] possibleCookies = cookieHeader.split("; ");
        List<Cookie> cookies = new ArrayList<>();
        for (String possibleCookie : possibleCookies) {
            int equalsSignIndex = -1;
            possibleCookie = possibleCookie.trim();
            equalsSignIndex = possibleCookie.indexOf("=");
            if (equalsSignIndex < 0) { continue; }
            cookies.add(
                new Cookie(
                    possibleCookie.substring(0, equalsSignIndex),
                    possibleCookie.substring (equalsSignIndex + 1)
                )
            );

        }
        return cookies;
    }

    /**
     * Hiermit wird der String für den Set-Cookie Header gebaut.
     * @return
     */
    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder();

            // Zunächst der Standard-Anfang eines Cookies <name>=<wert>
            builder.append (this.getName());
            builder.append ("=");
            builder.append (this.getValue());

            // Wenn ein Ablaufdatum gegeben wurde,
            // dann muss dies im Format "<Tag-Name>, dd <Monats-Name> yyyy HH:mm:ss GMT" angebeben werden.

            if (this.expires != null) {
                builder.append ("; Expires=");

                // Formatiere das Datum:
                SimpleDateFormat sdf = new SimpleDateFormat(
                        "E, dd MMM yyyy HH:mm:ss"
                );
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                builder.append(sdf.format(this.expires));
                builder.append(" GMT");
            }

            // Wurde ein Max-Age gebenen, soll dieser beigefügt werden.
            if (this.maxAge >= 0) {
                builder.append ("; Max-Age=");
                builder.append (this.maxAge);
            }

            // Wurde eine Domain gebenen, soll diese beigefügt werden.
            if (this.domain != null) {
                builder.append ("; Domain=");
                builder.append(this.domain);
            }

            // Wurde ein Pfad gebenen, soll dieser beigefügt werden.
            if (this.path != null) {
                builder.append ("; Path=");
                builder.append(this.path);
            }

            // Wurde der Cookie auf "secure" gesetzt, soll dieses Keyword beigefügt werden.
            // PS: hier eigentlich unnötig, da wir eh nicht über HTTPs ausliefern. :D
            if (this.secure == true) {
                builder.append ("; Secure");
            }

            // Wurde der Cookie auf "httpOnly" gesetzt, soll dieses Keyword beigefügt werden.
            if (this.httpOnly == true) {
                builder.append ("; HttpOnly");
            }

            // Wurde eine SameSite Policy gebenen, soll diese beigefügt werden.
            if (this.sameSite != null) {
                builder.append ("; SameSite=");
                builder.append(this.sameSite);
            }

        // Erstelle den Cookie-String für den Set-Cookie Header:
        return builder.toString();
    }
}
