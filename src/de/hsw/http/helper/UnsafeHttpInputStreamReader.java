package de.hsw.http.helper;

import de.hsw.errors.ConnectionClosedException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UnsafeHttpInputStreamReader {

    // Definiere die NewLine-Character
    public static final byte CHAR_CR = 0x0D;
    public static final byte CHAR_LF = 0x0A;

    // Der Interne-Buffer als Array-Liste, damit er dynamisch erweitert werden kann.
    private List<Byte> buffer = new ArrayList<>(512);

    // Der Stream, der zum Lesen verwendet wrid.
    private InputStream inputStream;

    public UnsafeHttpInputStreamReader (InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Hiermit wird eine Line im US-ASCII aus dem Stream gelesen.
     * Das US-ASCII Format wird vor allem in der Request-Line und den Header-Lines verwendet.
     * @return Die Line als US-ASCII String.
     * @throws ConnectionClosedException
     */
    public String readLineAsUSASCII () throws ConnectionClosedException {
        byte[] requestLineAsByteArray = this.readUntilCRLF ();
        return new String (requestLineAsByteArray, StandardCharsets.US_ASCII);
    }

    /**
     * Hiermit kann eine, nach dem RFC-2616 kodierte Zeile eines HTTP-Requests gelesen werden.
     * Da der PrintReader lediglich bis LF ließt, könnte es "false" positives geben.
     * Wir benötigen jedoch ein CRLF, dementsprechend lesen wir hier die Bytes einzeln bis CRLF
     * @return Die gelesenen Bytes.
     * @throws ConnectionClosedException
     */
    public byte[] readUntilCRLF () throws ConnectionClosedException {

        boolean foundCR = false;
        int character;
        byte characterAsByte;

        // zum Lesen muss zunächst der Buffer gelöscht werden:
        this.buffer.clear();

        // Es soll so lange gelesen werden, bis "\r\n" gefunden wird.
        while (true) {

            // Hole das Byte aus dem Stream:
            try {
                character = this.inputStream.read();
            } catch (IOException e) {
                throw new ConnectionClosedException ();
            }

            // Ist der Character -1, ist dies wie .readLine () == null.
            // Der InputStream wurde also vom Client geschlossen.
            if (character == -1) {
                throw new ConnectionClosedException ();
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
        return ByteUtils.byteArrayFromDynamicBuffer (this.buffer);
    }

    /**
     * Hiermit kann eine gegeben Anzahl an Bytes gelesen werden.
     * Dies ist besonders für das Lesen des Request-Bodys interessant.
     * @param bytes Die Anzahl an Bytes, die gelesen werden sollen.
     * @return Die gelesenen Bytes.
     * @throws ConnectionClosedException
     */
    public byte[] readBytes (int bytes) throws ConnectionClosedException {
        int character;

        // zum Lesen muss zunächst der Buffer gelöscht werden:
        this.buffer.clear();

        for (int readBytes = 0; readBytes < bytes; readBytes += 1) {

            // Hole das Byte aus dem Stream:
            try {
                character = this.inputStream.read();
            } catch (IOException e) {
                throw new ConnectionClosedException ();
            }

            // Wenn der Character == -1, dann ist der Stream geschlossen bzw. keine weiteren Daten vorhanden
            // In diesem Fall könnte ein Fehler geworfen werden.
            // Wir haben uns aber dazu entschieden die Daten bis dahin zurückzugeben.
            if (character == -1) {
                break;
            }

            this.buffer.add ((byte) character);
        }

        return ByteUtils.byteArrayFromDynamicBuffer (this.buffer);
    }
}
