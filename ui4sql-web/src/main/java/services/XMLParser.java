/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */
package services;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/*******************************************************************************
 * 
 * @author PAULSEAR
 * 
 * Class for parsing an XML document
 * 
 * Constructor takes : XML string, tagname of the parent element
 * 
 * then use method parseString (elementName
 * 
 * 9/16 allow parsing of multiple nodes, constructor sets up the first node ..
 * then use getElementNo(i) to parse other nodes in the tagname
 * 
 * 
 */
public class XMLParser {

	private Element element = null; // the current sub-element

	private Element documentElement = null; // the element with all the nodes

	private NodeList nodes = null;

	private Document document = null;

	private boolean success = false;

	private int elementCount = 0;

	// Construct problem object from the Remedy XML string

	public XMLParser(String xml, String tagname) {

		success = false;
		if (xml.length() < 1) {
			return;
		}

		document = getDocument(xml);

		if (document == null) {
			return;
		}
		documentElement = document.getDocumentElement();

		element = getElementZero(tagname);

		success = true;

	}

	public boolean getSuccess() {
		return success;
	}

	public int getCount() {
		return elementCount;
	}

	private Document getDocument(String xmlString) {
		// get the factory

		if (xmlString.length() < 1) {
			return document;
		}

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// parse using builder to get DOM representation of the XML file

			InputSource inStream = new InputSource();
			inStream.setCharacterStream(new StringReader(xmlString));

			document = db.parse(inStream);

		} catch (ParserConfigurationException pce) {
			debug("parseXMLFile.. ParserConfigurationException.");
			pce.printStackTrace();
		} catch (SAXException se) {
			debug("parseXMLFile.. SaxException.");
			se.printStackTrace();
		} catch (IOException ioe) {
			debug("parseXMLFile.. IOException.");
			ioe.printStackTrace();
		}

		return document;
	}

	public int getElementCount(String tagname) {

		// get a nodelist of <employee> elements

		return element.getElementsByTagName(tagname).getLength();
	}

	
	public int getElementCount() {

		// get a nodelist of <employee> elements

		return elementCount;
	}

	
	public void setElementNo(int i) {
		// set the element to zero-based (0 = first) node


		if (nodes != null && nodes.getLength() >= i -1 ) {

			element = (Element) nodes.item(i);
		}

		
	}

	private Element getElementZero(String tagname) {

		// get a nodelist of <employee> elements
		nodes = documentElement.getElementsByTagName(tagname);

		elementCount = nodes.getLength();

		Element el = null;

		if (nodes != null && nodes.getLength() > 0) {

			el = (Element) nodes.item(0);
		}

		return el;

	}

	public String parseTextValue(String tagName) {

		try {
			String textVal = "";
			NodeList nl = element.getElementsByTagName(tagName);
			if (nl != null && nl.getLength() > 0) {
				Element el = (Element) nl.item(0);
				textVal = el.getFirstChild().getNodeValue();
			}

			return textVal;
		} catch (Exception ex) {
			return "";
		}

	}

	private void debug(String s) {
		System.out.println(s);
	}

}
