package de.hsw.sessions;

import de.hsw.sessions.helper.DataBox;

import java.util.HashMap;
import java.util.Map;

public class Session {

    private boolean unsafe = false;
    private String id;
    private Map<String, DataBox> data = new HashMap<>();

    public Session (String id) {
        this.id = id;
    }

    /**
     * Hiermit kann ein Integer in der Session gespeichert werden.
     * @param name Name des Feldes.
     * @param data Die zu speichernde Zahl.
     */
    public void put (String name, int data) {
        this.data.put(name, DataBox.fromPrimitive (data));
    }

    /**
     * Hiermit kann ein String in der Session gespeichert werden.
     * @param name Name des Feldes.
     * @param data Der zu speichernde String.
     */
    public void put (String name, String data) {
        this.data.put(name, DataBox.fromPrimitive(data));
    }

    /**
     * Hiermit kann ein beliebiges Objekt in der Session gespeichert werden.
     * @param name Name des Feldes.
     * @param data Das zu speichernde Objekt.
     */
    public<T> void  put (String name, T data) {
        this.data.put(name, DataBox.from (data));
    }

    /**
     * Hiermit kann überprüft werden, ob ein Feld vorhanden ist.
     * @param name Name des Feldes.
     * @return "True", wenn das Feld vorhanden ist, "False" falls nicht.
     */
    public boolean has (String name) {
        return this.data.containsKey (name);
    }

    /**
     * Hiermit wird eine DataBox eines Feldes ausgepackt.
     * @param name Name des Feldes.
     * @param <T> Der return Typ der Variable, die den Wert erwartet.
     * @return Der aus der DataBox ausgepackte Wert im Datentyp des T.
     */
    public<T> T get (String name) {
        return this.data.get(name).get();
    }

    /**
     * Hiermit kann die Id, unter der die Session geführt wird, gelesen werden.
     * @return Die Id, unter der die Session geführt wird.
     */
    public String getId () {
        return this.id;
    }

    /**
     * Hiermit kann eine Session auf "Unsafe" gesetzt werden.
     * Dies ist der Fall, wenn ihre ID = "AAAA".
     * Es bedeutet, dass keine eigene ID vorliegt, die Session also von mehreren Nutzern verwendet worden sein könnte.
     * @param unsafe
     */
    public void setUnsafe (boolean unsafe) {
        this.unsafe = unsafe;
    }
    public boolean isUnsafe () {
        return this.unsafe;
    }
}
