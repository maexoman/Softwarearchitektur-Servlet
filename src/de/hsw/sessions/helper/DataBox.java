package de.hsw.sessions.helper;

public class DataBox {

    private Class type;
    private Object data;

    /**
     * Die DataBox speichert einen Wert und den dazugehörigen Klassen-Typ.
     * @param type Der Typ des Objekts.
     * @param data Das zu speichernde Objekt.
     */
    private DataBox (Class type, Object data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Hiermit kann eine DataBox aus einem Integer erstellt werden.
     * @param data Der zu speichernde Integer.
     * @return Die DataBox, die den Integer Wert entählt.
     */
    public static DataBox fromPrimitive (int data) {
        return new DataBox(Integer.class, data);
    }

    /**
     * Hiermit kann eine DataBox aus einem String erstellt werden.
     * @param data Der zu speichernde String.
     * @return Die DataBox, die den String Wert entählt.
     */
    public static DataBox fromPrimitive (String data) {
        return new DataBox(String.class, data);
    }

    /**
     * Hiermit kann eine DataBox aus einem Byte erstellt werden.
     * @param data Das zu speichernde Byte.
     * @return Die DataBox, die den Byte Wert entählt.
     */
    public static DataBox fromPrimitive (byte data) {
        return new DataBox(Byte.class, data);
    }

    /**
     * Hiermit kann eine DataBox aus einem Boolean erstellt werden.
     * @param data Der zu speichernde Boolean.
     * @return Die DataBox, die den Boolean Wert entählt.
     */
    public static DataBox fromPrimitive (boolean data) {
        return new DataBox(Boolean.class, data);
    }

    /**
     * Hiermit kann eine DataBox aus einem Objekt erstellt werden.
     * @param data Das zu speichernde Objekt.
     * @return Die DataBox, die das Objekt entählt.
     */
    public static<T> DataBox from (T data) {
        return new DataBox(data.getClass(), data);
    }

    /**
     * Hiermit kann eine DataBox wieder entpackt werden.
     * @param <T> Der Typ des gewünschten Ausgabe objekts.
     * @return Das gespeicherte Objekt im Typ T.
     */
    public<T> T get () {
        return (T) this.data;
    }

    /**
     * Hiermit kann der Typ einer DataBox ermittelt werden.
     * @return Der Typ des in der DataBox gespeicherten Objekts.
     */
    public Class getType () {
        return this.type;
    }

}
