package de.hsw;

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

    public static SurfletMapper loadFromFile (String filePath) {
        SurfletMapper mapper = new SurfletMapper();

        String classPath = "/";

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File (filePath));
            doc.getDocumentElement().normalize();

            NodeList classPaths = doc.getDocumentElement().getElementsByTagName("classPath");
            for (int index = 0; index < classPaths.getLength(); index += 1) {
                Node node = classPaths.item (index);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element element = (Element) node;
                if (element.hasAttributes() == false) {
                    throw new Exception("Invalid structure");
                }
                classPath = element.getAttribute("path");
            }

            System.out.println(classPath);

            String absolutePath = new File(classPath).getAbsolutePath();
            URL url = new URL(new URL("file:"), absolutePath);
            ClassLoader loader = new URLClassLoader(new URL[] {url});

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

                String surfletId = element.getAttribute("id");
                String qualifyingClassName = element.getAttribute("class");

                Class loaded = loader.loadClass(qualifyingClassName);

                boolean hasSurfletInterface = false;

                Class[] interfaces = loaded.getInterfaces();
                for (int i = 0; i < interfaces.length; i += 1) {
                    if (interfaces[i].isAssignableFrom(Surflet.class)) {
                        hasSurfletInterface = true;
                        break;
                    }
                }

                if (hasSurfletInterface == false) {
                    throw new Exception("The Provided class is not a surflet");
                }

                Surflet surflet = (Surflet) loaded.getDeclaredConstructor().newInstance();
                mapper.registerSurflet(surfletId, surflet);
            }

            NodeList mappings = doc.getDocumentElement().getElementsByTagName("mapping");
            for (int index = 0; index < mappings.getLength(); index += 1) {

                Node node = mappings.item(index);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element element = (Element) node;

                String methods = element.getAttribute("methods");
                String path = element.getAttribute("path");
                String surfletId = element.getAttribute("surfletId");

                if (mapper.hasSurflet (surfletId) == false) {
                    throw new Exception("Das Mapping auf das Surflet mit der Id \"" + surfletId + "\" ist unzulässig, da diese Id noch unbekannt ist.");
                }

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

    private Map<String, Surflet> surflets = new HashMap<>();
    private Map<String, String> pathMapping = new HashMap<>();

    private SurfletMapper () {
        this.registerSurflet("$default", new Error404Surflet());
        this.createMapping("ALL", "default", "$default");
    }

    public void registerSurflet (String surfletId, Surflet surflet) {
        this.surflets.put(surfletId, surflet);
    }

    public boolean hasSurflet (String surfletId) {
        return this.surflets.containsKey (surfletId);
    }

    public void createMapping (String method, String path, String surfletId) {
        if (method.equalsIgnoreCase("ALL") == true) {
            HttpRequest
                    .getSupportedRequestMethods()
                    .stream().forEach(m -> SurfletMapper.this.pathMapping.put(m + "-" + path, surfletId));
        }
        this.pathMapping.put(method + "-" + path, surfletId);
    }

    public Surflet resolveSurflet (HttpRequest.RequestMethod method, String path) {
        String key = method.toString () + "-" + path;
        if (this.pathMapping.containsKey(key) == false) {
            key = "GET-default";
        }
        return this.surflets.get (
            this.pathMapping.get(key)
        );
    }

    @Override
    public String toString() {
        return "SurfletMapper{" +
                "surflets=" + surflets +
                ", pathMapping=" + pathMapping +
                '}';
    }
}
