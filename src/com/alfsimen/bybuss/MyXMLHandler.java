package com.alfsimen.bybuss;

import android.os.StrictMode;
import android.view.ViewDebug;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

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
            plass.setId(Integer.parseInt(attributes.getValue("id")));
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
