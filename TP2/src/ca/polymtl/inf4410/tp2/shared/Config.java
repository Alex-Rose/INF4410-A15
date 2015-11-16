package ca.polymtl.inf4410.tp2.shared;

import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * @author Alexandre on 11/12/2015.
 */
public abstract class Config {
    protected static String getValue(Document doc, String name) {
        Node node = doc.getElementsByTagName(name).item(0);
        return node.getTextContent();
    }

    protected static boolean getBooleanValue(Document doc, String name) {
        Node node = doc.getElementsByTagName(name).item(0);
        return Boolean.parseBoolean(node.getTextContent());
    }

    protected static ArrayList<String> getStringList(Document doc, String name) {
        Node node = doc.getElementsByTagName(name).item(0);

        NodeList children = node.getChildNodes();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            list.add(child.getTextContent());
        }

        return list;
    }

    protected static ArrayList<Pair<String, String>> getPairList(Document doc, String name, String item1, String item2) {
        Node node = doc.getElementsByTagName(name).item(0);

        NodeList children = node.getChildNodes();
        ArrayList<Pair<String, String>> list = new ArrayList<>();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            NodeList params = child.getChildNodes();
            String host = null;
            String objname = null;
            for (int j = 0; j < params.getLength(); j++){
                if (params.item(j).getNodeName().equals(item1)) {
                    host = params.item(j).getTextContent();
                } else {
                    objname = params.item(j).getTextContent();
                }
            }
            list.add(new Pair<String, String>(host, objname));
        }

        return list;
    }

    protected static int getIntegerValue(Document doc, String name) {
        Node node = doc.getElementsByTagName(name).item(0);
        return Integer.parseInt(node.getTextContent());
    }

}
