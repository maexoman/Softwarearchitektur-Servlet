package de.hsw;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UnsafeHttpInputStreamReader {

    public static final byte CHAR_CR = 0x0D;
    public static final byte CHAR_LF = 0x0A;

    private List<Byte> buffer = new ArrayList<>(512);
    private InputStream inputStream;

    public UnsafeHttpInputStreamReader (InputStream inputStream) {
        this.inputStream = inputStream;
    }

    private byte[] getByteArrayFromInternalBuffer () {
        int bufferSize = this.buffer.size();
        Iterator<Byte> iterator = this.buffer.iterator();
        byte[] result = new byte[bufferSize];
        int index = 0;
        while (iterator.hasNext()) {
            result [index] = iterator.next();
            index += 1;
        }
        return result;
    }

    public String readLineAsUSASCII () throws IOException {
        byte[] requestLineAsByteArray = this.readUntilCRLF ();
        return new String (requestLineAsByteArray, StandardCharsets.US_ASCII);
    }

    /**
     * Hiermit kann eine, nach dem RFC-2616 kodierte Zeile eines HTTP-Requests gelesen werden.
     * @return Die gelesenen Bytes.
     * @throws IOException
     */
    public byte [] readUntilCRLF () throws IOException {

        boolean foundCR = false;
        int character;
        byte characterAsByte;

        // zum Lesen muss zunächst der Buffer gelöscht werden:
        this.buffer.clear();

        // Es soll so lange gelesen werden, bis "\r\n" gefunden wird.
        while (true) {

            // Hole das Byte aus dem Stream:
            character = this.inputStream.read();

            // Ist der Character -1, ist dies wie .readLine () == null.
            // Der InputStream wurde also vom Client geschlossen.
            if (character == -1) {
                throw new IOException();
            }

            characterAsByte = (byte) character;

            // Wenn foundCR an dieser Stelle den boolschen Wert "true" hat,
            // dann war das letzte Zeichen ein "\r".
            // Es kann sein, dass das "\r" alleine stehen sollte, oder nun ein "\n" folgt.
            // Dafür muss nun geprüft werden ob ein "\n" vorliegt.
            // Andernfalls soll "\r" zum Buffer hinzugefügt werden.
            if (foundCR == true) {
                // Wenn das jetzige Zeichen "\n" ist, wurde ein "\r\n", also CRLF, gefunden.
                // Das Lesen kann nun also abgebrochen werden.
                if (characterAsByte == CHAR_LF) {
                    break;
                }

                // Hier angekommen wurde kein CRLF gefunden.
                // Es soll "\r" also zum Buffer hinzugefügt, und weitergesucht werden.
                foundCR = false;
                this.buffer.add(CHAR_CR);
            }

            // Wenn "\r" gefunden wird, dann soll dies für das nächste Zeichen vorgemerkt werden.
            if (characterAsByte == CHAR_CR) {
                foundCR = true;
                continue;
            }

            // Hier angekommen handelt es sich bei dem Zeichen nicht um ein "\r",
            // also soll es zum Buffer hinzugefügt werden.
            this.buffer.add(characterAsByte);

        }
        return this.getByteArrayFromInternalBuffer ();
    }

    public byte[] readBytes (int bytes) throws IOException {
        int character;

        // zum Lesen muss zunächst der Buffer gelöscht werden:
        this.buffer.clear();

        for (int readBytes = 0; readBytes < bytes; readBytes += 1) {
            character = this.inputStream.read ();
            if (character == -1) {
                break;
            }
            this.buffer.add ((byte) character);
        }

        return this.getByteArrayFromInternalBuffer ();
    }
}
