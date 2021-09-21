package de.hsw;

import java.text.SimpleDateFormat;
import java.util.*;

public class Cookie {

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


    public Cookie (String name, String value) {
        this.name = name;
        this.value = value;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getName () {
        return this.name;
    }

    public String getValue () {
        return this.value;
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

    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder();
            builder.append (this.getName());
            builder.append ("=");
            builder.append (this.getValue());

            if (this.expires != null) {
                builder.append ("; Expires=");

                SimpleDateFormat sdf = new SimpleDateFormat(
                        "E, dd MMM yyyy HH:mm:ss"
                );
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                builder.append(sdf.format(this.expires));
                builder.append(" GMT");
            }

            if (this.maxAge >= 0) {
                builder.append ("; Max-Age=");
                builder.append (this.maxAge);
            }

            if (this.domain != null) {
                builder.append ("; Domain=");
                builder.append(this.domain);
            }

            if (this.path != null) {
                builder.append ("; Path=");
                builder.append(this.path);
            }

            if (this.secure == true) {
                builder.append ("; Secure");
            }

            if (this.httpOnly == true) {
                builder.append ("; HttpOnly");
            }

            if (this.sameSite != null) {
                builder.append ("; SameSite=");
                builder.append(this.sameSite);
            }

        return builder.toString();
    }
}
