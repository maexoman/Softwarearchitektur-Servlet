package de.hsw.server.configs;

import de.hsw.Surflet;
import de.hsw.http.HttpRequest;
import de.hsw.surflets.Error404Surflet;
import de.hsw.surflets.StaticFileSurflet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SurfletMapper {

    /**
     * Hiermit kann aus einer beliebigen Config-XML-Datei das Mapping der Surflets gelesen werden.
     * @param filePath Der Pfad zu der Config-XML-Datei.
     * @return Gibt den aus der Config-XML-Datei resultierenden Surflet-Mapper zurück.
     */
    public static SurfletMapper loadFromFile (String filePath) {

        // Der Initiale Mapper, der nach und nach mit Inhalt gefüllt wird:
        SurfletMapper mapper = new SurfletMapper();

        // Der Standard-Pfad, von denen die angegebenen Fully-Qualifying-Class-Names interpretiert,
        // und die jeweiligen Klassen gelesen werden.
        String classPath = "/";

        try {

            // Erstelle einen XML-Dokument-Parser:
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();

            // Lese das XML-Dokument vom angegebenen Dateipfad:
            Document doc = db.parse (new File (filePath));
            doc.getDocumentElement().normalize();

            // Zunächst soll der gewünschte ClassPath (siehe oben) evaluiert werden:
            // Dafür werden alle Elemente mit dem Tag-Namen classPath gelesen und durchgegangen.
            // Diese müssen ein Attribut "path" enthalten.
            // Nur das letzte im XML-Dokument auftauchende Objekt wird berücksichtigt.
            NodeList classPaths = doc.getDocumentElement().getElementsByTagName("classPath");
            for (int index = 0; index < classPaths.getLength(); index += 1) {

                // Schau ob es sich um ein gültiges Element-Node handelt:
                Node node = classPaths.item (index);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element element = (Element) node;
                if (element.hasAttributes() == false) {
                    throw new Exception("Invalid structure");
                }

                // Hole den ClassPath aus dem Path Attribut des XML-Elements:
                classPath = element.getAttribute("path");
            }

            // Baue nun den Class-Loader mithilfe des ermittelten ClassPaths:
            String absolutePath = new File(classPath).getPath();
            URL url = new URL(new URL("file:"), absolutePath);
            ClassLoader loader = new URLClassLoader(new URL[] {url});

            // Nun wird zunächst eine Map mit einer Id für ein Surflet und der damit verbundenen Klasse erzeugt.
            // Dafür werden alle "surflet" Objekte gelesen und die Attribute "id" und "class" verarbeitet,
            // und in der Map gespeichert.
            NodeList surflets = doc.getDocumentElement().getElementsByTagName("surflet");
            for (int index = 0; index < surflets.getLength(); index += 1) {
                Node node = surflets.item(index);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element element = (Element) node;
                if (element.hasAttributes() == false) {
                    throw new Exception("Invalid structure");
                }

                // Hole die jeweiligen Attribute:
                String surfletId = element.getAttribute("id");
                String qualifyingClassName = element.getAttribute("class");

                // Lade die Klasse mithilfe des Class-Loaders und dem Fully-Qualifying-Class-Name:
                Class loaded = loader.loadClass(qualifyingClassName);

                // Nun soll sichergestellt werden, dass die geladene Klasse auch das Interface "Surflet" implementiert:
                boolean hasSurfletInterface = false;
                Class[] interfaces = loaded.getInterfaces();
                for (int i = 0; i < interfaces.length; i += 1) {
                    if (interfaces[i].isAssignableFrom(Surflet.class)) {
                        hasSurfletInterface = true;
                        break;
                    }
                }

                // Ist dies nicht der Fall, so soll hier ein Fehler geworfen werden:
                if (hasSurfletInterface == false) {
                    throw new Exception("The Provided class is not a surflet");
                }

                // Hier angekommen ist alles i. O.
                // Es kann also das Mapping erstellt werden:
                mapper.registerSurflet (surfletId, loaded);
            }

            // Anschließend sollen die Mappings zwischen Methode, Pfad und SurfletId hergestellt werden.
            // Dafür werden die Mapping-Elemente gelesen und durchgegangen:
            NodeList mappings = doc.getDocumentElement().getElementsByTagName("mapping");
            for (int index = 0; index < mappings.getLength(); index += 1) {
                Node node = mappings.item(index);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element element = (Element) node;
                if (element.hasAttributes() == false) {
                    throw new Exception("Invalid structure");
                }

                // Hole die jeweiligen Attribute:
                String methods = element.getAttribute("methods");
                String path = element.getAttribute("path");
                String surfletId = element.getAttribute("surfletId");

                // Wenn noch kein Surflet mit der gefundenen Id existiert, soll ein Fehler geworfen werden:
                if (mapper.hasSurflet (surfletId) == false) {
                    throw new Exception("Das Mapping auf das Surflet mit der Id \"" + surfletId + "\" ist unzulässig, da diese Id noch unbekannt ist.");
                }

                // In dem Attribut "methods" können prinzipiell mehrere Methoden definiert werden (mit "," getrennt)
                // Das Mapping soll dann für alle Methoden-Pfad Ausprägungen hinzugefügt werden.
                Arrays
                        .stream(methods.split(","))
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .filter(method -> HttpRequest.getSupportedRequestMethods(true).contains (method))
                        .forEach(method -> mapper.createMapping (method, path, surfletId));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapper;
    }

    // Hier werden die Surflets mit ihrer Id und die Mappings dazu gespeichert:
    private Map<String, Class> surflets = new HashMap<>();
    private Map<String, String> pathMapping = new HashMap<>();

    private SurfletMapper () {
        this.registerSurflet("$default", (Class) Error404Surflet.class);
        this.registerSurflet("$file", (Class) StaticFileSurflet.class);
        this.createMapping("ALL", "default", "$default");
    }

    /**
     * Hiermit kann ein Surflet unter einer bestimmten Id regestriert werden.
     * @param surfletId Die Id des Surflets.
     * @param surflet
     */
    public void registerSurflet (String surfletId, Class surflet) {
        this.surflets.put(surfletId, surflet);
    }

    public boolean hasSurflet (String surfletId) {
        return this.surflets.containsKey (surfletId);
    }

    /**
     * Hiermit kann ein neues Mapping hinzugefügt werden:
     * @param method Die Methode, unter der das Mapping hinzugefügt werden soll.
     * @param path Der Pfad für das Surflet.
     * @param surfletId Die Id des aufzurufenden Surflets.
     */
    public void createMapping (String method, String path, String surfletId) {

        // Wenn als Methode der Spezialfall "ALL" angegeben wurde,
        // dann soll ein Mapping für alle ausprägungen hinzugefügt werden.
        if (method.equalsIgnoreCase("ALL") == true) {
            HttpRequest
                    .getSupportedRequestMethods()
                    .stream()
                    .forEach(m -> SurfletMapper.this.createMapping(m, path, surfletId));
            return;
        }

        // Hier wird das Mapping tatsächlich erstellt.
        this.pathMapping.put(method + "-" + path, surfletId);
    }

    /**
     * Hiermit kann eine Pfad-Methoden Kombination zu einem Surflet aufgeläst werden.
     * Wenn nichts zu dieser Kombination gefunden wird, dann wird das Standard Surflet zurückgegeben.
     * @param method Die Http-Methode des Aufrufs.
     * @param path Der Pfad des Aufrufs.
     * @return Das Surflet, dass für die Bearbeitung dieser Anfrage zuständig ist.
     */
    public Surflet resolveSurflet (HttpRequest.RequestMethod method, String path) {
        String key = method.toString() + "-" + path;

        // Wenn zu dieser Kombination kein Surflet gefunden werden kann, dann wird der Key zum default Surflet gemappt.
        if (this.pathMapping.containsKey(key) == false) {
            key = "GET-default";
        }

        // Hole das zuständige Surflet über die jeweiligen Maps.
        try {
            return (Surflet) this.surflets.get(
                    this.pathMapping.get(key)
            ).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return new Error404Surflet();
        }

    }
}
