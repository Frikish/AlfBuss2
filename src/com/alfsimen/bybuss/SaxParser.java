package com.alfsimen.bybuss;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: alf
 * Date: 5/13/11
 * Time: 9:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class SaxParser {
    private String filename = "/res/raw/holdeplasser.xml";
    private ArrayList<Holdeplass> list = new ArrayList<Holdeplass>();
    private SAXParserFactory spf;
    private SAXParser sp;
    private XMLReader xr;
    private InputStream xmlFile;

    public void SaxParser(InputStream io) {
        xmlFile = io;
        parseXML();
        parse();
    }

    public void parseXML() {
        spf = SAXParserFactory.newInstance();
        try {
            sp = spf.newSAXParser();
            xr = sp.getXMLReader();
        }
        catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        catch (SAXException f) {
            f.printStackTrace();
        }
    }

    public void parse() {
        MyXMLHandler myXMLHandler = new MyXMLHandler();
        myXMLHandler.setHoldeplassList(list);
        xr.setContentHandler(myXMLHandler);
        try {
            xr.parse(new InputSource(xmlFile));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (SAXException f) {
            f.printStackTrace();
        }
        //myXMLHandler.getHoldeplasList();
    }

    public ArrayList<Holdeplass> getHoldeplasser() {
        return list;
    }
}
