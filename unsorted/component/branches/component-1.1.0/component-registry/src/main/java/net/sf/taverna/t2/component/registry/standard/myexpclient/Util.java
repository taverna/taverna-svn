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

import static java.net.InetAddress.getLocalHost;
import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import net.sf.taverna.t2.component.registry.standard.myexpclient.Resource.Access;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Sergejs Aleksejevs
 */
class Util {
	private static Logger logger = Logger.getLogger(Util.class);

	// ******** DATA ENCRYPTION ********

	private static final String PBE_PASSWORD = System.getProperty("user.home");
	private static final String PBE_SALT;

	static {
		String host_name = "";
		try {
			host_name = getLocalHost().toString();
		} catch (UnknownHostException e) {
			host_name = "unknown_localhost";
		}
		PBE_SALT = host_name;
	}

	/**
	 * The following section (encrypt(), decrypt() and doEncryption() methods)
	 * is used to store user passwords in an encrypted way within the settings
	 * file.
	 */
	public static byte[] encrypt(String str) {
		return doEncryption(str, ENCRYPT_MODE);
	}

	public static byte[] decrypt(String str) {
		return doEncryption(str, DECRYPT_MODE);
	}

	private static final String CRYPTOSUITE = "PBEWithMD5AndDES";

	private static byte[] doEncryption(String str, int mode) {
		/*
		 * password-based encryption uses 2 parameters for processing: a
		 * *password*, which is then hashed with a *salt* to generate a strong
		 * key - these 2 are defined as class constants
		 */
		try {
			SecretKeyFactory keyFactory = SecretKeyFactory
					.getInstance(CRYPTOSUITE);
			SecretKey key = keyFactory.generateSecret(new PBEKeySpec(
					PBE_PASSWORD.toCharArray()));
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(PBE_SALT.getBytes("UTF-8"));
			byte[] digest = md.digest();
			byte[] salt = new byte[8];
			for (int i = 0; i < 8; ++i)
				salt[i] = digest[i];

			Cipher cipher = Cipher.getInstance(CRYPTOSUITE);
			cipher.init(mode, key, new PBEParameterSpec(salt, 20));

			return cipher.doFinal(str.getBytes("UTF-8"));
		} catch (Exception e) {
			logger.error("Could not encrypt and store password");
			logger.error(e.getCause() + "\n" + e.getMessage());
			return new byte[1];
		}
	}

	// ******** DATA RETRIEVAL FROM XML DOCUMENT FRAGMENTS ********

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

	private static List<Element> children(Element element, String name) {
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

		Resource r = new Resource();
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

		User u = new User();
		u.setName(e.getTextContent());
		u.setTitle(e.getTextContent());
		u.setURI(e.getAttribute("uri"));
		u.setResource(e.getAttribute("resource"));
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
	 * Takes XML Element instance with privilege listing for an item and returns
	 * an integer value for that access type.
	 */
	public static Access getAccessType(Element privileges) {
		/*
		 * if the item for which the privileges are processed got received,
		 * there definitely is viewing access to it
		 */
		Access accessType = Access.VIEWING;

		// pick the highest allowed access type
		if (privileges != null)
			for (Element privilege : children(privileges, "privilege")) {
				String strValue = privilege.getAttribute("type");

				Access thisPrivilege = Access.VIEWING;
				if (strValue.equals("download"))
					thisPrivilege = Access.DOWNLOADING;
				else if (strValue.equals("edit"))
					thisPrivilege = Access.EDITING;

				if (thisPrivilege.value() > accessType.value())
					accessType = thisPrivilege;
			}

		return accessType;
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

	// ******** STRIPPING OUT HTML FROM STRINGS ********

	/**
	 * Tiny helper to strip out HTML tags. Basic HTML tags like &nbsp; and <br>
	 * are left in place, because these can be rendered by JLabel. This helps to
	 * present HTML content inside JAVA easier.
	 */
	public static String stripHTML(String source) {
		// don't do anything if not string is provided
		if (source == null)
			return "";

		/*
		 * need to preserve at least all line breaks (ending and starting
		 * paragraph also make a line break)
		 */
		source = source.replaceAll("</p>[\r\n]*<p>", "<br>");
		source = source.replaceAll("\\<br/?\\>", "[-=BR=-]");

		// strip all HTML
		source = source.replaceAll("\\<.*?\\>", "");

		// put the line breaks back
		source = source.replaceAll("\\[-=BR=-\\]", "<br><br>");

		return source;
	}

	/**
	 * Tiny helper to strip out all HTML tags. This will not leave any HTML tags
	 * at all (so that the content can be displayed in DialogTextArea - and the
	 * like - components. This helps to present HTML content inside JAVA easier.
	 */
	public static String stripAllHTML(String source) {
		// don't do anything if not string is provided
		if (source == null)
			return "";

		// need to preserve at least all line breaks
		// (ending and starting paragraph also make a line break)
		source = source.replaceAll("</p>[\r\n]*<p>", "<br>");
		source = source.replaceAll("\\<br/?\\>", "\n\n");

		// strip all HTML
		source = source.replaceAll("\\<.*?\\>", ""); // any HTML tags
		source = source.replaceAll("&\\w{1,4};", ""); // this is for things like
														// "&nbsp;", "&gt;", etc

		return source;
	}

	// ******** VARIOUS HELPERS ********

	/**
	 * The parameter is the class name to be processed; class name is likely to
	 * be in the form <class_name>$<integer_value>, where the trailing part
	 * starting with the $ sign indicates the anonymous inner class within the
	 * base class. This will strip out that part of the class name to get the
	 * base class name.
	 */
	public static String getBaseClassName(String strClassName) {
		// strip out the class name part after the $ sign; return
		// the original value if the dollar sign wasn't found
		String result = strClassName;

		int iDollarIdx = result.indexOf("$");
		if (iDollarIdx != -1)
			result = result.substring(0, iDollarIdx);

		return result;
	}

	/**
	 * Determines whether the plugin is running as a standalone JFrame or inside
	 * Taverna Workbench.
	 */
	public static boolean isRunningInTaverna() {
		try {
			/*
			 * ApplicationRuntime class is defined within Taverna API. If this
			 * is available, it should mean that the plugin runs within Taverna.
			 */
			Class.forName("net.sf.taverna.raven.appconfig.ApplicationRuntime")
					.getMethod("getInstance", new Class<?>[0]).invoke(null);
			return true;
		} catch (NoClassDefFoundError e) {
		} catch (ClassNotFoundException e) {
		} catch (IllegalArgumentException e) {
		} catch (SecurityException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		} catch (NoSuchMethodException e) {
		}
		return false;
	}
}
