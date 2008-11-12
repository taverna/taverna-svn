package net.sf.taverna.t2.platform.pom.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.t2.platform.pom.ArtifactParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Static helper methods for parsing XML Document instances
 * 
 * @author Tom Oinn
 */
public class DomUtils {

	private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
			.newInstance();

	/**
	 * Find any descendants of the given node with the specified element name
	 * 
	 * @param fromnode
	 * @param name
	 * @return
	 */
	public static List<Node> findElements(Node fromnode, String name) {
		List<Node> list = new ArrayList<Node>();
		NodeList childNodes = fromnode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (name.equals(node.getNodeName())) {
					list.add(node);
				}
				list.addAll(findElements(node, name));
			}
		}
		return list;
	}

	/**
	 * Find all descendants of the given node with the specified sequence of
	 * element names - must be an exact match for the path from the specified
	 * node to a found node with the names of elements in the path corresponding
	 * to those in the names[] array.
	 * 
	 * @param fromNode
	 * @param names
	 * @return
	 */
	public static List<Node> findElements(Node fromNode, String[] names) {
		List<Node> fromNodes = new ArrayList<Node>();
		fromNodes.add(fromNode);
		List<Node> foundNodes = new ArrayList<Node>();
		for (String name : names) {
			for (Node from : fromNodes) {
				foundNodes.addAll(findImmediateElements(from, name));
			}
			fromNodes = foundNodes;
			foundNodes = new ArrayList<Node>();
		}
		return fromNodes;
	}

	/**
	 * Find all immediate children of the given node with the specified element
	 * name
	 * 
	 * @param fromnode
	 * @param name
	 * @return
	 */
	public static List<Node> findImmediateElements(Node fromnode, String name) {
		NodeList nodelist = fromnode.getChildNodes();
		List<Node> list = new ArrayList<Node>();
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node node = nodelist.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (name.equals(node.getNodeName())) {
					list.add(node);
				}
				// list.addAll(findElements(node, name));
			}
		}
		return list;
	}

	/**
	 * Read an xml document from a file into a Document object
	 * 
	 * @param xmlFile
	 *            a java File containing XML to parse
	 * @return contents of the file parsed into a Document
	 * @throws ArtifactParseException
	 *             if any exception is thrown from the underlying readXML(URL)
	 *             method or if the URL cannot be constructed, this wraps
	 *             checked exceptions associated with the various DOM and file
	 *             operations in the unchecked artifact parse exception.
	 */
	public static Document readXML(File xmlFile) {
		try {
			return readXML(xmlFile.toURI().toURL());
		} catch (MalformedURLException mue) {
			throw new ArtifactParseException("Malformed URL", mue);
		}
	}

	/**
	 * Read an xml document from a URL into a Document object
	 * 
	 * @param url
	 *            a URL object pointing at XML data to parse
	 * @return contents of the URL parsed into a document
	 * @throws ArtifactParseException
	 *             if any exception is thrown, this wraps checked exceptions
	 *             associated with the various DOM and file operations in the
	 *             unchecked artifact parse exception.
	 */
	public static Document readXML(URL url) {
		try {
			DocumentBuilder builder = documentBuilderFactory
					.newDocumentBuilder();
			InputStream stream = url.openStream();
			Document document;
			try {
				document = builder.parse(stream);
			} finally {
				stream.close();
			}
			return document;
		} catch (ParserConfigurationException pce) {
			throw new ArtifactParseException("Parser configuration error", pce);
		} catch (SAXException se) {
			throw new ArtifactParseException("SAX exception", se);
		} catch (IOException ioe) {
			throw new ArtifactParseException("IO exception", ioe);
		}
	}

}
