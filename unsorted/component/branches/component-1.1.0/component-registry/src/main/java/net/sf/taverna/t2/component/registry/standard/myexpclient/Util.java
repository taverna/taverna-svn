/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
 * 
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.component.registry.standard.myexpclient;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.Type;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Sergejs Aleksejevs
 */
class Util {
	/**
	 * Soft-bound reference to
	 * {@link net.sf.taverna.raven.appconfig.ApplicationRuntime
	 * ApplicationRuntime}.
	 */
	private static final String APPLICATION_RUNTIME = "net.sf.taverna.raven.appconfig.ApplicationRuntime";
	// old format
	private static final DateFormat OLD_DATE_FORMATTER = new SimpleDateFormat(
			"EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
	private static final DateFormat OLD_SHORT_DATE_FORMATTER = new SimpleDateFormat(
			"HH:mm 'on' dd/MM/yyyy", Locale.ENGLISH);
	// universal date formatter
	private static final DateFormat NEW_DATE_FORMATTER = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss Z");

	private static List<Element> elements(NodeList nl) {
		List<Element> result = new ArrayList<Element>(nl.getLength());
		for (int i = 0; i < nl.getLength(); i++)
			if (nl.item(i) instanceof Element)
				result.add((Element) nl.item(i));
		return result;
	}

	static List<Element> children(Element element) {
		if (element == null)
			return Collections.emptyList();
		return elements(element.getChildNodes());
	}

	static List<Element> children(Element element, String name) {
		if (element == null)
			return Collections.emptyList();
		return elements(element.getElementsByTagName(name));
	}

	/**
	 * Instantiates primitive Resource object from XML element. This is very
	 * lightweight and completely generic - it only sets resource's type, title
	 * and URL on myExperiment / URI in the API.
	 */
	public static Resource makeResource(Element e) {
		if (e == null)
			return null;

		@SuppressWarnings("serial")
		Resource r = new Resource(Type.UNKNOWN){};
		r.setItemType(e.getTagName());
		r.setTitle(e.getTextContent());
		r.setURI(e.getAttribute("uri"));
		r.setResource(e.getAttribute("resource"));
		return r;
	}

	/**
	 * Instantiates primitive User object from XML element. This is much more
	 * lightweight than User.buildFromXML() where full details of the user can
	 * be obtained from XML.
	 */
	public static User makeUser(Element e) {
		if (e == null)
			return null;

		User u = User.getExisting(e);
		if (u != null)
			return u;

		u = new User();
		u.setName(e.getTextContent());
		u.setTitle(e.getTextContent());
		u.setURI(e.getAttribute("uri"));
		u.setResource(e.getAttribute("resource"));
		u.setID(e, null);
		return u;
	}

	/**
	 * Instantiates primitive Tag object from XML element. Very lightweight
	 * method.
	 */
	public static Tag makeTag(Element e) {
		if (e == null)
			return null;

		Tag t = new Tag();
		t.setTitle(e.getTextContent());
		t.setTagName(e.getTextContent());
		t.setResource(e.getAttribute("resource"));
		t.setURI(e.getAttribute("uri"));
		return t;
	}

	/**
	 * Generic method that accepts the iterator over a collection of resources
	 * in XML format (obtained from myExperiment API) and an ArrayList to store
	 * the processed results in.
	 * 
	 * The method will extract 3 attributes for every item in the collection: 1)
	 * name of the item; 2) URI of the item (to access this item via the API);
	 * 3) URI of the item (to access via WEB);
	 * 
	 * @return Returns the number of processed elements in the collection.
	 */
	public static int getResourceCollection(NodeList nodes,
			List<Map<String, String>> collection) {
		int i = 0;
		for (Element element : elements(nodes)) {
			// store all details of current group into a hash map
			Map<String, String> itemDetails = new HashMap<String, String>();
			itemDetails.put("name", element.getTextContent());
			itemDetails.put("uri", element.getAttribute("uri"));
			itemDetails.put("resource", element.getAttribute("resource"));

			// add current item to the complete list of items
			collection.add(itemDetails);
			i++;
		}
		return i;
	}

	/**
	 * Returns contents of the "reason" field of the error message.
	 */
	public static String retrieveReasonFromError(Document doc) {
		if (doc == null)
			return "unknown reason";
		for (Element r : children(doc.getDocumentElement(), "reason"))
			return r.getTextContent();
		return "unknown reason";
	}

	static List<Resource> childResources(Element rootElement, String childName) {
		List<Resource> itemList = new ArrayList<Resource>();
		for (Element e : children(getChild(rootElement, childName)))
			itemList.add(makeResource(e));
		return itemList;
	}

	/**
	 * Returns a list of credits - can be applied to any XML document which
	 * contains "credits" element.
	 */
	public static List<Resource> retrieveCredits(Element docRootElement) {
		return childResources(docRootElement, "credits");
	}

	/**
	 * Returns a list of attributions - can be applied to any XML document which
	 * contains "attributions" element.
	 */
	public static List<Resource> retrieveAttributions(Element docRootElement) {
		return childResources(docRootElement, "attributions");
	}

	/**
	 * Returns a list of tags - can be applied to any XML document which
	 * contains "tags" element.
	 */
	public static List<Tag> retrieveTags(Element docRootElement) {
		List<Tag> itemList = new ArrayList<Tag>();
		for (Element tag : children(getChild(docRootElement, "tags")))
			itemList.add(makeTag(tag));
		return itemList;
	}

	static Element getChild(Element elem, String childName) {
		Node n = elem.getElementsByTagName(childName).item(0);
		if (n == null)
			return null;
		return (Element) n;
	}

	static String getChildText(Element elem, String childName) {
		Node n = elem.getElementsByTagName(childName).item(0);
		if (n == null)
			return null;
		String value = n.getTextContent();
		return value == null || value.isEmpty() ? null : value;
	}

	static String getChildText(Element elem, String childName,
			String childChildName) {
		Element child = getChild(elem, childName);
		if (child == null)
			return null;
		Node n = child.getElementsByTagName(childChildName).item(0);
		if (n == null)
			return null;
		String value = n.getTextContent();
		return value == null || value.isEmpty() ? null : value;
	}

	public static Date parseDate(String date) {
		if (date == null || date.isEmpty())
			return null;
		try {
			return OLD_DATE_FORMATTER.parse(date);
		} catch (ParseException e) {
		}
		try {
			return OLD_SHORT_DATE_FORMATTER.parse(date);
		} catch (ParseException e) {
		}
		try {
			return NEW_DATE_FORMATTER.parse(date);
		} catch (ParseException e) {
		}
		return null;
	}

	public static String formatDate(Date date) {
		return NEW_DATE_FORMATTER.format(date);
	}

	/**
	 * Determines whether the plugin is running as a standalone JFrame or inside
	 * Taverna Workbench.
	 */
	public static File getTavernaHomeDir() {
		Logger log = Logger.getLogger(Util.class);
		try {
			/*
			 * ApplicationRuntime class is defined within Taverna API. If this
			 * is available, it should mean that the plugin runs within Taverna.
			 */
			Class<?> appRuntimeClass = Class.forName(APPLICATION_RUNTIME);
			Object runtime = appRuntimeClass.getMethod("getInstance",
					new Class<?>[0]).invoke(null);
			if (runtime == null)
				return null;
			return (File) appRuntimeClass.getMethod("getApplicationHomeDir",
					new Class<?>[0]).invoke(runtime);
		} catch (NoClassDefFoundError e) {
			log.info("could not find ApplicationRuntime or a support class");
		} catch (ClassNotFoundException e) {
			log.info("could not find ApplicationRuntime");
		} catch (IllegalArgumentException e) {
			log.info("could not find valid ApplicationRuntime");
		} catch (SecurityException e) {
			log.info("could not access ApplicationRuntime");
		} catch (IllegalAccessException e) {
			log.info("could not find valid ApplicationRuntime");
		} catch (InvocationTargetException e) {
			log.info("ApplicationRuntime threw unexpected exception",
					e.getCause());
		} catch (NoSuchMethodException e) {
			log.info("could not find valid ApplicationRuntime");
		}
		return null;
	}
}
