/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.biomoby.client.CentralImpl;
import org.biomoby.shared.Central;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.Utils;
import org.biomoby.shared.mobyxml.jdom.MobyObjectClassNSImpl;
import org.biomoby.shared.mobyxml.jdom.jDomUtilities;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * 
 * @author Eddie Kawas
 * 
 */
public class XMLUtilities {

	// private variables
	// machine independent new line character
	public final static String newline = System.getProperty("line.separator");

	// class variable to keep persistant queryIDs
	private static int queryCount = 0;

	// the moby namespaces
	public final static Namespace MOBY_NS = MobyObjectClassNSImpl.MOBYNS;

	/**
	 * 
	 * @param message
	 *            the structurally valid BioMoby message as a String
	 * @return true if the message contains multiple invocations, false
	 *         otherwise.
	 * @throws MobyException
	 *             if the message is null
	 */
	public static boolean isMultipleInvocationMessage(String message) throws MobyException {
		if (message == null)
			throw new MobyException(
					newline
							+ "null 'xml' found where it was not expected in isMultipleInvocationMessage(String message).");
		Element documentElement = getDOMDocument(message).getRootElement();
		return isMultipleInvocationMessage(documentElement);
	}

	/**
	 * 
	 * @param message
	 *            the structurally valid BioMoby message as an Element
	 * @return true if the message contains multiple invocations, false
	 *         otherwise.
	 * @throws MobyException
	 *             if the message is null
	 */
	public static boolean isMultipleInvocationMessage(Element message) throws MobyException {
		if (message == null)
			throw new MobyException(
					newline
							+ "null 'xml' found where it was not expected in isMultipleInvocationMessage(Element message).");
		List list = new ArrayList();
		jDomUtilities.listChildren(message, "mobyData", list);
		if (list != null)
			if (list.size() > 1)
				return true;
		return false;
	}

	/**
	 * 
	 * @param element
	 *            the element to extract the list of simples from. This method
	 *            assumes that you are passing in a single invokation and that
	 *            you wish to extract the Simples not contained in any
	 *            collections. This method also maintains any past queryIDs.
	 * @return an array of elements that are fully 'wrapped' simples.
	 * @throws MobyException
	 *             if the Element isnt structurally valid in terms of Moby
	 *             message structure
	 */
	public static Element[] getListOfSimples(Element element) throws MobyException {
		Element temp = element;
		String queryID = "";

		Element serviceNotes = getServiceNotes(element);

		// if the current elements name isnt MOBY, see if its direct child is
		if (!element.getName().equals("MOBY")) {
			if (element.getChild("MOBY") != null)
				temp = element.getChild("MOBY");
			else if (element.getChild("MOBY", MOBY_NS) != null)
				temp = element.getChild("MOBY", MOBY_NS);
			else
				throw new MobyException(newline
						+ "Expected 'MOBY' as the local name for the element " + newline
						+ "and instead received '" + element.getName()
						+ "' (getListOfSimples(Element element).");
		}
		// parse the mobyContent node
		if (temp.getChild("mobyContent") != null)
			temp = temp.getChild("mobyContent");
		else if (temp.getChild("mobyContent", MOBY_NS) != null)
			temp = temp.getChild("mobyContent", MOBY_NS);
		else
			throw new MobyException(
					newline
							+ "Expected 'mobyContent' as the local name for the next child element but it "
							+ newline
							+ "wasn't there. I even tried a qualified name (getListOfSimples(Element element).");

		// parse the mobyData node
		if (temp.getChild("mobyData") != null) {
			temp = temp.getChild("mobyData");
		} else if (temp.getChild("mobyData", MOBY_NS) != null) {
			temp = temp.getChild("mobyData", MOBY_NS);
		} else {
			throw new MobyException(
					newline
							+ "Expected 'mobyData' as the local name for the next child element but it "
							+ newline
							+ "wasn't there. I even tried a qualified name (getListOfSimples(Element element).");
		}

		// temp == mobyData now we need to get the queryID and save it
		if (temp.getAttribute("queryID") != null) {
			queryID = temp.getAttribute("queryID").getValue();
		} else if (temp.getAttribute("queryID", MOBY_NS) != null) {
			queryID = temp.getAttribute("queryID", MOBY_NS).getValue();
		} else {
			// create a new one -> shouldnt happen very often
			queryID = "a" + queryCount++;
		}

		// now we iterate through all of the direct children called Simple, wrap
		// them individually and set the queryID = queryID
		List list = temp.getChildren("Simple", MOBY_NS);
		if (list.isEmpty()) {
			list = temp.getChildren("Simple");
			if (list.isEmpty()) {
				return new Element[] {};
			}
		}
		// non empty list
		Element[] elements = new Element[list.size()];
		int index = 0;
		for (Iterator it = list.iterator(); it.hasNext();) {
			Element next = (Element) it.next();
			elements[index++] = createMobyDataElementWrapper(next, queryID, serviceNotes);
		}
		return elements;
	}

	/**
	 * 
	 * @param message
	 *            the String of xml to extract the list of simples from. This
	 *            method assumes that you are passing in a single invokation and
	 *            that you wish to extract the Simples not contained in any
	 *            collections. This method also maintains any past queryIDs.
	 * @return an array of Strings that represent fully 'wrapped' simples.
	 * @throws MobyException
	 *             if the String doesnt contain a structurally valid Moby
	 *             message structure or if an unexpected error occurs
	 */
	public static String[] getListOfSimples(String message) throws MobyException {
		Element element = getDOMDocument(message).getRootElement();
		Element[] elements = getListOfSimples(element);
		String[] strings = new String[elements.length];
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		for (int count = 0; count < elements.length; count++) {
			try {
				strings[count] = outputter.outputString(elements[count]);
			} catch (Exception e) {
				throw new MobyException(newline
						+ "Unexpected error occured while creating String[]:" + newline
						+ Utils.format(e.getLocalizedMessage(), 3));
			}
		}
		return strings;
	}

	public static String[] getListOfCollections(String message) throws MobyException {
		Element element = getDOMDocument(message).getRootElement();
		Element[] elements = getListOfCollections(element);
		String[] strings = new String[elements.length];
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		for (int count = 0; count < elements.length; count++) {
			try {
				strings[count] = outputter.outputString(elements[count]);
			} catch (Exception e) {
				throw new MobyException(newline
						+ "Unexpected error occured while creating String[]:" + newline
						+ Utils.format(e.getLocalizedMessage(), 3));
			}
		}
		return strings;
	}

	public static Element[] getListOfCollections(Element element) throws MobyException {
		Element temp = element;
		String queryID = "";

		Element serviceNotes = getServiceNotes(element);

		// if the current elements name isnt MOBY, see if its direct child is
		if (!element.getName().equals("MOBY")) {
			if (element.getChild("MOBY") != null)
				temp = element.getChild("MOBY");
			else if (element.getChild("MOBY", MOBY_NS) != null)
				temp = element.getChild("MOBY", MOBY_NS);
			else
				throw new MobyException(newline
						+ "Expected 'MOBY' as the local name for the element " + newline
						+ "and instead received '" + element.getName()
						+ "' (getListOfCollections(Element element).");
		}
		// parse the mobyContent node
		if (temp.getChild("mobyContent") != null)
			temp = temp.getChild("mobyContent");
		else if (temp.getChild("mobyContent", MOBY_NS) != null)
			temp = temp.getChild("mobyContent", MOBY_NS);
		else
			throw new MobyException(
					newline
							+ "Expected 'mobyContent' as the local name for the next child element but it "
							+ newline
							+ "wasn't there. I even tried a qualified name (getListOfCollections(Element element).");

		// parse the mobyData node
		if (temp.getChild("mobyData") != null) {
			temp = temp.getChild("mobyData");
		} else if (temp.getChild("mobyData", MOBY_NS) != null) {
			temp = temp.getChild("mobyData", MOBY_NS);
		} else {
			throw new MobyException(
					newline
							+ "Expected 'mobyData' as the local name for the next child element but it "
							+ newline
							+ "wasn't there. I even tried a qualified name (getListOfCollections(Element element).");
		}

		// temp == mobyData now we need to get the queryID and save it
		if (temp.getAttribute("queryID") != null) {
			queryID = temp.getAttribute("queryID").getValue();
		} else if (temp.getAttribute("queryID", MOBY_NS) != null) {
			queryID = temp.getAttribute("queryID", MOBY_NS).getValue();
		} else {
			// create a new one -> shouldnt happen very often
			queryID = "a" + queryCount++;
		}

		// now we iterate through all of the direct children called Simple, wrap
		// them individually and set the queryID = queryID
		List list = temp.getChildren("Collection", MOBY_NS);
		if (list.isEmpty()) {
			list = temp.getChildren("Collection");
			if (list.isEmpty()) {
				return new Element[] {};
			}
		}
		// non empty list
		Element[] elements = new Element[list.size()];
		int index = 0;
		for (Iterator it = list.iterator(); it.hasNext();) {
			Element next = (Element) it.next();
			elements[index++] = createMobyDataElementWrapper(next, queryID, serviceNotes);
		}
		return elements;
	}

	/**
	 * 
	 * @param name
	 *            the article name of the simple that you are looking for
	 * @param type
	 *            the datatype of the xml element that you are looking for
	 * @param xml
	 *            the xml that you want to query
	 * @param endpoint
	 *            the mobycentral endpoint to use (defaults to mobycentral)
	 * @return an array of String objects that represent the simples found.
	 * @throws MobyException
	 *             if no simple was found given the article name and/or data
	 *             type or if the xml was not valid moby xml or if an unexpected
	 *             error occurs.
	 */
	public static String getSimple(String name, String type, String xml, String endpoint)
			throws MobyException {
		Element element = getDOMDocument(xml).getRootElement();
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		Element simples = getSimple(name, type, element, endpoint);
		if (simples != null) {
			try {
				return outputter.outputString(simples);
			} catch (Exception e) {
				throw new MobyException(newline
						+ "Unexpected error occured while creating String[]:" + newline
						+ Utils.format(e.getLocalizedMessage(), 3));
			}
		}
		throw new MobyException(newline + "The simple named '" + name
				+ "' was not found in the xml:" + newline + xml + newline
				+ "I even tried searching for the children of the type '" + type
				+ "' but couldnt find it.");
	}

	/**
	 * 
	 * @param name
	 *            the article name of the simple that you are looking for
	 * @param type
	 *            the datatype of the xml element that you are looking for
	 * @param element
	 *            the Element that you want to query
	 * @param endpoint
	 *            the mobycentral endpoint to use (defaults to mobycentral)
	 * @return an array of Element objects that represent the simples found.
	 * @throws MobyException
	 *             if no simple was found given the article name and/or data
	 *             type or if the xml was not valid moby xml or if an unexpected
	 *             error occurs.
	 */
	public static Element getSimple(String name, String type, Element element, String endpoint)
			throws MobyException {
		Element[] elements = getListOfSimples(element);
		// try matching based on type(less impt) and/or article name (more impt)
		for (int i = 0; i < elements.length; i++) {
			// PRE: elements[i] is a fully wrapped element
			Element e = elements[i];
			if (e.getChild("mobyContent") != null) {
				e = e.getChild("mobyContent");
			} else if (e.getChild("mobyContent", MOBY_NS) != null) {
				e = e.getChild("mobyContent", MOBY_NS);
			} else {
				throw new MobyException(
						newline
								+ "Expected 'mobyContent' as the local name for the next child element but it "
								+ newline
								+ "wasn't there. I even tried a qualified name (getSimple(String name, String type, "
								+ "Element element, String endpoint).");
			}
			if (e.getChild("mobyData") != null) {
				e = e.getChild("mobyData");
			} else if (e.getChild("mobyData", MOBY_NS) != null) {
				e = e.getChild("mobyData", MOBY_NS);
			} else {
				throw new MobyException(
						newline
								+ "Expected 'mobyData' as the local name for the next child element but it "
								+ newline
								+ "wasn't there. I even tried a qualified name (getSimple(String name,"
								+ " String type, Element element, String endpoint).");
			}
			if (e.getChild("Simple") != null) {
				e = e.getChild("Simple");
			} else if (e.getChild("Simple", MOBY_NS) != null) {
				e = e.getChild("Simple", MOBY_NS);
			} else {
				throw new MobyException(
						newline
								+ "Expected 'Simple' as the local name for the next child element but it "
								+ newline
								+ "wasn't there. I even tried a qualified name (getSimple(String name, String type,"
								+ " Element element, String endpoint).");
			}
			// e == Simple -> check its name as long as name != ""
			if (!name.equals(""))
				if (e.getAttributeValue("articleName") != null) {
					String value = e.getAttributeValue("articleName");
					if (value.equals(name)) {
						return e;
					}
				} else if (e.getAttributeValue("articleName", MOBY_NS) != null) {
					String value = e.getAttributeValue("articleName", MOBY_NS);
					if (value.equals(name)) {
						return e;
					}
				}
			// name didnt match, so lets try matching the object type of the
			// simple
			if (e.getChild(type) != null) {
				return e;
			} else if (e.getChild(type, MOBY_NS) != null) {
				return e;
			}
			// type didnt match - now try matching child types
			try {
				Central central = new CentralImpl(endpoint);
				List children = e.getChildren();
				// should be a single simple element
				if (children.size() != 1)
					continue;
				String simpleObjectType = ((Element) children.get(0)).getName();
				String[] isa = central.getDataTypeRelationships(simpleObjectType, Central.ISA);
				for (int j = 0; j < isa.length; j++) {
					if (isa[j].equals(type)) {
						// the element passed in is a sub child of type
						return e;
					}
				}
			} catch (MobyException me) {
				// ignore this exception
			}
		}
		throw new MobyException(newline + "The simple named '" + name
				+ "' was not found in the xml:" + newline
				+ (new XMLOutputter(Format.getPrettyFormat())).outputString(element) + newline
				+ "I even tried searching for the children of the type '" + type
				+ "' but couldnt find it.");
	}

	/**
	 * 
	 * @param xml
	 *            a string of xml containing a single invocation message to
	 *            extract the queryID from
	 * @return the queryID contained in the xml or a generated one if one doesnt
	 *         exist
	 * @throws MobyException
	 *             if the String of xml is invalid
	 */
	public static String getQueryID(String xml) throws MobyException {
		return getQueryID(getDOMDocument(xml).getRootElement());
	}

	/**
	 * 
	 * @param xml
	 *            a single invocation message to extract the queryID from
	 * @return the queryID contained in the xml or a generated one if one doesnt
	 *         exist
	 */
	public static String getQueryID(Element xml) {
		Element temp = xml;
		if (!xml.getName().equals("MOBY")) {
			if (xml.getChild("MOBY") != null)
				temp = xml.getChild("MOBY");
			else if (xml.getChild("MOBY", MOBY_NS) != null)
				temp = xml.getChild("MOBY", MOBY_NS);
		}
		// parse the mobyContent node
		if (temp.getChild("mobyContent") != null)
			temp = temp.getChild("mobyContent");
		else if (temp.getChild("mobyContent", MOBY_NS) != null)
			temp = temp.getChild("mobyContent", MOBY_NS);

		// parse the mobyData node
		if (temp.getChild("mobyData") != null) {
			temp = temp.getChild("mobyData");
		} else if (temp.getChild("mobyData", MOBY_NS) != null) {
			temp = temp.getChild("mobyData", MOBY_NS);
		}

		// temp == mobyData now we need to get the queryID and save it
		if (temp.getAttribute("queryID") != null) {
			return temp.getAttribute("queryID").getValue();
		} else if (temp.getAttribute("queryID", MOBY_NS) != null) {
			return temp.getAttribute("queryID", MOBY_NS).getValue();
		} else {
			// create a new one -> shouldnt happen very often
			return "a" + queryCount++;
		}
	}

	/**
	 * 
	 * @param xml
	 *            a string of xml containing a single invocation message to
	 *            extract the queryID from
	 * @return the queryID contained in the xml or a generated one if one doesnt
	 *         exist
	 * @throws MobyException
	 *             if the String of xml is invalid
	 */
	public static String setQueryID(String xml, String id) throws MobyException {
		return new XMLOutputter(Format.getPrettyFormat()).outputString(setQueryID(getDOMDocument(
				xml).getRootElement(), id));
	}

	/**
	 * 
	 * @param xml
	 *            a single invocation message to extract the queryID from
	 * @return the queryID contained in the xml or a generated one if one doesnt
	 *         exist
	 */
	public static Element setQueryID(Element xml, String id) {
		Element temp = xml;
		if (!xml.getName().equals("MOBY")) {
			if (xml.getChild("MOBY") != null)
				temp = xml.getChild("MOBY");
			else if (xml.getChild("MOBY", MOBY_NS) != null)
				temp = xml.getChild("MOBY", MOBY_NS);
		}
		// parse the mobyContent node
		if (temp.getChild("mobyContent") != null)
			temp = temp.getChild("mobyContent");
		else if (temp.getChild("mobyContent", MOBY_NS) != null)
			temp = temp.getChild("mobyContent", MOBY_NS);

		// parse the mobyData node
		if (temp.getChild("mobyData") != null) {
			temp = temp.getChild("mobyData");
		} else if (temp.getChild("mobyData", MOBY_NS) != null) {
			temp = temp.getChild("mobyData", MOBY_NS);
		}

		temp.removeAttribute("queryID");
		temp.removeAttribute("queryID", MOBY_NS);
		temp.setAttribute("queryID", (id == null || id == "" ? "a" + queryCount++ : id), MOBY_NS);
		return temp;
	}

	/**
	 * 
	 * @param name
	 * @param type
	 * @param xml
	 * @param endpoint
	 * @return
	 * @throws MobyException
	 */
	public static String getWrappedSimple(String name, String type, String xml, String endpoint)
			throws MobyException {
		Element element = getWrappedSimple(name, type, getDOMDocument(xml).getRootElement(),
				endpoint);
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		return outputter.outputString(element);
	}

	/**
	 * 
	 * @param name
	 * @param type
	 * @param element
	 * @param endpoint
	 * @return
	 * @throws MobyException
	 */
	public static Element getWrappedSimple(String name, String type, Element element,
			String endpoint) throws MobyException {
		String queryID = getQueryID(element);
		Element serviceNotes = getServiceNotes(element);
		Element simple = getSimple(name, type, element, endpoint);
		return createMobyDataElementWrapper(simple, queryID, serviceNotes);
	}

	/**
	 * 
	 * @param name
	 * @param element
	 * @return
	 * @throws MobyException
	 */
	public static Element getCollection(String name, Element element) throws MobyException {
		Element[] elements = getListOfCollections(element);
		for (int i = 0; i < elements.length; i++) {
			// PRE: elements[i] is a fully wrapped element
			Element e = elements[i];
			if (e.getChild("mobyContent") != null) {
				e = e.getChild("mobyContent");
			} else if (e.getChild("mobyContent", MOBY_NS) != null) {
				e = e.getChild("mobyContent", MOBY_NS);
			} else {
				throw new MobyException(
						newline
								+ "Expected 'mobyContent' as the local name for the next child element but it "
								+ newline
								+ "wasn't there. I even tried a qualified name (getCollection(String name, "
								+ "Element element).");
			}
			if (e.getChild("mobyData") != null) {
				e = e.getChild("mobyData");
			} else if (e.getChild("mobyData", MOBY_NS) != null) {
				e = e.getChild("mobyData", MOBY_NS);
			} else {
				throw new MobyException(
						newline
								+ "Expected 'mobyData' as the local name for the next child element but it "
								+ newline
								+ "wasn't there. I even tried a qualified name (getCollection(String name,"
								+ " Element element).");
			}
			if (e.getChild("Collection") != null) {
				e = e.getChild("Collection");
			} else if (e.getChild("Collection", MOBY_NS) != null) {
				e = e.getChild("Collection", MOBY_NS);
			} else {
				// TODO should i throw exception or continue?
				throw new MobyException(
						newline
								+ "Expected 'Collection' as the local name for the next child element but it "
								+ newline
								+ "wasn't there. I even tried a qualified name (getCollection(String name,"
								+ " Element element).");
			}
			// e == collection -> check its name
			if (e.getAttributeValue("articleName") != null) {
				String value = e.getAttributeValue("articleName");
				if (value.equals(name)) {
					return e;
				}
			} else if (e.getAttributeValue("articleName", MOBY_NS) != null) {
				String value = e.getAttributeValue("articleName", MOBY_NS);
				if (value.equals(name)) {
					return e;
				}
			}
			if (elements.length == 1) {
				if (e.getAttributeValue("articleName") != null) {
					String value = e.getAttributeValue("articleName");
					if (value.equals("")) {
						// rename it to make it compatible with moby
						e.setAttribute("articleName", name, MOBY_NS);
						return e;
					}
				} else if (e.getAttributeValue("articleName", MOBY_NS) != null) {
					String value = e.getAttributeValue("articleName", MOBY_NS);
					if (value.equals("")) {
						// rename it to make it compatible with moby
						e.setAttribute("articleName", name, MOBY_NS);
						return e;
					}
				}
			}
			// name didnt match, so too bad ;-)
		}
		throw new MobyException(newline + "The Collection named '" + name
				+ "' was not found in the xml:" + newline
				+ (new XMLOutputter(Format.getPrettyFormat())).outputString(element));
	}

	/**
	 * 
	 * @param name
	 * @param xml
	 * @return
	 * @throws MobyException
	 */
	public static String getCollection(String name, String xml) throws MobyException {
		Element element = getDOMDocument(xml).getRootElement();
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		Element collection = getCollection(name, element);
		if (collection != null)
			return outputter.outputString(collection);
		return null;
	}

	/**
	 * 
	 * @param name
	 * @param element
	 * @return
	 * @throws MobyException
	 */
	public static Element getWrappedCollection(String name, Element element) throws MobyException {
		String queryID = getQueryID(element);
		Element collection = getCollection(name, element);
		Element serviceNotes = getServiceNotes(element);
		return createMobyDataElementWrapper(collection, queryID, serviceNotes);
	}

	/**
	 * 
	 * @param name
	 * @param xml
	 * @return
	 * @throws MobyException
	 */
	public static String getWrappedCollection(String name, String xml) throws MobyException {
		Element element = getWrappedCollection(name, getDOMDocument(xml).getRootElement());
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		return outputter.outputString(element);
	}

	/**
	 * 
	 * @param name
	 *            the name of the collection to extract the simples from.
	 * @param xml
	 *            the XML to extract from
	 * @return an array of String objects that represent the simples
	 * @throws MobyException
	 */
	public static String[] getSimplesFromCollection(String name, String xml) throws MobyException {
		Element[] elements = getSimplesFromCollection(name, getDOMDocument(xml).getRootElement());
		String[] strings = new String[elements.length];
		XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
		for (int i = 0; i < elements.length; i++) {
			try {
				strings[i] = output.outputString(elements[i]);
			} catch (Exception e) {
				throw new MobyException(newline + "Unknown error occured while creating String[]."
						+ newline + Utils.format(e.getLocalizedMessage(), 3));
			}
		}
		return strings;
	}

	/**
	 * 
	 * @param name
	 *            the name of the collection to extract the simples from.
	 * @param element
	 *            the Element to extract from
	 * @return an array of Elements objects that represent the simples
	 * @throws MobyException
	 */
	public static Element[] getSimplesFromCollection(String name, Element element)
			throws MobyException {
		// exception thrown if not found
		Element collection = getCollection(name, element);

		List list = collection.getChildren("Simple");
		if (list.isEmpty())
			list = collection.getChildren("Simple", MOBY_NS);
		if (list.isEmpty())
			return new Element[] {};
		Vector vector = new Vector();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Object o = it.next();
			if (o instanceof Element) {
				((Element) o).setAttribute("articleName", name, MOBY_NS);
			}
			vector.add(o);
		}
		Element[] elements = new Element[vector.size()];
		vector.copyInto(elements);
		return elements;
	}

	/**
	 * 
	 * @param xml
	 *            the XML to extract from
	 * @return an array of String objects that represent the simples
	 * @throws MobyException
	 */
	public static String[] getSimplesFromCollection(String xml) throws MobyException {
		Element[] elements = getSimplesFromCollection(getDOMDocument(xml).getRootElement());
		String[] strings = new String[elements.length];
		XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
		for (int i = 0; i < elements.length; i++) {
			try {
				strings[i] = output.outputString(elements[i]);
			} catch (Exception e) {
				throw new MobyException(newline + "Unknown error occured while creating String[]."
						+ newline + Utils.format(e.getLocalizedMessage(), 3));
			}
		}
		return strings;
	}

	/**
	 * 
	 * @param name
	 *            the name of the collection to extract the simples from.
	 * @param element
	 *            the Element to extract from
	 * @return an array of Elements objects that represent the 'unwrapped'
	 *         simples
	 * @throws MobyException
	 */
	public static Element[] getSimplesFromCollection(Element element) throws MobyException {

		Element mobyData = extractMobyData(element);

		Element collection = mobyData.getChild("Collection");
		if (collection == null)
			collection = mobyData.getChild("Collection", MOBY_NS);

		List list = collection.getChildren("Simple");
		if (list.isEmpty())
			list = collection.getChildren("Simple", MOBY_NS);
		if (list.isEmpty())
			return new Element[] {};
		Vector vector = new Vector();
		for (Iterator it = list.iterator(); it.hasNext();) {
			vector.add(it.next());
		}
		Element[] elements = new Element[vector.size()];
		vector.copyInto(elements);
		return elements;
	}

	/**
	 * 
	 * @param name
	 *            the name of the collection to extract the simples from.
	 * @param xml
	 *            the XML to extract from
	 * @return an array of String objects that represent the simples
	 * @throws MobyException
	 */
	public static String[] getWrappedSimplesFromCollection(String name, String xml)
			throws MobyException {
		Element[] elements = getWrappedSimplesFromCollection(name, getDOMDocument(xml)
				.getRootElement());
		String[] strings = new String[elements.length];
		XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
		for (int i = 0; i < elements.length; i++) {
			try {
				strings[i] = output.outputString(elements[i]);
			} catch (Exception e) {
				throw new MobyException(newline + "Unknown error occured while creating String[]."
						+ newline + Utils.format(e.getLocalizedMessage(), 3));
			}
		}
		return strings;
	}

	/**
	 * 
	 * @param name
	 *            the name of the collection to extract the simples from.
	 * @param element
	 *            the Element to extract from
	 * @return an array of Elements objects that represent the simples
	 * @throws MobyException
	 */
	public static Element[] getWrappedSimplesFromCollection(String name, Element element)
			throws MobyException {
		String queryID = getQueryID(element);
		Element collection = getCollection(name, element);
		Element serviceNotes = getServiceNotes(element);
		List list = collection.getChildren("Simple");
		if (list.isEmpty())
			list = collection.getChildren("Simple", MOBY_NS);
		if (list.isEmpty())
			return new Element[] {};
		Vector vector = new Vector();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Element e = (Element) it.next();
			e = createMobyDataElementWrapper(e, queryID + "_split" + queryCount++, serviceNotes);
			vector.add(e);
		}
		Element[] elements = new Element[vector.size()];
		vector.copyInto(elements);
		return elements;
	}

	/**
	 * 
	 * @param xml
	 * @return
	 * @throws MobyException
	 */
	public static String[] getSingleInvokationsFromMultipleInvokations(String xml)
			throws MobyException {
		Element[] elements = getSingleInvokationsFromMultipleInvokations(getDOMDocument(xml)
				.getRootElement());
		String[] strings = new String[elements.length];
		XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());

		for (int i = 0; i < elements.length; i++) {
			strings[i] = output.outputString(elements[i]);
		}
		return strings;
	}

	/**
	 * 
	 * @param element
	 * @return
	 * @throws MobyException
	 */
	public static Element[] getSingleInvokationsFromMultipleInvokations(Element element)
			throws MobyException {
		Element e = element;
		Element serviceNotes = getServiceNotes(element);
		if (e.getChild("MOBY") != null) {
			e = e.getChild("MOBY");
		} else if (e.getChild("MOBY", MOBY_NS) != null) {
			e = e.getChild("MOBY", MOBY_NS);
		}

		if (e.getChild("mobyContent") != null) {
			e = e.getChild("mobyContent");
		} else if (e.getChild("mobyContent", MOBY_NS) != null) {
			e = e.getChild("mobyContent", MOBY_NS);
		} else {
			throw new MobyException(newline
					+ "Expected a child element called 'mobyContent' and did not receive it in:"
					+ newline + new XMLOutputter(Format.getPrettyFormat()).outputString(e));
		}
		List invocations = e.getChildren("mobyData");
		if (invocations.isEmpty())
			invocations = e.getChildren("mobyData", MOBY_NS);
		Element[] elements = new Element[invocations.size()];
		int count = 0;
		for (Iterator it = invocations.iterator(); it.hasNext();) {
			Element MOBY = new Element("MOBY", MOBY_NS);
			Element mobyContent = new Element("mobyContent", MOBY_NS);
			if (serviceNotes != null)
				mobyContent.addContent(serviceNotes.detach());
			Element mobyData = new Element("mobyData", MOBY_NS);
			Element next = (Element) it.next();
			String queryID = next.getAttributeValue("queryID", MOBY_NS);
			if (queryID == null)
				queryID = next.getAttributeValue("queryID");

			mobyData.setAttribute("queryID", queryID, MOBY_NS);
			mobyData.addContent(next.cloneContent());
			MOBY.addContent(mobyContent);
			mobyContent.addContent(mobyData);
			elements[count++] = MOBY;
		}
		return elements;
	}

	/**
	 * 
	 * @param document
	 * @return
	 * @throws MobyException
	 */
	public static Document getDOMDocument(String document) throws MobyException {
		if (document == null)
			throw new MobyException(newline + "null found where an XML document was expected.");
		SAXBuilder builder = new SAXBuilder();
		// Create the document
		Document doc = null;
		try {
			doc = builder.build(new StringReader(document));
		} catch (JDOMException e) {
			// e.printStackTrace();
			throw new MobyException(newline + "Error parsing XML:->" + newline + document + newline
					+ Utils.format(e.getLocalizedMessage(), 3) + ".");
		} catch (IOException e) {
			// e.printStackTrace();
			throw new MobyException(newline + "Error parsing XML:->" + newline
					+ Utils.format(e.getLocalizedMessage(), 3) + ".");
		} catch (Exception e) {
			// e.printStackTrace();
			throw new MobyException(newline + "Error parsing XML:->" + newline
					+ Utils.format(e.getLocalizedMessage(), 3) + ".");
		}
		return doc;
	}

	/**
	 * 
	 * @param elements
	 * @param queryID
	 * @return
	 * @throws MobyException
	 */
	public static String createServiceInput(String[] elements, String queryID) throws MobyException {
		Element[] element = new Element[elements.length];
		for (int i = 0; i < elements.length; i++) {
			element[i] = getDOMDocument(elements[i]).getRootElement();
		}
		XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
		return output.outputString(createServiceInput(element, queryID));
	}

	/**
	 * 
	 * @param elements
	 * @param queryID
	 * @return
	 * @throws MobyException
	 */
	public static Element createServiceInput(Element[] elements, String queryID)
			throws MobyException {
		// create the main elements
		Element MOBY = new Element("MOBY", MOBY_NS);
		Element mobyContent = new Element("mobyContent", MOBY_NS);
		Element mobyData = new Element("mobyData", MOBY_NS);
		mobyData.setAttribute("queryID", (queryID == null ? "" : queryID), MOBY_NS);

		// add the content
		MOBY.addContent(mobyContent);
		mobyContent.addContent(mobyData);

		// iterate through elements adding the content of mobyData
		for (int i = 0; i < elements.length; i++) {
			Element e = elements[i];
			e = extractMobyData(e);
			mobyData.addContent(e.cloneContent());
		}

		return MOBY;
	}

	/**
	 * @param element
	 * @return
	 * @throws MobyException
	 */
	public static Element extractMobyData(Element element) throws MobyException {
		if (element.getChild("MOBY") != null) {
			element = element.getChild("MOBY");
		} else if (element.getChild("MOBY", MOBY_NS) != null) {
			element = element.getChild("MOBY", MOBY_NS);
		}

		if (element.getChild("mobyContent") != null) {
			element = element.getChild("mobyContent");
		} else if (element.getChild("mobyContent", MOBY_NS) != null) {
			element = element.getChild("mobyContent", MOBY_NS);
		} else {
			throw new MobyException(newline
					+ "Expected the child element 'mobyContent' and did not receive it in:"
					+ newline + new XMLOutputter(Format.getPrettyFormat()).outputString(element));
		}

		if (element.getChild("mobyData") != null) {
			element = element.getChild("mobyData");
		} else if (element.getChild("mobyData", MOBY_NS) != null) {
			element = element.getChild("mobyData", MOBY_NS);
		} else {
			throw new MobyException(newline
					+ "Expected the child element 'mobyData' and did not receive it in:" + newline
					+ new XMLOutputter(Format.getPrettyFormat()).outputString(element));
		}
		return element;
	}

	/**
	 * 
	 * @param oldName
	 * @param newName
	 * @param element
	 * @return
	 * @throws MobyException
	 */
	public static Element renameCollection(String newName, Element element) throws MobyException {
		Element mobyData = extractMobyData(element);
		Element coll = mobyData.getChild("Collection");
		if (coll == null)
			coll = mobyData.getChild("Collection", MOBY_NS);
		if (coll == null)
			return element;
		coll.removeAttribute("articleName");
		coll.removeAttribute("articleName", MOBY_NS);
		coll.setAttribute("articleName", newName, MOBY_NS);
		return coll;
	}

	/**
	 * 
	 * @param oldName
	 * @param newName
	 * @param xml
	 * @return
	 * @throws MobyException
	 */
	public static String renameCollection(String newName, String xml) throws MobyException {
		return new XMLOutputter(Format.getPrettyFormat()).outputString(renameCollection(newName,
				getDOMDocument(xml).getRootElement()));
	}

	/**
	 * 
	 * @param oldName
	 * @param newName
	 * @param type
	 * @param xml
	 * @return
	 * @throws MobyException
	 */
	public static String renameSimple(String newName, String type, String xml) throws MobyException {
		return new XMLOutputter(Format.getPrettyFormat()).outputString(renameSimple(newName, type,
				getDOMDocument(xml).getRootElement()));
	}

	/**
	 * 
	 * @param oldName
	 * @param newName
	 * @param type
	 * @param element
	 * @return
	 * @throws MobyException
	 */
	public static Element renameSimple(String newName, String type, Element element)
			throws MobyException {
		Element mobyData = extractMobyData(element);
		String queryID = getQueryID(element);
		Element serviceNotes = getServiceNotes(element);
		Element simple = mobyData.getChild("Simple");
		if (simple == null)
			simple = mobyData.getChild("Simple", MOBY_NS);
		if (simple == null)
			return element;
		simple.removeAttribute("articleName");
		simple.removeAttribute("articleName", MOBY_NS);
		simple.setAttribute("articleName", newName, MOBY_NS);
		return createMobyDataElementWrapper(simple, queryID, serviceNotes);
	}

	/**
	 * 
	 * @return
	 * @throws MobyException
	 */
	public static Document createDomDocument() throws MobyException {
		Document d = new Document();
		d.setBaseURI(MobyObjectClassNSImpl.MOBYNS.getURI());
		return d;
	}

	/**
	 * 
	 * @param element
	 * @param queryID
	 * @param serviceNotes
	 * @return
	 * @throws MobyException
	 */
	public static Element createMobyDataElementWrapper(Element element, String queryID,
			Element serviceNotes) throws MobyException {
		Element MOBY = new Element("MOBY", MOBY_NS);
		Element mobyContent = new Element("mobyContent", MOBY_NS);
		Element mobyData = new Element("mobyData", MOBY_NS);
		mobyData.setAttribute("queryID", queryID, MOBY_NS);
		MOBY.addContent(mobyContent);
		mobyContent.addContent(mobyData);
		// add the serviceNotes if they exist
		if (serviceNotes != null)
			mobyContent.addContent(serviceNotes.detach());

		if (element != null) {
			if (element.getName().equals("Simple")) {
				Element simple = new Element("Simple", MOBY_NS);
				simple.setAttribute("articleName",
						(element.getAttributeValue("articleName") == null ? element
								.getAttributeValue("articleName", MOBY_NS, "") : element
								.getAttributeValue("articleName", "")), MOBY_NS);
				simple.addContent(element.cloneContent());
				mobyData.addContent(simple.detach());
			} else if (element.getName().equals("Collection")) {
				Element collection = new Element("Collection", MOBY_NS);
				collection.setAttribute("articleName",
						(element.getAttributeValue("articleName") == null ? element
								.getAttributeValue("articleName", MOBY_NS, "") : element
								.getAttributeValue("articleName", "")), MOBY_NS);
				collection.addContent(element.cloneContent());
				mobyData.addContent(collection.detach());
			}
		}

		return MOBY;
	}

	public static Element createMobyDataWrapper(String queryID, Element serviceNotes)
			throws MobyException {
		Element MOBY = new Element("MOBY", MOBY_NS);
		Element mobyContent = new Element("mobyContent", MOBY_NS);
		if (serviceNotes != null)
			mobyContent.addContent(serviceNotes.detach());
		Element mobyData = new Element("mobyData", MOBY_NS);
		mobyData.setAttribute("queryID", queryID, MOBY_NS);
		MOBY.addContent(mobyContent);
		mobyContent.addContent(mobyData);
		return MOBY;
	}

	/**
	 * 
	 * @param xml
	 * @return
	 * @throws MobyException
	 */
	public static String createMobyDataElementWrapper(String xml) throws MobyException {
		return createMobyDataElementWrapper(xml, "a" + queryCount++);
	}

	/**
	 * 
	 * @param element
	 * @return
	 * @throws MobyException
	 */
	public static Element createMobyDataElementWrapper(Element element) throws MobyException {
		Element serviceNotes = getServiceNotes(element);
		return createMobyDataElementWrapper(element, "a" + queryCount++, serviceNotes);
	}

	/**
	 * 
	 * @param xml
	 * @param queryID
	 * @return
	 * @throws MobyException
	 */
	public static String createMobyDataElementWrapper(String xml, String queryID)
			throws MobyException {
		if (xml == null)
			return null;
		Element serviceNotes = getServiceNotes(getDOMDocument(xml).getRootElement());
		Element element = createMobyDataElementWrapper(getDOMDocument(xml).getRootElement(),
				queryID, serviceNotes);
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		return (element == null ? null : outputter.outputString(element));
	}

	/**
	 * 
	 * @param elements
	 * @return
	 * @throws MobyException
	 */
	public static Element createMultipleInvokations(Element[] elements) throws MobyException {
		Element MOBY = new Element("MOBY", MOBY_NS);
		Element mobyContent = new Element("mobyContent", MOBY_NS);
		Element serviceNotes = null;
		for (int i = 0; i < elements.length; i++) {
			if (serviceNotes == null) {
				serviceNotes = getServiceNotes(elements[i]);
				if (serviceNotes != null)
					mobyContent.addContent(serviceNotes.detach());
			}
			Element mobyData = new Element("mobyData", MOBY_NS);
			Element md = extractMobyData(elements[i]);
			String queryID = getQueryID(elements[i]);
			mobyData.setAttribute("queryID", queryID, MOBY_NS);
			mobyData.addContent(md.cloneContent());
			mobyContent.addContent(mobyData);
		}
		MOBY.addContent(mobyContent);

		return MOBY;
	}

	/**
	 * 
	 * @param xmls
	 * @return
	 * @throws MobyException
	 */
	public static String createMultipleInvokations(String[] xmls) throws MobyException {
		Element[] elements = new Element[xmls.length];
		for (int i = 0; i < elements.length; i++) {
			elements[i] = getDOMDocument(xmls[i]).getRootElement();
		}
		XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
		return output.outputString(createMultipleInvokations(elements));
	}

	/**
	 * 
	 * @param element an Element
	 * @return true if the element represents a full moby message, false otherwise.
	 * @throws MobyException
	 */
	public static boolean isWrapped(Element element) throws MobyException {
		try {
			extractMobyData(element);
			return true;
		} catch (MobyException e) {
			return false;
		}
	}

	/**
	 * 
	 * @param xml a string of xml
	 * @return true if the xml is a full moby message
	 * @throws MobyException
	 */
	public static boolean isWrapped(String xml) throws MobyException {
		Element element = getDOMDocument(xml).getRootElement();
		return isWrapped(element);
	}

	/**
	 * 
	 * @param element an Element
	 * @return true if the element contains a moby collection, false otherwise.
	 * @throws MobyException if xml is invalid
	 */
	public static boolean isCollection(Element element) throws MobyException {
		try {
			return getListOfCollections(element).length > 0;

		} catch (MobyException e) {
			return false;
		}
	}

	/**
	 * 
	 * @param xml a string of xml
	 * @return true if the xml contains a moby collection, false otherwise.
	 * @throws MobyException if xml is invalid
	 */
	public static boolean isCollection(String xml) throws MobyException {
		Element element = getDOMDocument(xml).getRootElement();
		return isCollection(element);
	}


	/**
	 * 
	 * @param xml a string of xml to check for emptiness
	 * @return true if the element is empty, false otherwise.
	 */
	public static boolean isEmpty(String xml) {
		try {
			return isEmpty(getDOMDocument(xml).getRootElement());
		} catch (MobyException e) {
			return true;
		}
	}

	/**
	 * 
	 * @param xml an element to check for emptiness
	 * @return true if the element is empty, false otherwise.
	 */
	public static boolean isEmpty(Element xml) {
		try {
			Element e = extractMobyData(xml);
			if (e.getChild("Collection") != null)
				return false;
			if (e.getChild("Collection", MOBY_NS) != null)
				return false;
			if (e.getChild("Simple") != null)
				return false;
			if (e.getChild("Simple", MOBY_NS) != null)
				return false;
		} catch (MobyException e) {
		}
		return true;

	}

	/**
	 * 
	 * @param theList
	 *            a list of Elements that represent collections (wrapped in a
	 *            MobyData tag
	 * @return a list containing a single collection that contains all of the
	 *         simples in the collections in theList
	 * @throws MobyException
	 * 
	 */
	public static List mergeCollections(List theList, String name) throws MobyException {
		if (theList == null)
			return new ArrayList();
		Element mainCollection = new Element("Collection", MOBY_NS);
		mainCollection.setAttribute("articleName", name, MOBY_NS);
		String queryID = "";
		for (Iterator iter = theList.iterator(); iter.hasNext();) {
			Element mobyData = (Element) iter.next();
			queryID += getQueryID(mobyData);
			Element collection = mobyData.getChild("Collection");
			if (collection == null)
				collection = mobyData.getChild("Collection", MOBY_NS);
			if (collection == null)
				continue;
			mainCollection.addContent(collection.cloneContent());
		}
		theList = new ArrayList();
		theList.add(extractMobyData(createMobyDataElementWrapper(mainCollection, queryID, null)));
		return theList;
	}

	/**
	 * 
	 * @param element
	 *            a full moby message (root element called MOBY) and may be
	 *            prefixed
	 * @return the serviceNotes element if it exists, null otherwise.
	 */
	public static Element getServiceNotes(Element element) {
		Element serviceNotes = null;
		Element mobyContent = element.getChild("mobyContent");
		if (mobyContent == null)
			mobyContent = element.getChild("mobyContent", MOBY_NS);

		// should throw exception?
		if (mobyContent == null)
			return serviceNotes;

		serviceNotes = mobyContent.getChild("serviceNotes");
		if (serviceNotes == null)
			serviceNotes = mobyContent.getChild("serviceNotes", MOBY_NS);
		// note: servicenotes may be null
		return serviceNotes;
	}

	/**
	 * 
	 * @param xml
	 *            a full moby message (root element called MOBY) and may be
	 *            prefixed
	 * @return the serviceNotes element as a string if it exists, null
	 *         otherwise.
	 */
	public static String getServiceNotes(String xml) {
		try {
			Element e = getServiceNotes(getDOMDocument(xml).getRootElement());
			if (e == null)
				return null;
			XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
			return out.outputString(e);
		} catch (MobyException ex) {
			return null;
		}
	}

	/**
	 * 
	 * @param xml
	 *            a full moby message (root element called MOBY) and may be
	 *            prefixed
	 * @return the serviceNotes element if it exists, null
	 *         otherwise.
	 */
	public static Element getServiceNotesAsElement(String xml) {
		try {
			Element e = getServiceNotes(getDOMDocument(xml).getRootElement());
			return e;
		} catch (MobyException ex) {
			return null;
		}
	}

	/*
	 * messages if the document didnt contain any simples, collections, etc then
	 * why not send return the old message?
	 */


	/**
	 * 
	 * @param element the xml element
	 * @param articleName the name of the child to extract
	 * @return an element that represents the direct child or null if it wasnt found.
	 */
	public static Element getDirectChildByArticleName(Element element, String articleName) {
		List list = element.getChildren();
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			Object object = iter.next();
			if (object instanceof Element) {
				Element child = (Element) object;
				if (child.getAttributeValue("articleName") != null) {
					if (child.getAttributeValue("articleName").equals(articleName))
						return child;
				} else if (child.getAttributeValue("articleName", MOBY_NS) != null) {
					if (child.getAttributeValue("articleName", MOBY_NS).equals(articleName)) {
						return child;
					}
				}
			}

		}
		return null;

	}

	/**
	 * 
	 * @param xml the string of xml
	 * @param articleName the name of the child to extract
	 * @return an xml string that represents the direct child or null if it wasnt found.
	 */
	public static String getDirectChildByArticleName(String xml, String articleName) {
		try {
		Element e = getDirectChildByArticleName(getDOMDocument(xml).getRootElement(), articleName);
		if (e == null)
			return null;
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		return out.outputString(e);
		} catch (MobyException me) {
			return null;
		}
	}

}
