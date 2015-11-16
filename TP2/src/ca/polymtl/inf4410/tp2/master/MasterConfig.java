package ca.polymtl.inf4410.tp2.master;

import ca.polymtl.inf4410.tp2.shared.Config;
import javafx.util.Pair;
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
import java.util.ArrayList;


/**
 * @author Alexandre on 11/12/2015.
 */
public class MasterConfig extends Config {
    private boolean safeMode;
    private String operationFile;
    private ArrayList<Pair<String, String>> servers;
    private String configFile;
    private int rmiPort;

    public static MasterConfig readFromFile(String path) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(path);

        MasterConfig config = new MasterConfig();
        config.safeMode = getBooleanValue(doc, "safeMode");
        config.operationFile = getValue(doc, "operationFile");
        config.configFile = getValue(doc, "configFile");
        config.servers = getPairList(doc, "servers", "host", "name");
        config.rmiPort = getIntegerValue(doc, "rmiPort");

        return config;
    }

    public void writeToFile(String path) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        Document doc = builder.newDocument();
        Element root = doc.createElement("Configuration");
        doc.appendChild(root);

        Element child = doc.createElement("safeMode");
        child.appendChild(doc.createTextNode(safeMode ? "true" : "false"));
        root.appendChild(child);

        child = doc.createElement("operationFile");
        child.appendChild(doc.createTextNode(operationFile));
        root.appendChild(child);

        child = doc.createElement("configFile");
        child.appendChild(doc.createTextNode(operationFile));
        root.appendChild(child);

        child = doc.createElement("rmiPort");
        child.appendChild(doc.createTextNode(Integer.toString(rmiPort)));
        root.appendChild(child);

        child = doc.createElement("servers");
        for (Pair<String, String> server : servers) {
            Element sNode = doc.createElement("server");
            Element hostNode = doc.createElement("host");
            hostNode.appendChild(doc.createTextNode(server.getKey()));
            sNode.appendChild(hostNode);
            Element portNode = doc.createElement("port");
            portNode.appendChild(doc.createTextNode(server.getValue()));
            sNode.appendChild(portNode);
            child.appendChild(sNode);
        }
        root.appendChild(child);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(path));

        transformer.transform(source, result);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != MasterConfig.class) {
            return false;
        }
        MasterConfig cfg = (MasterConfig) obj;

        if (cfg.safeMode != this.safeMode) {
            return false;
        }

        if (!cfg.operationFile.equals(this.operationFile)) {
            return false;
        }

        if (cfg.servers.size() != this.servers.size()) {
            return false;
        }

        if (cfg.rmiPort != this.rmiPort) {
            return false;
        }

        for (Pair<String, String> server : cfg.servers) {
            if (!this.servers.contains(server)){
                return false;
            }
        }

        return true;
    }

    public boolean isSafeMode() {
        return safeMode;
    }

    public void setSafeMode(boolean safeMode) {
        this.safeMode = safeMode;
    }

    public String getOperationFile() {
        return operationFile;
    }

    public void setOperationFile(String operationFile) {
        this.operationFile = operationFile;
    }

    public ArrayList<Pair<String, String>> getServers() {
        return servers;
    }

    public void setServers(ArrayList<Pair<String, String>> servers) {
        this.servers = servers;
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public int getRmiPort() {
        return rmiPort;
    }

    public void setRmiPort(int rmiPort) {
        this.rmiPort = rmiPort;
    }
}
