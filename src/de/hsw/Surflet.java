package de.hsw;

import de.hsw.http.HttpRequest;
import de.hsw.http.HttpResponse;

/**
 * Dieses Interface muss von den jeweiligen Surflets implementiert werden.
 */
public interface Surflet {
    void handleRequest (HttpRequest request, HttpResponse response) throws Exception;
}
