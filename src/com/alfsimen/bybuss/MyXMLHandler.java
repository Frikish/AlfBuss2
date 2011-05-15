package com.alfsimen.bybuss;

import android.os.StrictMode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: alf
 * Date: 5/13/11
 * Time: 10:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyXMLHandler extends DefaultHandler {
    Boolean currentElement = false;
    String currentValue = null;
    Holdeplass plass;
    private ArrayList<Holdeplass> list;

    public ArrayList<Holdeplass> getHoldeplassList() {
        return list;
    }

    public void setHoldeplassList(ArrayList<Holdeplass> list) {
        this.list = list;
    }

    @Override
    public void startElement(String url, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = true;

        if(localName.equals("osm")) {
            list = new ArrayList<Holdeplass>();
        }
        else if(localName.equals("node")) {
            plass = new Holdeplass();
            plass.setLatitude(Double.parseDouble(attributes.getValue("lat")));
            plass.setLongtitude(Double.parseDouble(attributes.getValue("lon")));
            plass.setName(attributes.getValue("v"));
        }
    }

    @Override
    public void endElement(String url, String localName, String qName) throws SAXException {
        currentElement = false;
        if(localName.equalsIgnoreCase("node")) {
            list.add(plass);
        }
    }
}
