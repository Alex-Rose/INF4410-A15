package ca.polymtl.inf4410.tp2.shared;

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

}
