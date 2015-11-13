package ca.polymtl.inf4410.tp2.worker;

import ca.polymtl.inf4410.tp2.shared.Config;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

/**
 * @author Alexandre on 11/12/2015.
 */
public class WorkerConfig extends Config {

    private String name;
    private String configFile;
    private int port;
    private int capacity;
    private int malice;

    public static WorkerConfig readFromFile(String path) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(path);

        WorkerConfig config = new WorkerConfig();
        config.name = getValue(doc, "name");
        config.port = getIntegerValue(doc, "port");
        config.capacity = getIntegerValue(doc, "capacity");
        config.malice = getIntegerValue(doc, "malice");

        return config;
    }

    public void writeToFile(String path) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        Document doc = builder.newDocument();
        Element root = doc.createElement("Configuration");
        doc.appendChild(root);

        Element child = doc.createElement("name");
        child.appendChild(doc.createTextNode(name));
        root.appendChild(child);

        child = doc.createElement("port");
        child.appendChild(doc.createTextNode(Integer.toString(port)));
        root.appendChild(child);

        child = doc.createElement("capacity");
        child.appendChild(doc.createTextNode(Integer.toString(capacity)));
        root.appendChild(child);

        child = doc.createElement("malice");
        child.appendChild(doc.createTextNode(Integer.toString(malice)));
        root.appendChild(child);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(path));

        transformer.transform(source, result);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != WorkerConfig.class) {
            return false;
        }
        WorkerConfig cfg = (WorkerConfig) obj;

        if (!cfg.name.equals(this.name)) {
            return false;
        }

        if (cfg.port != this.port) {
            return false;
        }

        if (cfg.capacity != this.capacity) {
            return false;
        }

        if (cfg.malice != this.malice) {
            return false;
        }

        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getMalice() {
        return malice;
    }

    public void setMalice(int malice) {
        this.malice = malice;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }
}
