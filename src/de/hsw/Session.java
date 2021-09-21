package de.hsw;

import java.util.HashMap;
import java.util.Map;

public class Session {

    private boolean unsafe = false;
    private Map<String, DataBox> data = new HashMap<>();
    private String id;

    public Session (String id) {
        this.id = id;
    }


    public void put (String name, int data) {
        this.data.put(name, DataBox.fromPrimitive (data));
    }

    public void put (String name, String data) {
        this.data.put(name, DataBox.fromPrimitive(data));
    }

    public boolean has (String name) {
        return this.data.containsKey (name);
    }

    public<T> T get (String name) {
        return this.data.get(name).get();
    }

    public String getId () {
        return this.id;
    }


    public void setUnsafe (boolean unsafe) {
        this.unsafe = unsafe;
    }
    public boolean isUnsafe () {
        return this.unsafe;
    }
}
