package de.hsw.examples.surflets;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class TodoItem {

    private String id;
    private boolean completed;
    private String task;

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

    public TodoItem (String task) {
        this (task, false);
    }

    public TodoItem (String task, boolean completed) {
        this.id = randomId();
        this.task = task;
        this.completed = completed;
    }

    public String getId () { return this.id; }
    public boolean isCompleted () { return this.completed; }
    public String getTask () { return this.task; }

    public void toggleCompletion () { this.completed = this.completed == false; }

}
