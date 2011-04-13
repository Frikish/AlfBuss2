/**
 *
 * Denne klassen leser og returnerer et ArrayList med alle holdeplassene fra XML-fila
 *
 * Bussholdeplassene hentes fra OpenStreetMap
 *
 * @author Tri M. Nguyen
 *
 */

package com.alfsimen.bybuss;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class XmlParser {
    private String filename = "/res/raw/holdeplasser.xml";
    private Document dom;
    private ArrayList<Holdeplass> list = new ArrayList<Holdeplass>();
    private DocumentBuilderFactory dbf;
    private InputStream xmlFile;


    public XmlParser() {
        parseXml();
        parseDocument();
    }


    public XmlParser(InputStream io) {
        xmlFile = io;
        parseXml();
        parseDocument();
    }

    public void parseXml() {
        dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
//			dom = db.parse(urlConn.getInputStream());
            dom = db.parse(xmlFile);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTest() {
        return filename;
    }

    public void parseDocument() {
        // root element
        Element docEle = dom.getDocumentElement();

        // get nodelist of elements
        NodeList nl = docEle.getElementsByTagName("node");

        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {
                // get the employee element
                Element el = (Element) nl.item(i);

                // get the Employee object
                Holdeplass h = getHoldeplass(el);

                // add it to list
                list.add(h);
            }
        }
    }

    private Holdeplass getHoldeplass(Element elem) {
        double lat = Double.parseDouble(elem.getAttribute("lat"));
        double lon = Double.parseDouble(elem.getAttribute("lon"));

        NodeList nodeList = elem.getElementsByTagName("tag");

        String name = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (((Element) nodeList.item(i)).getAttribute("k").equals("name")) {
                name = ((Element) nodeList.item(i)).getAttribute("v");
            }
        }

        Holdeplass h = new Holdeplass(name, lat, lon);
        return h;
    }

    public ArrayList<Holdeplass> getHoldeplasser() {
        return list;
    }
}