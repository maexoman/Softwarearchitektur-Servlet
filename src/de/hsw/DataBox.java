package de.hsw;

public class DataBox {

    private Class type;
    private Object data;

    private DataBox (Class type, Object data) {
        this.type = type;
        this.data = data;
    }



    public static DataBox fromPrimitive (int data) {
        return new DataBox(Integer.class, data);
    }

    public static DataBox fromPrimitive (String data) {
        return new DataBox(String.class, data);
    }

    public static DataBox fromPrimitive (byte data) {
        return new DataBox(Byte.class, data);
    }

    public static DataBox fromPrimitive (boolean data) {
        return new DataBox(Boolean.class, data);
    }

    public static<T> DataBox from (T data) {
        return new DataBox(data.getClass(), data);
    }

    public<T> T get () {
        return (T) this.data;
    }

}
